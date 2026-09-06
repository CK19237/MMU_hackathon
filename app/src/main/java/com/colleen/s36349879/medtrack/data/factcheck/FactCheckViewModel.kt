package com.colleen.s36349879.medtrack.data.factcheck

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

/** UI state for the Fact-Check tab. Mirrors the sealed-state pattern already used by [com.colleen.s36349879.medtrack.data.GenAiUiState]. */
sealed class FactCheckUiState {
    object Idle : FactCheckUiState()
    object Loading : FactCheckUiState()
    data class Success(val result: FactCheckResult) : FactCheckUiState()
    data class Error(val message: String) : FactCheckUiState()
}

/**
 * ViewModel backing the Fact-Check tab.
 *
 * Flow: check the local myth cache first (instant, offline-capable) → only if
 * there's no confident cache match, call the live grounded Gemini endpoint.
 * This satisfies the "local cache fallback" hard requirement and keeps the
 * common-myth case fast even on a slow connection.
 */
class FactCheckViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext
    private val mythCacheDao: MythCacheDao = MedTrackDatabase.getDatabase(appContext).mythCacheDao()
    private val flaggedItemDao: FlaggedItemDao = MedTrackDatabase.getDatabase(appContext).flaggedItemDao()
    private val repository = FactCheckRepository(appContext, mythCacheDao)

    var uiState by mutableStateOf<FactCheckUiState>(FactCheckUiState.Idle)
        private set

    var selectedLanguage by mutableStateOf(FactCheckLanguage.DEFAULT)

    /** Ensures the myth cache has its seed data. Call once, e.g. from MainActivity's existing seeding block. */
    fun seedMythCacheIfNeeded() {
        viewModelScope.launch {
            MythSeedData.seedIfEmpty(mythCacheDao)
        }
    }

    /**
     * Verifies [claim]: checks the local cache first, then falls back to the
     * live grounded API call if no confident cache match is found.
     */
    fun verifyClaim(claim: String) {
        val trimmed = claim.trim()
        if (trimmed.isEmpty()) {
            uiState = FactCheckUiState.Error("Please enter a claim to check.")
            return
        }

        viewModelScope.launch {
            uiState = FactCheckUiState.Loading
            try {
                val cached = repository.checkLocalCache(trimmed, selectedLanguage)
                if (cached != null) {
                    uiState = FactCheckUiState.Success(cached)
                    return@launch
                }

                val live = repository.checkClaimLive(trimmed, selectedLanguage)
                uiState = FactCheckUiState.Success(live)
            } catch (e: Exception) {
                uiState = FactCheckUiState.Error(
                    "Couldn't reach the verification service. Please check your connection and try again."
                )
            }
        }
    }

    /**
     * Sends the current result to the async Doctor Review queue. Does not
     * block or alter [uiState] — the verdict already shown to the user stands;
     * this only queues it for a clinician to later confirm or correct.
     */
    fun flagCurrentResult() {
        val current = (uiState as? FactCheckUiState.Success)?.result ?: return
        viewModelScope.launch {
            flaggedItemDao.insert(
                FlaggedItem(
                    sourceTab = FlaggedSourceTab.FACT_CHECK,
                    originalInput = current.claim,
                    aiOutput = "${current.verdict}: ${current.explanation}",
                    languageCode = current.languageCode,
                    flaggedAtEpochMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun reset() {
        uiState = FactCheckUiState.Idle
    }

    class FactCheckViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FactCheckViewModel(context) as T
        }
    }
}
