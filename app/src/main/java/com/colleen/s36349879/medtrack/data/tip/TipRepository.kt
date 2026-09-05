package com.colleen.s36349879.medtrack.data.tip

import android.content.Context
import com.colleen.s36349879.medtrack.data.MedTrackDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsible for managing AI-generated tip data from the local Room database.
 *
 * All database operations are delegated to [tipDao].
 *
 * @param context The application context used to access the database.
 */
class TipRepository(context: Context) {

    /**The DAO used to perform local database operations on the tip table.*/
    private val tipDao = MedTrackDatabase.getDatabase(context).tipDao()

    /**
     * Inserts a single AI-generated tip record into the database.
     *
     * @param tip The [MedCoachTip] record to save.
     */
    suspend fun saveTip(tip: MedCoachTip) {
        tipDao.insertTip(tip)
    }

    /**
     * Retrieves the full tip history for a specific patient as a reactive stream.
     * The [Flow] will re-emit whenever the patient's tip history changes.
     *
     * @param patientId The ID of the patient whose tip history should be retrieved.
     * @return A [Flow] emitting the list of [MedCoachTip] records for the given patient.
     */
    fun getTipHistory(patientId: String): Flow<List<MedCoachTip>> {
        return tipDao.getTipsForPatient(patientId)
    }
}