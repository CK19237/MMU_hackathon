package com.colleen.s36349879.medtrack.data.patient

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.colleen.s36349879.medtrack.data.isDatabaseSeeded
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import com.colleen.s36349879.medtrack.data.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.String


/**
 * ViewModel responsible for managing patient data, authentication, and session handling.
 *
 * This ViewModel communicates with [PatientRepository] for database operations and
 * delegates session management to [AuthManager]. It also handles seeding the patient
 * database from CSV and JSON sources on first launch.
 *
 * @param context The application context used to initialize the repository and access assets.
 */
class PatientViewModel (private val context: Context): ViewModel() {

    /** The repository used for all patient database operations. */
    private val patientRepository: PatientRepository = PatientRepository(context)

    private val gson = Gson()

    /**
     * Checks whether a patient is currently logged in by querying [AuthManager].
     *
     * @return True if a patient session is active, false otherwise.
     */
    fun isUserLoggedIn(): Boolean = AuthManager.isLoggedIn()

    /**
     * Retrieves the currently logged-in patient as a reactive stream.
     * Returns a [Flow] emitting null if no patient is found for the active session ID.
     *
     * @return A [Flow] emitting the current [Patient], or null if not found.
     */
    fun getCurrentPatient(): Flow<Patient?> {
        val id = AuthManager.getPatientSession() ?: ""
        return patientRepository.getPatientById(id)
    }

    /**
     * Returns the patient ID of the currently logged-in patient.
     * Returns an empty string if no session is active.
     *
     * @return The logged-in patient's ID string, or an empty string if unauthenticated.
     */
    fun getLoggedInPatientId(): String {
        //Get the id
        val id = AuthManager.getPatientSession()

        //Cast it to String and if it's null, return an empty string
        return (id as? String) ?: ""
    }

    /**
     * Attempts to log in a patient using their ID and password.
     *
     * If the credentials are valid, the session is saved via [AuthManager] and
     * [onResult] is invoked with true. Otherwise, [onResult] is invoked with false.
     *
     * @param id The patient's ID.
     * @param pass The patient's password.
     * @param onResult A callback invoked with true on success, false on failure.
     */
    fun login(id: String, pass: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val patient = patientRepository.login(id, pass)
            if (patient != null) {
                // Session is saved
                AuthManager.savePatientSession(id)
                onResult(true)
            } else {
                onResult(false) // Invalid credentials; do not save a session
            }
        }
    }

    /**
     * Registers a new patient with the provided details.
     *
     * First checks whether the phone number is already associated with an existing account.
     * If the phone is taken, [onResult] is invoked with null to signal the conflict.
     * On success, a new patient ID is generated, the patient is saved, and [onResult]
     * is invoked with the new ID.
     *
     * @param name The patient's full name.
     * @param phone The patient's phone number, used as a unique identifier check.
     * @param pass The patient's chosen password.
     * @param onResult A callback invoked with the new patient ID on success, or null if the phone already exists.
     */
    fun register(name: String, phone: String, pass: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            //Check if phone exists
            val existing = patientRepository.getPatientByPhone(phone)

            if (existing != null) {
                onResult(null) // Return null to signal that phone already exists
            } else {
                //Phone is unique, proceed with registration
                val newId = patientRepository.generateNewId()
                val newPatient = Patient(
                    patientId = newId,
                    patientName = name,
                    phoneNumber = phone,
                    password = pass
                )
                patientRepository.addPatient(newPatient)
                onResult(newId) // Return the new ID
            }
        }
    }

    /**
     * Attempts to claim an existing patient account by verifying the ID and phone number,
     * then updating the password if the details match.
     *
     * Used when a pre-seeded patient needs to set up their own login credentials.
     *
     * @param id The ID of the account to claim.
     * @param phone The phone number associated with the account, used for verification.
     * @param newPass The new password to set on the account.
     * @param onResult A callback invoked with true if the claim succeeded, false otherwise.
     */
    fun claimAccount(id: String, phone: String, newPass: String, onResult: (Boolean) -> Unit){
        viewModelScope.launch {
            val success = patientRepository.claimAccount(id, phone, newPass)
            onResult(success) // Notify the UI whether the claim was successful
        }
    }

    /** Logs out the current patient by clearing their session from [AuthManager].*/
    fun logout() {
        AuthManager.clearSession() // Removes the stored session so isUserLoggedIn() returns false
    }

    /**
     * Seeds the patient database from the CSV asset file
     * and any migrated patient JSON stored in [AuthManager].
     *
     * @param context The context used to open the CSV asset file.
     */
    suspend fun seedPatient(context: Context) {
        // withContext(Dispatchers.IO) temporarily switches this code to a background thread
        // do this because these operations can be slow, and running them on the main thread
        // would freeze the UI
        // Once the code inside finishes, it automatically switches back to the main thread
        withContext(Dispatchers.IO) {
            try {
                val patientList = mutableListOf<Patient>()

                //CSV
                context.assets.open("patients.csv").use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream))
                    reader.readLine() // Skip header
                    reader.forEachLine { line ->
                        val cleanLine = line.replace("\uFEFF", "")
                        if (cleanLine.isNotBlank()) {
                            val row = splitCsvLine(cleanLine)
                            if (row.size >= 4) {
                                patientList.add(
                                    Patient(
                                        patientId = row[0],
                                        phoneNumber = row[1],
                                        patientName = row[2],
                                        password = row[3]
                                    )
                                )
                            }
                        }
                    }
                }

                //JSON Migration
                AuthManager.getOldPatientsJson()?.let { json ->
                    val type = object : TypeToken<List<Patient>>() {}.type
                    val oldPatients: List<Patient> = gson.fromJson(json, type)

                    patientList.addAll(oldPatients) // Merge migrated patients with CSV data

                    // Clean up the old data through the AuthManager
                    AuthManager.clearOldPatients()
                }

                if (patientList.isNotEmpty()) {
                    patientRepository.insertAll(patientList) // Insert all collected records in one batch
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /**
     * Initialises [AuthManager] with the given context to restore any saved session on app start.
     * Should be called early in the app lifecycle before checking login state.
     *
     * @param context The context used to initialise [AuthManager].
     */
    fun loadSession(context: Context) {
        AuthManager.init(context)
    }

    /**
     * Returns the total number of patient records in the database as a reactive stream.
     * The [Flow] will re-emit whenever the total count changes.
     *
     * @return A [Flow] emitting the total patient count.
     */
    fun getTotalPatientCount(): Flow<Int> {
        return patientRepository.getTotalPatientCount()
    }

    /**
     * Factory class for creating instances of [PatientViewModel].
     *
     * Required by [ViewModelProvider] to pass the [Context] dependency
     * into the ViewModel's constructor.
     *
     * @param context The context used to retrieve the application context.
     */
    class PatientViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        private val applicationContext = context.applicationContext
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PatientViewModel(applicationContext) as T

    }
}