package com.colleen.s36349879.medtrack.data.healthassistant

import android.content.Context
import com.colleen.s36349879.medtrack.data.genAI.GenAIRepository

/**
 * Repository for the Health Assistant tab (general informational guidance only).
 *
 * Reuses the existing [GenAIRepository] / plain-text Gemini call rather than
 * the grounded REST path used by Fact-Check — this tab doesn't need Google
 * Search grounding, it needs a tightly-scoped, safety-filtered response.
 *
 * Safety is enforced in two layers, per the hard requirement that the AI must
 * never suggest medication names or dosages anywhere in the app:
 * 1. The system prompt instructs the model never to name medications or doses.
 * 2. [sanitizeResponse] runs a second, independent pass over the model's own
 *    output and strips/redacts anything that looks like a dosage or drug
 *    reference, because prompt instructions alone are not a hard guarantee.
 */
class HealthAssistantRepository(context: Context) {

    private val genAiRepository = GenAIRepository(context)

    /** Common dosage-unit patterns, e.g. "500mg", "10 ml", "2 tablets twice a day". */
    private val dosagePattern = Regex(
        """\b\d+\s*(mg|milligrams?|ml|millilit(er|re)s?|mcg|micrograms?|g|grams?|iu|tablets?|capsules?|pills?|doses?)\b""",
        RegexOption.IGNORE_CASE
    )

    /** Phrases that indicate the model is about to name/recommend a specific drug or brand. */
    private val recommendationPattern = Regex(
        """\b(take|try|use)\s+\d*\s*(mg|ml)?\s*of?\s*[A-Z][a-zA-Z]+""",
    )

    suspend fun getGuidance(symptoms: String, vitals: String, recentMedications: List<String>): String? {
        val medContext = if (recentMedications.isNotEmpty()) {
            "The patient is currently taking (for context only, do not comment on dosing): ${recentMedications.joinToString(", ")}."
        } else {
            "No current medications on file."
        }

        val prompt = """
            You are a cautious health information assistant. You provide general,
            plain-language educational information ONLY — never a diagnosis, never a
            treatment plan, and never medication names or dosages of any kind, including
            over-the-counter products. If the user's message asks about or implies a need
            for medication, respond with general category-level guidance only
            (e.g. "over-the-counter pain relief" is acceptable; a specific drug name or
            dose, e.g. "ibuprofen 400mg", is NOT).

            $medContext

            Reported symptoms: $symptoms
            Reported vitals: ${vitals.ifBlank { "None provided" }}

            TASK: In 3-5 short sentences, explain in plain language what these symptoms
            could generally relate to and what general self-care or monitoring steps are
            reasonable, while being clear this is not medical advice. Always end by
            recommending the person consult a doctor, especially for any severe,
            worsening, or unusual symptoms.
        """.trimIndent()

        val rawResponse = genAiRepository.getAiResponse(prompt) ?: return null
        return sanitizeResponse(rawResponse)
    }

    /**
     * Second-pass safety filter over the model's raw text. Redacts anything
     * matching a dosage pattern and appends a note if a redaction occurred, so
     * the flagging clinician can see that a filter fired on this response.
     */
    private fun sanitizeResponse(raw: String): String {
        var redacted = false
        var text = dosagePattern.replace(raw) { redacted = true; "[redacted]" }
        text = recommendationPattern.replace(text) { redacted = true; "[redacted]" }

        val consultNote = "\n\nPlease consult a doctor or pharmacist for diagnosis and treatment."
        val withNote = if (text.contains("consult a doctor", ignoreCase = true) ||
            text.contains("consult a pharmacist", ignoreCase = true)
        ) text else text + consultNote

        return if (redacted) {
            "$withNote\n\n(Note: part of this response was automatically removed because it referenced a specific dose or medication — please ask a doctor or pharmacist directly for that information.)"
        } else {
            withNote
        }
    }
}
