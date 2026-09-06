package com.colleen.s36349879.medtrack.data.factcheck

import android.content.Context
import com.colleen.s36349879.medtrack.BuildConfig
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Repository for the Fact-Check tab.
 *
 * IMPORTANT ARCHITECTURE NOTE (read before changing this file):
 * The rest of this app's Gemini calls go through [com.colleen.s36349879.medtrack.data.genAI.GenAIRepository],
 * which wraps the `com.google.ai.client.generativeai` SDK (v0.9.0). That SDK is
 * Google's *deprecated* Android client (superseded by Firebase AI Logic) and its
 * public `Tool` API does not expose Google Search grounding. Rather than pull in
 * the entire Firebase SDK + a Firebase project just for this one tab, this
 * repository talks to the Generative Language REST API directly over OkHttp,
 * using the same API key already stored in [BuildConfig.GEMINI_API_KEY].
 *
 * A second, less obvious constraint shaped this code: Gemini 2.5 models return a
 * 400 error ("Tool use with a response mime type: 'application/json' is
 * unsupported") if you combine the `google_search` tool with `responseSchema` /
 * `responseMimeType: application/json`. That combo only works on Gemini 3+
 * models. Since the project currently targets gemini-2.5-flash, this code asks
 * for JSON via the *prompt* instead of the schema field, and parses the reply
 * leniently (stripping ```json fences) rather than relying on enforced schema.
 * If the project later moves to a Gemini 3.x model, [buildGenerationConfigJson]
 * can add `responseMimeType`/`responseSchema` back in for stricter guarantees.
 */
class FactCheckRepository(
    private val context: Context,
    private val mythCacheDao: MythCacheDao
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json".toMediaType()

    /** The Gemini model used for fact-checking. Kept separate from GenAIRepository's model
     *  constant in case the two tabs need to diverge (e.g. moving Fact-Check to a Gemini 3
     *  model first, once responseSchema + grounding support is confirmed). */
    private val model = "gemini-2.5-flash"

    /**
     * Checks the local myth cache for a claim matching [claim] using simple
     * keyword-overlap scoring (word-level, not exact string match, since
     * forwarded messages are rarely worded identically to a canonical myth).
     *
     * @return A [FactCheckResult] with [FactCheckResult.fromCache] = true if a
     *   confident match is found, or null if nothing scored highly enough and
     *   the caller should fall through to the live API.
     */
    suspend fun checkLocalCache(claim: String, language: FactCheckLanguage): FactCheckResult? {
        val claimWords = tokenize(claim)
        if (claimWords.isEmpty()) return null

        val candidates = mythCacheDao.getAll()
        var best: MythCache? = null
        var bestScore = 0

        for (myth in candidates) {
            val mythWords = myth.normalizedKeywords.split(",").map { it.trim() }.filter { it.isNotBlank() }
            // Count how many of the myth's keyword fragments appear in the claim text.
            val score = mythWords.count { keyword -> claimWords.any { it.contains(keyword) || keyword.contains(it) } }
            if (score > bestScore) {
                bestScore = score
                best = myth
            }
        }

        // Require at least 2 overlapping keyword fragments to avoid false-positive matches
        // (e.g. a single common word like "vaccine" shouldn't be enough on its own).
        if (best == null || bestScore < 2) return null

        val explanation = if (language == FactCheckLanguage.BAHASA_MALAYSIA && !best.explanationMs.isNullOrBlank()) {
            best.explanationMs
        } else {
            best.explanationEn
        }

        val names = best.sourceNames.split(";").filter { it.isNotBlank() }
        val urls = best.sourceUrls.split(";")
        val sources = names.mapIndexed { i, name -> SourceRef(name, urls.getOrNull(i)) }

        return FactCheckResult(
            claim = claim,
            verdict = Verdict.fromApiString(best.verdict),
            explanation = explanation,
            sources = sources,
            languageCode = language.code,
            fromCache = true
        )
    }

    private fun tokenize(text: String): List<String> =
        text.lowercase().split(Regex("[^a-z0-9]+")).filter { it.length > 2 }

    /**
     * Calls Gemini with Google Search grounding enabled to verify [claim].
     * Throws on network/parse failure; callers should catch and surface an
     * "Unverified — couldn't reach verification service" state rather than
     * silently guessing (per the system instruction's own instructions to the model).
     */
    suspend fun checkClaimLive(claim: String, language: FactCheckLanguage): FactCheckResult = withContext(Dispatchers.IO) {
        val systemInstruction = """
            You are a health-claim verification assistant. Given a claim, determine if it is
            True, False, Misleading, or Unverified based on trusted health sources (WHO, CDC,
            national health ministries, peer-reviewed sources). Respond in ${language.displayName}.
            If sources conflict or are insufficient, use "Unverified" rather than guessing.
            Never suggest medication or dosages.

            Respond with ONLY a single raw JSON object (no markdown fences, no commentary
            before or after it) matching exactly this shape:
            {
              "verdict": "true" | "false" | "misleading" | "unverified",
              "explanation": "2-3 sentences in plain language, in ${language.displayName}",
              "sources": [ { "name": "string", "url": "string or empty" } ]
            }
        """.trimIndent()

        val requestBody = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
            })
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", "Claim to verify: $claim")))
                }
            ))
            // Google Search grounding tool. NOTE: do not also set generationConfig.responseSchema /
            // responseMimeType here on gemini-2.5-flash — see class doc comment above.
            put("tools", JSONArray().put(JSONObject().put("google_search", JSONObject())))
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.1)
            })
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
        val request = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", BuildConfig.GEMINI_API_KEY)
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val bodyString = response.body?.string()
                ?: throw IllegalStateException("Empty response from Gemini (HTTP ${response.code})")

            if (!response.isSuccessful) {
                throw IllegalStateException("Gemini request failed (HTTP ${response.code}): $bodyString")
            }

            parseGeminiResponse(bodyString, claim, language)
        }
    }

    /**
     * Parses a raw generateContent response body into a [FactCheckResult].
     *
     * Two things are pulled out separately and merged:
     * 1. The model's text part, which we asked (via prompt) to be raw JSON with
     *    verdict/explanation/sources.
     * 2. `groundingMetadata.groundingChunks[].web.{title,uri}`, the actual search
     *    citations Google Search grounding returns. These are used as a fallback
     *    (and a supplement) if the model's own "sources" field is thin, since the
     *    grounding metadata is the authoritative source list per Gemini's docs.
     */
    private fun parseGeminiResponse(body: String, claim: String, language: FactCheckLanguage): FactCheckResult {
        val root = JsonParser.parseString(body).asJsonObject
        val candidate = root.getAsJsonArray("candidates")?.get(0)?.asJsonObject
            ?: throw IllegalStateException("No candidates returned")

        val textPart = candidate.getAsJsonObject("content")
            ?.getAsJsonArray("parts")
            ?.get(0)?.asJsonObject
            ?.get("text")?.asString
            ?: throw IllegalStateException("No text part in Gemini response")

        // Strip ```json ... ``` fences in case the model wraps the JSON anyway.
        val cleanText = textPart.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val parsed = JsonParser.parseString(cleanText).asJsonObject

        val verdict = Verdict.fromApiString(parsed.get("verdict")?.asString)
        val explanation = parsed.get("explanation")?.asString ?: "No explanation returned."

        val modelSources = parsed.getAsJsonArray("sources")?.mapNotNull { el ->
            val obj = el.asJsonObject
            val name = obj.get("name")?.asString ?: return@mapNotNull null
            val url = obj.get("url")?.asString?.takeIf { it.isNotBlank() }
            SourceRef(name, url)
        } ?: emptyList()

        // Pull grounding citations too (title + uri), and merge in anything the
        // model's own JSON didn't already include.
        val groundingSources = candidate.getAsJsonObject("groundingMetadata")
            ?.getAsJsonArray("groundingChunks")
            ?.mapNotNull { chunk ->
                val web = chunk.asJsonObject.getAsJsonObject("web") ?: return@mapNotNull null
                val title = web.get("title")?.asString ?: return@mapNotNull null
                val uri = web.get("uri")?.asString
                SourceRef(title, uri)
            } ?: emptyList()

        val mergedSources = (modelSources + groundingSources)
            .distinctBy { it.name.lowercase() }
            .ifEmpty { listOf(SourceRef("No sources returned")) }

        return FactCheckResult(
            claim = claim,
            verdict = verdict,
            explanation = explanation,
            sources = mergedSources,
            languageCode = language.code,
            fromCache = false
        )
    }
}
