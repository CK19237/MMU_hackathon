package com.colleen.s36349879.medtrack.data.factcheck

/**
 * The verdict returned for a health claim after fact-checking.
 *
 * Maps directly to the "verdict" enum value requested from the Gemini
 * structured-output schema, plus [CACHE_MATCH] which is used internally
 * when a claim is resolved from the local myth cache instead of the API.
 */
enum class Verdict {
    TRUE,
    FALSE,
    MISLEADING,
    UNVERIFIED;

    companion object {
        /** Parses the lowercase string Gemini returns (e.g. "false") into a [Verdict]. */
        fun fromApiString(value: String?): Verdict {
            return when (value?.trim()?.lowercase()) {
                "true" -> TRUE
                "false" -> FALSE
                "misleading" -> MISLEADING
                else -> UNVERIFIED
            }
        }

        /**
         * Maps the 5-category verdict vocabulary used when the LLM is given
         * retrieved evidence (SUPPORTED / UNSUPPORTED / MISLEADING /
         * POTENTIALLY_HARMFUL / INSUFFICIENT_EVIDENCE) onto this app's
         * existing 4-value [Verdict], so the UI/cache/seed data don't need to
         * change. POTENTIALLY_HARMFUL collapses into [FALSE] — the claim is
         * still false/unsupported, and the "potentially harmful" nuance is
         * preserved in the explanation text instead (see
         * [FactCheckRepository.checkClaimLive]).
         */
        fun fromEvidenceBackedApiString(value: String?): Verdict {
            return when (value?.trim()?.uppercase()) {
                "SUPPORTED" -> TRUE
                "UNSUPPORTED" -> FALSE
                "MISLEADING" -> MISLEADING
                "POTENTIALLY_HARMFUL" -> FALSE
                "INSUFFICIENT_EVIDENCE" -> UNVERIFIED
                else -> fromApiString(value)
            }
        }
    }
}

/**
 * A single supporting source returned alongside a verdict.
 *
 * @property name Display name of the source (e.g. "WHO", "CDC", or a search result title).
 * @property url Optional link to the source. May be null if Gemini only returned a name.
 */
data class SourceRef(
    val name: String,
    val url: String? = null,
    /** e.g. "NPRA", "KKM_CPG", "NHMS", "DATA_GOV_MY", or "web" for a general grounding citation. Defaults to "web" for backward compatibility with existing call sites. */
    val sourceType: String = "web"
)

/**
 * The full result of a fact-check, whether it came from the local myth
 * cache or a live grounded Gemini call.
 *
 * @property claim The original claim text submitted by the user.
 * @property verdict The determined [Verdict].
 * @property explanation A 2-3 sentence plain-language explanation.
 * @property sources Supporting sources for the verdict.
 * @property languageCode BCP-47-ish code the explanation is written in (e.g. "en", "ms").
 * @property fromCache True if this result came from the local myth cache or the verified-claim cache rather than a live API call.
 * @property officialEvidenceFound True if this result was grounded in curated Malaysian government evidence (NPRA/KKM/NHMS/data.gov.my) or a cached result derived from one; false if no such evidence was found and the general web-grounded fallback was used instead. Defaults to true for backward compatibility with the pre-existing myth cache path.
 */
data class FactCheckResult(
    val claim: String,
    val verdict: Verdict,
    val explanation: String,
    val sources: List<SourceRef>,
    val languageCode: String,
    val fromCache: Boolean,
    val officialEvidenceFound: Boolean = true
)

/** Supported languages for the Fact-Check tab's language selector. */
enum class FactCheckLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    BAHASA_MALAYSIA("ms", "Bahasa Malaysia"),
    CHINESE("zh", "中文"),
    TAMIL("ta", "தமிழ்");

    companion object {
        val DEFAULT = ENGLISH
    }
}
