package com.colleen.s36349879.medtrack.data.doctorreview

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Which tab a flagged item originated from. */
enum class FlaggedSourceTab {
    FACT_CHECK,
    HEALTH_ASSISTANT
}

/** Review status of a flagged item, set by a clinician on the Doctor Review screen. */
enum class ReviewStatus {
    PENDING,
    REVIEWED,
    CORRECTED
}

/**
 * A single item in the async Doctor Review queue.
 *
 * This is intentionally decoupled from [com.colleen.s36349879.medtrack.data.factcheck.FactCheckResult]
 * and the Health Assistant's response type — it stores plain strings so one
 * simple table and DAO can serve both tabs ("flag this verdict" /
 * "flag this response"), per the hard requirement that doctor review must not
 * block the main user flow. Nothing here is looked at until a clinician opens
 * the Doctor Review screen; the original AI response has already been shown
 * to the user before this row is even inserted.
 */
@Entity(tableName = "flagged_item_table")
data class FlaggedItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val sourceTab: FlaggedSourceTab,

    /** The user's original claim (Fact-Check) or symptoms/vitals summary (Health Assistant). */
    val originalInput: String,

    /** The AI's verdict+explanation (Fact-Check) or guidance text (Health Assistant). */
    val aiOutput: String,

    val languageCode: String,

    val flaggedAtEpochMillis: Long,

    val status: ReviewStatus = ReviewStatus.PENDING,

    /** Optional clinician note added when marking this item reviewed/corrected. */
    val clinicianNote: String? = null
)
