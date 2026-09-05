package com.colleen.s36349879.medtrack.data.genAI

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.colleen.s36349879.medtrack.BuildConfig
import com.colleen.s36349879.medtrack.data.GenAiUiState
import com.colleen.s36349879.medtrack.data.medication.MedicationViewModel
import com.colleen.s36349879.medtrack.data.symptom.SymptomRepository
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing AI-generated content using the Gemini model.
 *
 * This ViewModel communicates with [GenAIRepository] to fetch medication safety tips
 * and analyze clinical patterns based on patient data.
 *
 * @param context The application context used to initialize the repository.
 */
class GenAIViewModel(context: Context): ViewModel() {

    /** The repository used to send prompts to the Gemini AI and retrieve responses. */
    private val genAiRepository: GenAIRepository = GenAIRepository(context)

    /**
     * The current UI state of the AI interaction.
     * Can be [GenAiUiState.Idle], [GenAiUiState.Loading], [GenAiUiState.Success],
     * or [GenAiUiState.Error].
     */
    private var state by mutableStateOf<GenAiUiState>(GenAiUiState.Idle) // Start in Idle until a request is made


    /** True when the AI is currently processing a request. */
    val isLoading: Boolean get() = state is GenAiUiState.Loading


    /** The AI-generated tip text, or null if no successful response has been received. */
    val aiTip: String? get() = (state as? GenAiUiState.Success)?.tip


    /** An error message describing the most recent failure, or null if there is no error */
    val errorMessage: String? get() = (state as? GenAiUiState.Error)?.message


    /**
     * A list of AI-generated clinical insights produced by [findPatterns].
     * Updated each time a pattern analysis completes successfully.
     */
    var aiInsights by mutableStateOf<List<String>>(emptyList())


    /**
     * Generates a personalized medication safety tip based on the patient's
     * current medications and recently reported symptoms.
     *
     * Constructs a prompt for the Gemini AI and updates [state] based on the result.
     * On success, [onSuccess] is invoked with the generated tip text.
     *
     * @param medications A list of the patient's current medication names.
     * @param symptoms A list of the patient's recently reported symptom categories.
     * @param onSuccess A callback invoked with the tip text when generation succeeds.
     */
    fun generateMedicationTip(
        medications: List<String>,
        symptoms: List<String>,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            state = GenAiUiState.Loading // Show loading indicator while waiting for AI response

            try {

                val prompt = """
                You are a professional medical safety coach. 
                CONTEXT: 
                Medications: ${medications.ifEmpty { listOf("None") }.joinToString(", ")}
                Recent Symptoms: ${symptoms.ifEmpty { listOf("None reported") }.joinToString(", ")}

                TASK: Provide one safety tip (under 30 words) based on the specific combination of their medications and symptoms. 
                If they have no medications or symptoms, provide a general wellness tip. 
                Be professional and encouraging.
                """.trimIndent() // format multiline strings by removing common leading whitespace from every line.

                val tipText = genAiRepository.getAiResponse(prompt)

                if (!tipText.isNullOrBlank()) {
                    state = GenAiUiState.Success(tipText) // Update state with the generated tip
                    onSuccess(tipText) // Notify the caller so the UI can react
                } else {
                    state = GenAiUiState.Error("AI returned an empty tip. Please try again.")
                }
            } catch (e: Exception) {
                //Handle errors
                state = GenAiUiState.Error("Failed to reach AI Coach. Check your connection.")
            }
        }
    }

    /**
     * Analyses aggregated patient data to identify clinical patterns and observations.
     *
     * Sends a structured summary to the Gemini AI and parses the response into
     * exactly 3 insight sentences, stored in [aiInsights].
     *
     * @param totalPatients The total number of patients included in the data summary.
     * @param avgMeds The average number of medications per patient.
     * @param commonSymptom The most frequently reported symptom category across patients.
     * @param avgSeverity The average symptom severity score out of 10.
     */
    fun findPatterns(
        totalPatients: Int,
        avgMeds: Double,
        commonSymptom: String,
        avgSeverity: Double
    ) {
        viewModelScope.launch {
            state = GenAiUiState.Loading

            try {
                val prompt = """
                You are a senior clinical data analyst. 
                DATA SUMMARY:
                Total Patients: $totalPatients
                Average Medications per Patient: ${String.format("%.2f", avgMeds)}
                Most Common Symptom Category: $commonSymptom
                Average Symptom Severity: ${String.format("%.1f", avgSeverity)}/10

                TASK: Based on this data, provide exactly 3 interesting clinical patterns or observations. 
                Format your response as a simple list with each insight on a new line. 
                Do not use bullet points or numbers in the text, just the sentences.
                Keep each insight under 25 words.
                """.trimIndent()

                val responseText = genAiRepository.getAiResponse(prompt)

                if (!responseText.isNullOrBlank()) {
                    // Split the response by new lines and take the first 3
                    aiInsights = responseText.lines()
                        .filter { it.isNotBlank() }
                        .take(3)

                    state = GenAiUiState.Success("Insights generated") // Mark as successful once insights are gotten
                } else {
                    state = GenAiUiState.Error("AI returned no patterns.")
                }
            } catch (e: Exception) {
                state = GenAiUiState.Error("Failed to analyze data: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Factory class for creating instances of [GenAIViewModel].
     *
     * Required by [ViewModelProvider] to pass the [Context] dependency
     * into the ViewModel's constructor.
     *
     * @param context The context used to retrieve the application context.
     */
    class GenAiViewModelFactory(context: Context) : ViewModelProvider.Factory {
        private val context = context.applicationContext
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GenAIViewModel(context) as T

    }

}