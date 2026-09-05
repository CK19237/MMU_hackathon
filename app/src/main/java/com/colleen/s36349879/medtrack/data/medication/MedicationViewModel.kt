package com.colleen.s36349879.medtrack.data.medication

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.colleen.s36349879.medtrack.data.AuthManager
import com.colleen.s36349879.medtrack.data.DrugInfoUiState
import com.colleen.s36349879.medtrack.data.network.DrugResult
import com.colleen.s36349879.medtrack.data.splitCsvLine
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * ViewModel responsible for managing medication data and FDA drug information lookups.
 *
 * This ViewModel communicates with [MedicationRepository] to handle local database
 * operations and remote API searches. It also manages seeding the database from
 * CSV and JSON sources on first launch.
 *
 * @param context The application context used to initialize the repository and access assets.
 */
class MedicationViewModel(private val context: Context): ViewModel() {

    /** The repository used for all medication database operations and API calls. */
    private val medicationRepository: MedicationRepository = MedicationRepository(context)


    /** Gson instance used to deserialize migrated medication JSON data. */
    private val gson = Gson()


    /** True when an FDA drug search is currently in progress. */
    val isSearching: Boolean get() = drugUiState is DrugInfoUiState.Loading


    /** The drug details returned from a successful FDA search, or null if unavailable. */
    val drugDetails: DrugResult? get() = (drugUiState as? DrugInfoUiState.Success)?.data


    /** An error message from a failed drug search, or null if there is no error. */
    val apiError: String? get() = (drugUiState as? DrugInfoUiState.Error)?.message


    /**
     * The current UI state of the FDA drug information search.
     * Can be [DrugInfoUiState.Idle], [DrugInfoUiState.Loading],
     * [DrugInfoUiState.Success], or [DrugInfoUiState.Error].
     * Private set ensures only this ViewModel can update the state.
     */
    var drugUiState by mutableStateOf<DrugInfoUiState>(DrugInfoUiState.Idle)
        private set

    /**
     * Searches the FDA API for drug information matching the given drug name.
     *
     * Validates the input and checks network availability before making the call.
     * Updates [drugUiState] based on whether the search succeeds or fails.
     *
     * @param drugName The brand name of the drug to search for.
     */
    fun fetchDrugDetails(drugName: String) {
        if (drugName.isBlank()) return

        // Check network before making the call
        if (!medicationRepository.isNetworkAvailable()) {
            drugUiState = DrugInfoUiState.Error("No internet connection. Please check your network.")
            return
        }

        viewModelScope.launch {
            drugUiState = DrugInfoUiState.Loading // Show loading indicator while the API call is in progress
            val result = medicationRepository.searchDrugInfo(drugName)

            // Update state based on whether a result was returned
            drugUiState = if (result != null) {
                DrugInfoUiState.Success(result)
            } else {
                DrugInfoUiState.Error("Drug '$drugName' not found in FDA database.")
            }
        }
    }

    /**
     * Returns a [Flow] of distinct medication names for a specific patient.
     * Used to provide medication context to the AI tip generator.
     *
     * @param id The ID of the patient whose medication names should be retrieved.
     * @return A [Flow] emitting a deduplicated list of medication name strings.
     */
    fun getMyMedicationNames(id: String): Flow<List<String>> {
        return medicationRepository.getMedicationsForPatient(id).map { list ->
            // Inner .map transforms each Medication object into just its name string
            list.map { it.medicationName }.distinct() // Remove duplicates to avoid redundant AI context
        }
    }

    /**
     * Retrieves all medications for a specific patient and automatically resets
     * any medications that were marked as taken on a previous day.
     *
     * This ensures the taken status reflects the current day only,
     * resetting stale records in the database as they are encountered.
     *
     * @param patientId The ID of the patient whose medications should be retrieved.
     * @return A [Flow] emitting the current list of [Medication] records for the patient.
     */
    fun getMedicationsForPatient(patientId: String): Flow<List<Medication>> {
        return medicationRepository.getMedicationsForPatient(patientId).map { list ->
            val today = getTodayDate()

            // Check each medication to see if its taken status is from a previous day
            list.forEach { med ->
                if (med.lastTakenDate != today && med.isTaken) {
                    // Reset taken status for medications marked on a previous day
                    viewModelScope.launch {
                        medicationRepository.updateTakenStatus(med.id, false, today)
                    }
                }
            }
            list // Return the list
        }
    }

