package com.colleen.s36349879.medtrack.data.patient


import android.content.Context
import com.colleen.s36349879.medtrack.data.MedTrackDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsible for managing patient data from the local Room database.
 *
 * @param context The application context used to access the database.
 */
class PatientRepository (context: Context) {

    /** The DAO used to perform local database operations on the patient_table.*/
    private val patientDao = MedTrackDatabase.getDatabase(context).patientDao()

    /**
     * Inserts a list of patients into the database in a single operation.
     * Used during CSV and JSON seeding on first launch.
     *
     * @param patients The list of [Patient] records to insert.
     */
    suspend fun insertAll(patients: List<Patient>) { patientDao.insertAll(patients)}

    /**
     * Retrieves a specific patient as a reactive stream, matched by their ID.
     * The [Flow] will re-emit whenever that patient's record changes.
     *
     * @param pid The ID of the patient to observe.
     * @return A [Flow] emitting the matching [Patient], or null if not found.
     */
    fun getPatientById(pid: String): Flow<Patient?> = patientDao.getPatientFlow(pid)

    /**
     * Inserts a single new patient record into the database.
     * Used when a patient completes registration.
     *
     * @param patient The [Patient] record to insert.
     */
    suspend fun addPatient(patient: Patient) { patientDao.insertPatient(patient) }

    /**
     * Retrieves a patient by their phone number as a one-shot suspend call.
     * Used during registration to check whether a phone number is already in use.
     *
     * @param phone The phone number to search for.
     * @return The matching [Patient], or null if no patient has that phone number.
     */
    suspend fun getPatientByPhone(phone: String): Patient?
    {
        return patientDao.getPatientByPhone(phone)
    }

    /**
     * Validates a patient's credentials and returns the patient if login succeeds.
     *
     * Fetches the patient by ID and compares the stored password against the input.
     * Returns null if the patient does not exist or the password does not match.
     *
     * @param pid The ID entered by the patient attempting to log in.
     * @param pass The password entered by the patient attempting to log in.
     * @return The matching [Patient] if credentials are valid, or null if authentication fails.
     */
    suspend fun login(pid: String, pass: String): Patient? {
        val patient = patientDao.getPatientById(pid)

        // Return the patient only if they exist and the password matches
        return if (patient != null && patient.password == pass) {
            patient
        } else {
            null
        }
    }

    /**
     * Attempts to claim an existing patient account by verifying the ID and phone number,
     * then updating the password if both fields match.
     *
     * Used when a pre-seeded patient sets up their own login credentials for the first time.
     *
     * @param id The ID of the account to claim.
     * @param phone The phone number that must match the account for verification.
     * @param newPass The new password to set on the account if verification succeeds.
     * @return True if the account was successfully claimed, false if verification failed.
     */
    suspend fun claimAccount(id: String, phone: String, newPass: String): Boolean {
        val patient = patientDao.getPatientById(id) // Fetch the patient by ID first
        return if (patient != null && patient.phoneNumber == phone) {

            // Verification passed so create an updated copy with the new password
            val updatedPatient = patient.copy(password = newPass)
            patientDao.updatePatient(updatedPatient) // Persist the password change to the database
            true
        } else {
            false
        }
    }

    /**
     * Generates the next sequential patient ID based on the highest existing ID in the database.
     *
     * IDs follow the format "P{number}" (e.g. "P1001"). If the table is empty,
     * the sequence starts from "P1001" using "P1000" as the default base.
     *
     * @return A new unique patient ID string (e.g. "P1001").
     */
    suspend fun generateNewId(): String {
        val lastId = patientDao.getLastPatientId() ?: "P1000" // Default base if the table is empty
        val numericPart = lastId.substring(1).toIntOrNull() ?: 1000 // Strip the P prefix and parse the number
        return "P${numericPart + 1}"  // Increment by 1 to produce the next ID
    }

    /**
     * Returns the total number of patient records in the database as a reactive stream.
     * The [Flow] will re-emit whenever the total count changes.
     *
     * @return A [Flow] emitting the total count of patients in the database.
     */
    fun getTotalPatientCount(): Flow<Int> = patientDao.getTotalPatientCount()
}