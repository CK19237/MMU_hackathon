package com.colleen.s36349879.medtrack.data.factcheck

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * Data Access Object for the dynamic, runtime-populated verified-claim cache.
 * Same "return everything, score in Kotlin" pattern as [MythCacheDao] and
 * [GovEvidenceDao] — kept consistent across the Fact-Check tab's tables.
 */
@Dao
interface VerifiedClaimCacheDao {

    @Insert
    suspend fun insert(entry: VerifiedClaimCache)

    @Query("SELECT * FROM verified_claim_cache_table")
    suspend fun getAll(): List<VerifiedClaimCache>
}
