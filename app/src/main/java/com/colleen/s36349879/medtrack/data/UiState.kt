package com.colleen.s36349879.medtrack.data

import com.colleen.s36349879.medtrack.data.network.DrugResult

/**
 * Represents the possible UI states for an FDA drug information search.
 *
 * This sealed class ensures that the UI can only ever be in one of four
 * defined states at a time.
 * Used by [MedicationViewModel] to drive the drug search screen.
 */
sealed class DrugInfoUiState {

    object Idle : DrugInfoUiState()


    object Loading : DrugInfoUiState()

    /**
     * The search completed successfully and returned a result.
     * The UI should display the drug details in this state.
     *
     * @property data is The [DrugResult] returned by the FDA API.
     */
    data class Success(val data: DrugResult) : DrugInfoUiState()

    /**
     * The search failed or returned no usable result.
     * The UI should display the error message to the user in this state.
     *
     * @property message A description of what went wrong.
     */
    data class Error(val message: String) : DrugInfoUiState()
}

/**
 * Represents the possible UI states for an AI-generated medication tip request.
 *
 * This sealed interface ensures that the UI can only ever be in one of four
 * defined states at a time.
 * Used by [GenAIViewModel] to drive the AI coach feature.
 */
sealed class GenAiUiState {
    object Idle : GenAiUiState()
    object Loading : GenAiUiState()

    /**
     * The AI request completed successfully and returned a tip.
     * The UI should display the generated tip text in this state.
     *
     * @property tip The AI-generated medication safety tip string.
     */
    data class Success(val tip: String) : GenAiUiState()

    /**
     * The AI request failed or returned no usable content.
     * The UI should display the error message to the user in this state.
     *
     * @property message A description of what went wrong.
     */
    data class Error(val message: String) : GenAiUiState()
}