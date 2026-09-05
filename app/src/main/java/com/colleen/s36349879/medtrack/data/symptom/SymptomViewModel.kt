package com.colleen.s36349879.medtrack.data.symptom

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.colleen.s36349879.medtrack.data.AuthManager
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

/**
 * ViewModel responsible for managing symptom data and exposing it to the UI.
 *
 * This ViewModel communicates with [SymptomRepository] for all database operations
 * and handles seeding the symptom database from CSV and JSON sources on first launch.
 * It also provides aggregated symptom statistics used by the clinician dashboard.
 *
 * @param context The application context used to initialize the repository and access assets.
 */
class SymptomViewModel(private val context: Context): ViewModel() {

    /** The repository used for all symptom database operations */
    private val symptomRepository: SymptomRepository = SymptomRepository(context)
    private val gson = Gson()

    /**
     * Retrieves all symptoms for a specific patient as a reactive stream,
     * sorted from most recent to oldest by date and time.
     *
     * @param id The ID of the patient whose symptoms should be retrieved.
     * @return A [Flow] emitting the patient's symptoms sorted by [Symptom.symptomDateTime] descending.
     */
    fun getSymptomsForPatient(id: String): Flow<List<Symptom>> {
        return symptomRepository.getSymptomsForPatient(id).map { list ->
            list.sortedByDescending { it.symptomDateTime } // Most recent symptoms appear first
        }
    }

    /**
     * Constructs a new [Symptom] record from the provided fields and inserts it
     * into the database for the currently logged-in patient.
     *
     * The patient ID is retrieved from the active session via [AuthManager].
     *
     * @param category The category or type of symptom (e.g. "Headache", "Nausea").
     * @param severity The severity of the symptom on a numeric scale.
     * @param notes Any additional notes or observations about the symptom.
     * @param date The date the symptom was recorded (e.g. "2025-06-01").
     * @param time The time the symptom was recorded (e.g. "14:30").
     */
    fun addSymptom(category: String, severity: Int, notes: String, date: String, time: String) {
        viewModelScope.launch {
            val id = AuthManager.getPatientSession() ?: ""

            // Construct the Symptom object here so the UI layer only passes raw field values
            val newSymptom = Symptom(
                patientId = id,
                category = category,
                severity = severity,
                symptomNotes = notes,
                symptomDateTime = "$date $time" // Combine date and time into a single sortable string
            )
            symptomRepository.addSymptom(newSymptom)
        }
    }

    /**
     * Seeds the symptom database from two sources the CSV asset file
     * and any migrated symptom JSON stored in [AuthManager].
     *
     * @param context The context used to open the CSV asset file.
     */
    suspend fun seedSymptom(context: Context) {
        // withContext(Dispatchers.IO) temporarily switches this code to a background thread
        // do this because these operations can be slow, and running them on the main thread
        // would freeze the UI
        // Once the code inside finishes, it automatically switches back to the main thread
        withContext(Dispatchers.IO) {
            try {
                val symptomList = mutableListOf<Symptom>()

                //CSV
                context.assets.open("symptoms.csv").use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream))
                    reader.readLine()
                    reader.forEachLine { line ->
                        val cleanLine = line.replace("\uFEFF", "")
                        if (cleanLine.isNotBlank()) {
                            val row = splitCsvLine(cleanLine)
                            if (row.size >= 4) {
                                symptomList.add(
                                    Symptom(
                                        patientId = row[0],
                                        category = row[1],
                                        severity = row[2].toIntOrNull()?: 0,
                                        symptomNotes = row[3],
                                        symptomDateTime = row[4]
                                    )
                                )
                            }
                        }
                    }
                }

                //JSON Migration
                AuthManager.getOldSymptomsJson()?.let { json ->
                    val type = object : TypeToken<List<Symptom>>() {}.type
                    val oldSymptoms: List<Symptom> = gson.fromJson(json, type)
                    symptomList.addAll(oldSymptoms) // Merge migrated symptoms with CSV data
                    AuthManager.clearOldSymptoms() // Clear migration data after successful import
                }

                if (symptomList.isNotEmpty()) {
                    symptomRepository.insertAll(symptomList) // Insert all collected records in one batch
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Returns the most frequently recorded symptom category across all patients
     * as a reactive stream.
     *
     * Defaults to "None" if the database is empty or no category data is available.
     *
     * @return A [Flow] emitting the most common symptom category string.
     */
    fun getMostCommonCategory(): Flow<String> {
        // Use .map to replace a null result with "None" when the database is empty
        return symptomRepository.getMostCommonCategory().map { it ?: "None" }
    }

    /**
     * Returns the average severity score across all recorded symptoms as a reactive stream.
     *
     * Defaults to 0.0 if the database is empty or no severity data is available.
     *
     * @return A [Flow] emitting the average symptom severity as a [Double].
     */
    fun getAverageSeverity(): Flow<Double> {
        return symptomRepository.getAverageSeverity().map { it ?: 0.0 }
    }

    /**
     * Returns the severity scores of the most recently recorded symptoms as a reactive stream.
     * Used to populate the severity trend chart in the clinician dashboard.
     *
     * @return A [Flow] emitting a list of recent symptom severity values as integers.
     */
    fun getRecentSeverities(): Flow<List<Int>>{
        return symptomRepository.gerRecentSeverities()
    }

    /**
     * Factory class for creating instances of [SymptomViewModel].
     *
     * Required by [ViewModelProvider] to pass the [Context] dependency
     * into the ViewModel's constructor.
     *
     * @param context The context used to retrieve the application context.
     */
    class SymptomViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        private val applicationContext = context.applicationContext
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SymptomViewModel(applicationContext) as T
    }
}

