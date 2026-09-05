package com.colleen.s36349879.medtrack.data.clinician

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * This view model is for managing clinician authentication using a
 * secret access key
 *
 * This view model has the UI state for the clinician password screen
 * and provide functions to validate the access key and manage the
 * authentication session
 *
 * @param context is the application context
 */
class ClinicianPasswordViewModel(context: Context): ViewModel() {

    /**
     * The correct access key for clinician authentication
     * this is compared against the user's input during validation
     */
    private val accessKey = "dollar-entry-apples"

    /** the current value of the access key input field */
    var accessKeyInput by mutableStateOf("")

    /**
     * an error message to display when authentication fails
     * null when there is no error
     */
    var errorMessage by mutableStateOf<String?>(null)

    /**
     * indicates whether the clinician has been successfully authenticated
     */
    var isAuthenticated by mutableStateOf(false)

    /**
     * updates the access key input field and clears any existing error message
     *
     * @param newValue is the new value entered by the user
     */
    fun onAccessKeyChange(newValue: String){
        accessKeyInput = newValue
        errorMessage = null
    }

    /**
     * validates the entered access key against the correct key
     *
     * if the input matches, [isAuthenticated] is set to true and [onSuccess]
     * is invoked. Otherwise, an error message is set and authentication fails
     *
     * @param onSuccess is a callback invoked when the access key is correct
     */
    fun validateAccessKey(onSuccess: () -> Unit){
        if (accessKeyInput == accessKey){
            isAuthenticated = true
            errorMessage = null
            onSuccess()
        } else {
            isAuthenticated = false
            errorMessage = "Invalid Access Key"
        }
    }

    /**
     * logs out the clinician by resetting authentication state
     * and clearing the access key input
     */
    fun logout(){
        isAuthenticated = false
        accessKeyInput = ""
    }

    /**
     * factory class for creating instance of [ClinicianPasswordViewModel]
     *
     * required by [ViewModelProvider] to pass the [Context] dependency
     * into a view model's constructor
     *
     * @param context is the context used to retrieve the application context
     */
    class ClinicianPasswordViewModelFactory(context: Context) : ViewModelProvider.Factory {
        private val context = context.applicationContext
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ClinicianPasswordViewModel(context) as T

    }
}