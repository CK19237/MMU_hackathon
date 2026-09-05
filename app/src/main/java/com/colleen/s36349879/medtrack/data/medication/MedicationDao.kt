package com.colleen.s36349879.medtrack.data.medication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the medication_table.
 *
 * Provides the SQL queries and Room operations needed to insert,
 * retrieve, and update [Medication] records in the database.
 * Functions returning [Flow] will automatically emit new values
 * whenever the underlying data changes.
 */
@Dao
interface MedicationDao {

    /**
     * Inserts a list of medications into the database in a single operation.
     * Useful for pre-populating or batch-adding medications for a patient.
     *
     * @param medications The list of [Medication] records to insert.
     */
    @Insert
    suspend fun insertAll(medications: List<Medication>)

    /**
     * Retrieves all medications assigned to a specific patient as a reactive stream.
     * The [Flow] will re-emit whenever the patient's medication data changes.
     *
     * @param pid The ID of the patient whose medications should be retrieved.
     * @return A [Flow] emitting the list of [Medication] records for the given patient.
     */
    @Query("SELECT * FROM medication_table WHERE patientID = :pid") // Filters by patientId to scope results to one patient
    fun getMedicationsForPatient(pid: String): Flow<List<Medication>>

    /**
     * Inserts a single medication record into the database.
     *
     * @param med The [Medication] record to insert.
     */
    @Insert
    suspend fun insertMedication(med: Medication)

    /**
     * Updates the taken status and last taken date for a specific medication.
     * Called when a patient marks a medication as taken or untaken.
     *
     * @param medId The ID of the medication record to update.
     * @param status True if the medication has been taken, false otherwise.
     * @param date The date the medication was taken, formatted as a string.
     */
    @Query("UPDATE medication_table SET isTaken = :status, lastTakenDate = :date WHERE id = :medId") // Targets a single record by its primary key
    suspend fun updateTakenStatus(medId: Int, status: Boolean, date: String)

    /**
     * Returns the total number of medication records across all patients as a reactive stream.
     * The [Flow] will re-emit whenever the total count changes.
     *
     * @return A [Flow] emitting the total count of medications in the database.
     */
    @Query("SELECT COUNT(*) FROM medication_table") // Counts all rows regardless of patient
    fun getTotalMedicationCount(): Flow<Int>

}