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
    val url: String? = null
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
 * @property fromCache True if this result came from the local myth cache rather than a live API call.
 */
data class FactCheckResult(
    val claim: String,
    val verdict: Verdict,
    val explanation: String,
    val sources: List<SourceRef>,
    val languageCode: String,
    val fromCache: Boolean
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
