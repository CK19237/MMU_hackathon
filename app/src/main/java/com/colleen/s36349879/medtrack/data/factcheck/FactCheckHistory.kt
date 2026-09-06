package com.colleen.s36349879.medtrack.data.factcheck

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A per-patient record of one completed fact-check submission, shown on the History
 * screen. Written once at the end of [FactCheckViewModel.verifyClaim], whether the
 * result came from a cache hit or a fresh live Gemini call — this table never runs its
 * own verification logic, it only records the result the existing pipeline already
 * produced (see [FactCheckRepository]/[FactCheckModels] for that pipeline).
 *
 * Source fields are semicolon-joined, matching the existing convention used by
 * [MythCache] and [VerifiedClaimCache].
 */
@Entity(tableName = "fact_check_history_table")
data class FactCheckHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val patientId: String,
    val title: String,
    val originalMessage: String,
    val verdict: String,
    val explanation: String,
    val sourceNames: String,
    val sourceUrls: String,
    val sourceTypes: String,
    val languageCode: String,
    val officialEvidenceFound: Boolean,
    val checkedAtEpochMillis: Long
)
