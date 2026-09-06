package com.colleen.s36349879.medtrack.data.healthassistant

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.colleen.s36349879.medtrack.data.MedTrackDatabase
import com.colleen.s36349879.medtrack.data.doctorreview.FlaggedItem
import com.colleen.s36349879.medtrack.data.doctorreview.FlaggedItemDao
import com.colleen.s36349879.medtrack.data.doctorreview.FlaggedSourceTab
import kotlinx.coroutines.launch

sealed class HealthAssistantUiState {
    object Idle : HealthAssistantUiState()
    object Loading : HealthAssistantUiState()
    data class Success(val symptoms: String, val guidance: String) : HealthAssistantUiState()
    data class Error(val message: String) : HealthAssistantUiState()
}

class HealthAssistantViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext
    private val repository = HealthAssistantRepository(appContext)
    private val flaggedItemDao: FlaggedItemDao = MedTrackDatabase.getDatabase(appContext).flaggedItemDao()

    var uiState by mutableStateOf<HealthAssistantUiState>(HealthAssistantUiState.Idle)
        private set

    /**
     * @param recentMedications Optional context from the Medicine Log tab (nice-to-have
     *   per the spec, not required) — pass an empty list if not wiring that up yet.
     */
    fun requestGuidance(symptoms: String, vitals: String, recentMedications: List<String> = emptyList()) {
        if (symptoms.isBlank()) {
            uiState = HealthAssistantUiState.Error("Please describe your symptoms first.")
            return
        }
        viewModelScope.launch {
            uiState = HealthAssistantUiState.Loading
            val guidance = repository.getGuidance(symptoms, vitals, recentMedications)
            uiState = if (guidance != null) {
                HealthAssistantUiState.Success(symptoms, guidance)
            } else {
                HealthAssistantUiState.Error("Couldn't reach the health assistant. Please try again.")
            }
        }
    }

    fun flagCurrentResponse() {
        val current = (uiState as? HealthAssistantUiState.Success) ?: return
        viewModelScope.launch {
            flaggedItemDao.insert(
                FlaggedItem(
                    sourceTab = FlaggedSourceTab.HEALTH_ASSISTANT,
                    originalInput = current.symptoms,
                    aiOutput = current.guidance,
                    languageCode = "en",
                    flaggedAtEpochMillis = System.currentTimeMillis()
                )
            )
        }
    }

    class HealthAssistantViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HealthAssistantViewModel(context) as T
        }
    }
}
