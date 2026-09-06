package com.colleen.s36349879.medtrack.data.factcheck

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A previously live-verified claim, saved so the next equivalent claim can
 * reuse the result instead of calling evidence retrieval + the LLM again.
 *
 * Deliberately kept separate from [MythCache]: [MythCache] is a small,
 * hand-curated, hand-vetted list of well-known general myths (WHO/CDC/FDA
 * sourced) seeded once at build time, while this table is populated
 * automatically at runtime from live fact-checks. Mixing LLM-produced rows
 * into the hand-vetted table would make it harder to trust/audit the curated
 * list, so results build up here instead — [FactCheckRepository] checks both
 * (myth cache first, then this cache) before ever hitting the network.
 */
@Entity(tableName = "verified_claim_cache_table")
data class VerifiedClaimCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** The claim exactly as the user originally typed/spoke it. */
    val originalClaim: String,

    /** Lowercase keyword fragments (comma-separated) derived from [originalClaim], used for matching future claims. */
    val normalizedKeywords: String,

    /** One of "TRUE", "FALSE", "MISLEADING", "UNVERIFIED" (see [Verdict]). */
    val verdict: String,

    /** English explanation returned by the LLM. */
    val explanationEn: String,

    /** The evidence text the LLM was given, kept for traceability/audit. Null if this came from the ungrounded fallback path. */
    val evidenceUsed: String?,

    /** Source names, semicolon-separated. */
    val sourceNames: String,

    /** Source URLs, semicolon-separated, matching [sourceNames] by position. */
    val sourceUrls: String,

    /** Source types, semicolon-separated (e.g. "NPRA", "KKM_CPG", "web"), matching [sourceNames] by position. */
    val sourceTypes: String,

    /** False if no official Malaysian government evidence was found for this claim (i.e. the ungrounded/general-web fallback was used). */
    val officialEvidenceFound: Boolean,

    /** Epoch millis when this fact-check was performed. */
    val dateCheckedEpochMillis: Long
)
