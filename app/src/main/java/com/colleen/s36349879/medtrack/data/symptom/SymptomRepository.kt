package com.colleen.s36349879.medtrack.data.symptom

import android.content.Context
import com.colleen.s36349879.medtrack.data.MedTrackDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsible for managing symptom data from the local Room database.
 *
 * @param context The application context used to access the database.
 */
class SymptomRepository (context: Context){

    /**
     * The DAO used to perform local database operations on the symptom table.
     */
    private val symptomDao = MedTrackDatabase.getDatabase(context).symptomDao()

    /**
     * Inserts a list of symptoms into the database in a single operation.
     * Used during CSV and JSON seeding on first launch.
     *
     * @param symptoms The list of [Symptom] records to insert.
     */
    suspend fun insertAll(symptoms: List<Symptom>) {symptomDao.insertAll(symptoms)}

    /**
     * Retrieves all symptoms for a specific patient as a reactive stream.
     * The [Flow] will re-emit whenever that patient's symptom data changes.
     *
     * @param pid The ID of the patient whose symptoms should be retrieved.
     * @return A [Flow] emitting the list of [Symptom] records for the given patient.
     */
    fun getSymptomsForPatient(pid: String): Flow<List<Symptom>> {
        return symptomDao.getSymptomsForPatient(pid)
    }

    /**
     * Inserts a single symptom record into the database.
     * Used when a patient logs a new symptom through the app.
     *
     * @param symptom The [Symptom] record to insert.
     */
    suspend fun addSymptom(symptom: Symptom) {
        symptomDao.insertSymptom(symptom)
    }

    /**
     * Returns the most frequently recorded symptom category across all patients
     * as a reactive stream.
     *
     * Emits null if the database is empty, which the ViewModel maps to "None".
     *
     * @return A [Flow] emitting the most common symptom category string, or null if unavailable.
     */
    fun getMostCommonCategory(): Flow<String?> = symptomDao.getMostCommonCategory()

    /**
     * Returns the average severity score across all recorded symptoms as a reactive stream.
     *
     * Emits null if the database is empty, which the ViewModel maps to 0.0.
     *
     * @return A [Flow] emitting the average severity as a [Double], or null if unavailable.
     */
    fun getAverageSeverity(): Flow<Double?> = symptomDao.getAverageSeverity()

    /**
     * Returns the severity scores of the most recently recorded symptoms as a reactive stream.
     * Used to populate the severity trend chart in the clinician dashboard.
     *
     * @return A [Flow] emitting a list of recent symptom severity values as integers.
     */
    fun gerRecentSeverities(): Flow<List<Int>> = symptomDao.getRecentSeverities()
}