package com.colleen.s36349879.medtrack.data.tip

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the med_coach_tips table.
 *
 * Provides the SQL queries and Room operations needed to insert and
 * retrieve [MedCoachTip] records from the database.
 * Functions returning [Flow] will automatically emit new values
 * whenever the underlying data changes.
 */
@Dao
interface TipDao {

    /**
     * Inserts a single AI-generated tip record into the database.
     *
     * @param tip The [MedCoachTip] record to insert.
     */
    @Insert
    suspend fun insertTip(tip: MedCoachTip)


    /**
     * Retrieves all tips for a specific patient as a reactive stream,
     * ordered from most recent to oldest by timestamp.
     *
     * The [Flow] will re-emit whenever the patient's tip history changes,
     * ensuring the UI always reflects the latest saved tips.
     *
     * @param pid The ID of the patient whose tips should be retrieved.
     * @return A [Flow] emitting the list of [MedCoachTip] records for the given patient.
     */
    @Query("SELECT * FROM med_coach_tips WHERE patientId = :pid ORDER BY timestamp DESC")
    fun getTipsForPatient(pid: String): Flow<List<MedCoachTip>>
}