package com.colleen.s36349879.medtrack.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Singleton object responsible for managing patient session state and
 * legacy data migration using [SharedPreferences].
 *
 * This object serves two purposes:
 * 1. Session management: saving, retrieving, and clearing the logged-in
 *    patient's ID so the app can restore authentication state across launches.
 * 2. Data migration: retrieving and clearing patient, medication, and symptom
 *    data stored as JSON strings from a previous version of the app.
 *
 * [init] must be called before any other function, as all operations depend
 * on the [SharedPreferences] instance being initialized first.
 */
object AuthManager {

    /** The name of the [SharedPreferences] file used by this manager. */
    private const val PREFS_NAME = "app_sp"

    /** The key used to store and retrieve the logged-in patient's ID. */
    private const val KEY_PATIENT_ID = "logged_in_patient_id"

    /**
     * The [SharedPreferences] instance used for all read and write operations.
     * Initialised by [init] and must be set before any other function is called.
     */
    private lateinit var prefs: SharedPreferences

    /**
     * Initialises [AuthManager] with the application context.
     * Must be called once on app start in MainActivity (Welcome Screen) before any
     * session or migration functions are used.
     *
     * @param context The context used to access the [SharedPreferences] file.
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Saves the logged-in patient's ID to [SharedPreferences].
     * Called after a successful login or registration to persist the session.
     *
     * @param pid The ID of the patient to save as the active session.
     */
    fun savePatientSession(pid:String) {
        prefs.edit().putString("logged_in_patient_id", pid).apply()
    }

    /**
     * Retrieves the currently logged-in patient's ID from [SharedPreferences].
     *
     * @return The stored patient ID string, or null if no session exists.
     */
    fun getPatientSession(): String?{
        return prefs.getString(KEY_PATIENT_ID, null)
    }

    /**
     * Removes the logged-in patient's ID from [SharedPreferences], effectively logging them out.
     * After this call, [isLoggedIn] will return false.
     */
    fun clearSession() {
        prefs.edit().remove(KEY_PATIENT_ID).apply()
    }

    /**
     * Checks whether a patient is currently logged in by verifying
     * that a session ID is stored in [SharedPreferences].
     *
     * @return True if a patient session exists, false otherwise.
     */
    fun isLoggedIn(): Boolean {
        return getPatientSession() != null
    }

    /**
     * Retrieves the old patient data stored as a JSON string from a previous app version.
     *
     * @return A JSON string of patient records, or null if no migration data exists.
     */
    fun getOldPatientsJson(): String? {
        return prefs.getString("patients", null)
    }

    /**
     * Removes the old patient JSON from [SharedPreferences] after successful migration.
     */
    fun clearOldPatients() {
        prefs.edit().remove("patients").apply()
    }

    /**
     * Retrieves the old medication data stored as a JSON string from a previous app version.
     *
     * @return A JSON string of medication records, or null if no migration data exists.
     */
    fun getOldMedsJson(): String? = prefs.getString("medications", null)

    /**
     * Removes the legacy medication JSON from [SharedPreferences] after successful migration.
     */
    fun clearOldMeds() = prefs.edit().remove("medications").apply()

    /**
     * Retrieves the old symptom data stored as a JSON string from a previous app version.
     *
     * @return A JSON string of symptom records, or null if no migration data exists.
     */
    fun getOldSymptomsJson(): String? = prefs.getString("symptoms", null)

    /**
     * Removes the legacy symptom JSON from [SharedPreferences] after successful migration.
     */
    fun clearOldSymptoms() = prefs.edit().remove("symptoms").apply()
}