    /**
     * Toggles the taken status of a medication when the patient checks or unchecks it.
     * Records today's date alongside the status update.
     *
     * @param medication The [Medication] record to update.
     * @param isChecked True if the medication has been taken, false if unchecked.
     */
    fun toggleMedicationTaken(medication: Medication, isChecked: Boolean) {
        viewModelScope.launch {
            medicationRepository.updateTakenStatus(medication.id, isChecked, getTodayDate())
        }
    }

    /**
     * Constructs a new [Medication] record from the provided fields and inserts
     * it into the local database.
     *
     * @param patientId The ID of the patient this medication belongs to.
     * @param name The name of the medication.
     * @param dosage The prescribed dosage
     * @param frequency How often the medication should be taken
     * @param time The scheduled time for the medication
     * @param type The form of the medication
     * @param notes Any additional instructions or notes for the medication.
     */
    fun addMedication(
        patientId: String,
        name: String,
        dosage: String,
        frequency: String,
        time: String,
        type: String,
        notes: String
    ) {
        viewModelScope.launch {

            //The ViewModel constructs the Medication object
            val newMed = Medication(
                patientId = patientId,
                medicationName = name,
                dosage = dosage,
                frequency = frequency,
                medicationTime = time,
                medicationType = type,
                medicationNotes = notes
            )

            //The ViewModel sends it to the repository
            medicationRepository.addMedication(newMed)
        }
    }

    /**
     * Seeds the medication database from the CSV asset file
     * and any migrated medication JSON stored in [AuthManager].
     *
     * @param context The context used to open the CSV asset file.
     */
    suspend fun seedMedication(context: Context) {

        // withContext(Dispatchers.IO) temporarily switches this code to a background thread
        // do this because these operations can be slow, and running them on the main thread
        // would freeze the UI
        // Once the code inside finishes, it automatically switches back to the main thread
        withContext(Dispatchers.IO) {
            try {
                val allMedsToSeed = mutableListOf<Medication>()

                // Process CSV
                context.assets.open("medications.csv").use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream))
                    reader.readLine() // Skip header
                    reader.forEachLine { line ->
                        val cleanLine = line.replace("\uFEFF", "")
                        if (cleanLine.isNotBlank()) {
                            val row = splitCsvLine(cleanLine)

                            if (row.size >= 6) {
                                allMedsToSeed.add(
                                    Medication(
                                        patientId = row[0],
                                        medicationName = row[1],
                                        dosage = row[2],
                                        frequency = row[3],
                                        medicationTime = row[4],
                                        medicationType = row[5],
                                        medicationNotes = row.getOrNull(6) ?: ""
                                    )
                                )
                            }
                        }
                    }
                }

                // Process JSON Migration
                AuthManager.getOldMedsJson()?.let { medsJson ->
                    val type = object : TypeToken<List<Medication>>() {}.type
                    val oldMeds: List<Medication> = gson.fromJson(medsJson, type)
                    allMedsToSeed.addAll(oldMeds) // Merge migrated medications with CSV data
                    AuthManager.clearOldMeds() // Clear migration data after successful import
                }

                if (allMedsToSeed.isNotEmpty()) {
                    medicationRepository.insertAll(allMedsToSeed) // Insert all collected records in one batch
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Returns today's date formatted as "yyyy-MM-dd".
     * Used to record and compare medication taken dates.
     *
     * @return A string representing today's date (e.g. "2025-06-01").
     */
    fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    /**
     * Returns the total number of medication records across all patients as a reactive stream.
     * The [Flow] will re-emit whenever the total count changes.
     *
     * @return A [Flow] emitting the total medication count from the database.
     */
    fun getTotalMedicationCount(): Flow<Int> {
        return medicationRepository.getTotalMedicationCount()
    }

    /**
     * Factory class for creating instances of [MedicationViewModel].
     *
     * Required by [ViewModelProvider] to pass the [Context] dependency
     * into the ViewModel's constructor.
     *
     * @param context The context used to retrieve the application context.
     */
    class MedicationViewModelFactory(context: Context) : ViewModelProvider.Factory {
        private val context = context.applicationContext
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MedicationViewModel(context) as T

    }


}