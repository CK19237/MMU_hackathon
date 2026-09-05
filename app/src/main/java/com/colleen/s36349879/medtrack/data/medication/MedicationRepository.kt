package com.colleen.s36349879.medtrack.data.medication

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.colleen.s36349879.medtrack.data.MedTrackDatabase
import com.colleen.s36349879.medtrack.data.network.APIService
import com.colleen.s36349879.medtrack.data.network.DrugResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsible for managing medication data from both
 * the local Room database and the FDA API.
 *
 * @param context The application context used to access the database and system services.
 */
class MedicationRepository (private val context: Context){

    /** The DAO used to perform local database operations on the medication_table. */
    private val medicationDao = MedTrackDatabase.getDatabase(context).medicationDao()


    /** The API service used to query the remote FDA drug information endpoint. */
    private val apiService = APIService.create()


    /**
     * Searches the FDA API for drug information matching the given drug name.
     *
     * Queries the API using the brand name and returns the first matching result.
     * Returns null if the response is unsuccessful, the result list is empty,
     * or a network error occurs.
     *
     * @param drugName The brand name of the drug to search for.
     * @return The first matching [DrugResult], or null if none was found or an error occurred.
     */
    suspend fun searchDrugInfo(drugName: String): DrugResult? {
        return try {
            // Format the query to search by drug name
            val query = "openfda.brand_name:\"$drugName\""
            val response = apiService.getDrugInfo(query)

            if (response.isSuccessful) {
                // Return the first result if it exists, null if empty
                response.body()?.results?.firstOrNull()
            } else {
                null // Will trigger error state in ViewModel
            }
        } catch (e: Exception) {
            // Handles network failures (no internet, timeout)
            null
        }
    }

    /**
     * Checks whether the device currently has an active network connection.
     *
     * Detects connectivity across Wi-Fi, cellular, and ethernet transports.
     * Used to guard API calls before they are attempted.
     *
     * @return True if the device has an active and capable network connection, false otherwise.
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        // Return false early if there is no active network
        val network = connectivityManager.activeNetwork ?: return false

        // Return false early if network capabilities cannot be determined
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        // Return true if any supported transport type is available
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    /**
     * Inserts a list of medications into the local database in a single operation.
     *
     * @param medications The list of [Medication] records to insert.
     */
    suspend fun insertAll(medications: List<Medication>){medicationDao.insertAll(medications)}

    /**
     * Retrieves all medications for a specific patient as a reactive stream.
     * The [Flow] will re-emit whenever the patient's medication data changes.
     *
     * @param pid The ID of the patient whose medications should be retrieved.
     * @return A [Flow] emitting the list of [Medication] records for the given patient.
     */
    fun getMedicationsForPatient(pid: String): Flow<List<Medication>> {
        return medicationDao.getMedicationsForPatient(pid)
    }

    /**
     * Updates the taken status and last taken date for a specific medication record.
     *
     * @param medId The ID of the medication to update.
     * @param isTaken True if the medication has been taken, false otherwise.
     * @param date The date the medication was taken, formatted as a string.
     */
    suspend fun updateTakenStatus(medId: Int, isTaken: Boolean, date: String) {
        medicationDao.updateTakenStatus(medId, isTaken, date)
    }

    /**
     * Inserts a single medication record into the local database.
     *
     * @param med The [Medication] record to add.
     */
    suspend fun addMedication(med: Medication) =
        medicationDao.insertMedication(med)

    /**
     * Returns the total number of medication records across all patients as a reactive stream.
     * The [Flow] will re-emit whenever the total count changes.
     *
     * @return A [Flow] emitting the total count of medications in the database.
     */
    fun getTotalMedicationCount(): Flow<Int> = medicationDao.getTotalMedicationCount()
}