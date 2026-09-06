package com.colleen.s36349879.medtrack.data.factcheck

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * Data Access Object for the curated Malaysian government evidence table.
 *
 * As with [MythCacheDao], the `LIKE`-free "return everything" query is
 * intentional: the actual keyword-overlap scoring against the user's claim
 * happens in Kotlin (see [EvidenceRetriever]), since Room can't score partial
 * keyword overlap or apply claim-type-dependent source ordering on its own.
 * The evidence table is small (hand-curated), so this is cheap.
 */
@Dao
interface GovEvidenceDao {

    /** Inserts the seed evidence list. Safe to call once; see [GovEvidenceSeedData.seedIfEmpty]. */
    @Insert
    suspend fun insertAll(entries: List<GovEvidence>)

    @Query("SELECT COUNT(*) FROM gov_evidence_table")
    suspend fun count(): Int

    /** Returns every curated evidence entry so [EvidenceRetriever] can score them against the claim. */
    @Query("SELECT * FROM gov_evidence_table")
    suspend fun getAll(): List<GovEvidence>
}
