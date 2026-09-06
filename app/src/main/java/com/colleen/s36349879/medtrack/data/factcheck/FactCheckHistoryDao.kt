package com.colleen.s36349879.medtrack.data.factcheck

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FactCheckHistoryDao {

    @Insert
    suspend fun insert(entry: FactCheckHistory)

    /**
     * All of [patientId]'s past fact-check submissions, newest first, as a reactive
     * stream so the History screen updates immediately after a new check completes.
     */
    @Query("SELECT * FROM fact_check_history_table WHERE patientId = :patientId ORDER BY checkedAtEpochMillis DESC")
    fun getHistoryForPatient(patientId: String): Flow<List<FactCheckHistory>>
}
