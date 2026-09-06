package com.colleen.s36349879.medtrack.data.factcheck

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A pre-verified health myth/claim stored locally so the Fact-Check tab can
 * return an instant, offline-capable verdict without calling the live API.
 *
 * Matching is done on [normalizedKeywords] (lowercase, comma-separated keyword
 * fragments) rather than exact claim text, since forwarded messages are rarely
 * worded identically. See [MythCacheDao.findMatch] for the matching query and
 * [FactCheckRepository] for the keyword-overlap scoring done in Kotlin.
 */
@Entity(tableName = "myth_cache_table")
data class MythCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** Short canonical description of the myth, used for logging/debugging. */
    val title: String,

    /** Lowercase keyword fragments (comma-separated) used to match incoming claims. */
    val normalizedKeywords: String,

    /** One of "TRUE", "FALSE", "MISLEADING", "UNVERIFIED" (see [Verdict]). */
    val verdict: String,

    /** English explanation. Other languages fall back to this if not provided. */
    val explanationEn: String,

    /** Bahasa Malaysia explanation, if available. */
    val explanationMs: String? = null,

    /** Source names, semicolon-separated (e.g. "WHO;CDC"). */
    val sourceNames: String,

    /** Source URLs, semicolon-separated, matching [sourceNames] by position. */
    val sourceUrls: String
)
