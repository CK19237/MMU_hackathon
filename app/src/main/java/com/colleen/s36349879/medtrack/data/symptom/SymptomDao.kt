package com.colleen.s36349879.medtrack.data.symptom

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the symptom_table.
 *
 * Provides the SQL queries and Room operations needed to insert and
 * retrieve [Symptom] records from the database.
 * Functions returning [Flow] will automatically emit new values
 * whenever the underlying data changes.
 */
@Dao
interface SymptomDao {

    /**
     * Inserts a single symptom record into the database.
     * Used when a patient logs a new symptom through the app.
     *
     * @param symptom The [Symptom] record to insert.
     */
    @Insert
    suspend fun insertSymptom(symptom: Symptom)

    /**
     * Inserts a list of symptoms into the database in a single operation.
     * Used during CSV and JSON seeding on first launch.
     *
     * @param symptoms The list of [Symptom] records to insert.
     */
    @Insert
    suspend fun insertAll(symptoms: List<Symptom>)

    /**
     * Retrieves all symptoms for a specific patient as a reactive stream.
     * The [Flow] will re-emit whenever that patient's symptom data changes.
     *
     * @param pid The ID of the patient whose symptoms should be retrieved.
     * @return A [Flow] emitting the list of [Symptom] records for the given patient.
     */
    @Query("SELECT * FROM symptom_table WHERE patientID = :pid")  // Filters by patientId to scope results to one patient
    fun getSymptomsForPatient(pid: String): Flow<List<Symptom>>

    /**
     * Returns the symptom category that appears most frequently across all patients
     * as a reactive stream.
     *
     * Groups all symptoms by category, counts occurrences, and returns the top result.
     * Emits null if the table is empty.
     *
     * @return A [Flow] emitting the most common category string, or null if no data exists.
     */
    @Query("SELECT category FROM symptom_table GROUP BY category ORDER BY COUNT(*) DESC LIMIT 1")
    // GROUP BY groups rows with the same category together
    // COUNT(*) counts how many times each category appears
    // ORDER BY COUNT(*) DESC sorts from most to least frequent
    // LIMIT 1 returns only the single most common category
    fun getMostCommonCategory(): Flow<String?>

    /**
     * Returns the average severity score across all symptom records as a reactive stream.
     *
     * Uses SQL AVG() which returns null when the table is empty rather than 0,
     * so the return type is nullable and the ViewModel maps null to 0.0.
     *
     * @return A [Flow] emitting the average severity as a [Double], or null if no data exists.
     */
    @Query("SELECT AVG(severity) FROM symptom_table") // AVG() returns null on an empty table, hence Flow<Double?>
    fun getAverageSeverity(): Flow<Double?>

    /**
     * Returns the severity scores of the 7 most recently inserted symptoms
     * as a reactive stream. Used to populate the severity trend chart.
     *
     * Orders by [Symptom.id] descending so the latest records are returned first,
     * regardless of the recorded date and time.
     *
     * @return A [Flow] emitting a list of up to 7 recent severity values as integers.
     */
    @Query("SELECT severity FROM symptom_table ORDER BY id DESC LIMIT 7")
    // ORDER BY id DESC retrieves the most recently inserted rows first
    // LIMIT 7 caps the result to the last 7 symptoms for the trend chart
    fun getRecentSeverities(): Flow<List<Int>>
}