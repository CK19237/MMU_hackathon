package com.colleen.s36349879.medtrack.data.patient
import com.colleen.s36349879.medtrack.data.patient.Patient

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the patient_table.
 *
 * Provides the SQL queries and Room operations needed to insert, update,
 * and retrieve [Patient] records from the database.
 * Functions returning [Flow] will automatically emit new values
 * whenever the underlying data changes.
 */
@Dao
interface PatientDao {

    /**
     * Inserts a list of patients into the database in a single operation.
     * Used during CSV and JSON seeding on first launch.
     *
     * @param patients The list of [Patient] records to insert.
     */
    @Insert
    suspend fun insertAll(patients: List<Patient>)

    /**
     * Inserts a single patient record into the database.
     * Used when a new patient registers through the app.
     *
     * @param patient The [Patient] record to insert.
     */
    @Insert
    suspend fun insertPatient(patient: Patient)

    /**
     * Updates an existing patient record in the database.
     * Room matches the record to update using the [Patient.patientId] primary key.
     *
     * @param patient The [Patient] record containing the updated values.
     */
    @Update
    suspend fun updatePatient(patient: Patient)

    /**
     * Retrieves all patients from the database as a reactive stream.
     * The [Flow] will re-emit whenever any patient record changes.
     *
     * @return A [Flow] emitting the full list of [Patient] records.
     */
    @Query("SELECT * FROM patient_table")
    fun getAllPatients(): Flow<List<Patient>>

    /**
     * Retrieves a specific patient as a reactive stream, matched by their ID.
     * The [Flow] will re-emit whenever that patient's record changes.
     * Emits null if no patient with the given ID exists.
     *
     * @param pid The ID of the patient to observe.
     * @return A [Flow] emitting the matching [Patient], or null if not found.
     */
    @Query("SELECT * FROM patient_table WHERE patientId = :pid") // Filters to a single patient by primary key
    fun getPatientFlow(pid: String): Flow<Patient?>

    /**
     * Retrieves a single patient by their ID as a one-shot suspend call.
     * Unlike [getPatientFlow], this does not observe changes but returns once and completes.
     *
     * @param pid The ID of the patient to retrieve.
     * @return The matching [Patient], or null if not found.
     */
    @Query ("SELECT * FROM patient_table WHERE patientId = :pid") // Same filter as getPatientFlow but returns once, not as a stream
    suspend fun getPatientById(pid: String): Patient?

    /**
     * Retrieves a patient by their phone number as a one-shot suspend call.
     * Used during registration to check whether a phone number is already in use.
     *
     * @param phone The phone number to search for.
     * @return The matching [Patient], or null if no patient has that phone number.
     */
    @Query("SELECT * FROM patient_table WHERE phoneNumber = :phone") // Used to enforce unique phone numbers at registration
    suspend fun getPatientByPhone(phone: String): Patient?

    /**
     * Retrieves the ID of the most recently inserted patient, ordered by ID descending.
     * Used to generate the next sequential patient ID during registration.
     *
     * @return The highest patient ID string currently in the table, or null if the table is empty.
     */
    @Query("SELECT patientId FROM patient_table ORDER BY patientId DESC LIMIT 1") // DESC order ensures the latest ID is returned first
    suspend fun getLastPatientId(): String?

    /**
     * Retrieves a patient matching both their ID and phone number.
     * Used during account claiming to verify the patient's identity before
     * allowing them to set a new password.
     *
     * @param id The ID of the patient to verify.
     * @param phone The phone number that must match the given ID.
     * @return The matching [Patient] if both fields match, or null if verification fails.
     */
    @Query("SELECT * FROM patient_table WHERE patientId = :id AND phoneNumber = :phone") // Both conditions must match for the query to return a result
    suspend fun getPatientByIdAndPhone(id: String, phone: String): Patient?

    /**
     * Returns the total number of patient records in the database as a reactive stream.
     * The [Flow] will re-emit whenever the total count changes.
     *
     * @return A [Flow] emitting the total count of patients in the database.
     */
    @Query("SELECT COUNT(*) FROM patient_table") // Counts all rows regardless of any filters
    fun getTotalPatientCount(): Flow<Int>
}