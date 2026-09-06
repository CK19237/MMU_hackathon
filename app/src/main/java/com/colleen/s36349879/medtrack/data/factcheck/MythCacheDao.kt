package com.colleen.s36349879.medtrack.data.factcheck

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * Data Access Object for the local health-myth cache.
 *
 * Room's `LIKE` matching is used only as a cheap pre-filter; the final
 * keyword-overlap scoring against the user's claim happens in Kotlin inside
 * [FactCheckRepository.checkLocalCache], since Room can't score partial
 * keyword overlap on its own.
 */
@Dao
interface MythCacheDao {

    /** Inserts the seed myth list. Safe to call once; see [seedMythCacheIfEmpty]. */
    @Insert
    suspend fun insertAll(myths: List<MythCache>)

    @Query("SELECT COUNT(*) FROM myth_cache_table")
    suspend fun count(): Int

    /** Returns every cached myth so the repository can score them against the claim. */
    @Query("SELECT * FROM myth_cache_table")
    suspend fun getAll(): List<MythCache>
}
