package com.colleen.s36349879.medtrack.data.doctorreview

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the async Doctor Review queue. All writes here happen *after* the
 * user has already seen the AI's response — nothing in this file is on the
 * critical path of the Fact-Check or Health Assistant tabs.
 */
@Dao
interface FlaggedItemDao {

    @Insert
    suspend fun insert(item: FlaggedItem)

    /** Reactive stream of pending items for the clinician's review screen. */
    @Query("SELECT * FROM flagged_item_table WHERE status = 'PENDING' ORDER BY flaggedAtEpochMillis DESC")
    fun getPending(): Flow<List<FlaggedItem>>

    /** Reactive stream of every flagged item, pending or resolved, newest first. */
    @Query("SELECT * FROM flagged_item_table ORDER BY flaggedAtEpochMillis DESC")
    fun getAll(): Flow<List<FlaggedItem>>

    @Query("UPDATE flagged_item_table SET status = :status, clinicianNote = :note WHERE id = :id")
    suspend fun updateStatus(id: Int, status: ReviewStatus, note: String?)
}
