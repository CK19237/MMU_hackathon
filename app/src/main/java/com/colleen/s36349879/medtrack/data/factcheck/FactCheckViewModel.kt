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
    private val govEvidenceDao: GovEvidenceDao = MedTrackDatabase.getDatabase(appContext).govEvidenceDao()
    private val verifiedClaimCacheDao: VerifiedClaimCacheDao = MedTrackDatabase.getDatabase(appContext).verifiedClaimCacheDao()
    private val repository = FactCheckRepository(appContext, mythCacheDao, govEvidenceDao, verifiedClaimCacheDao)

    var uiState by mutableStateOf<FactCheckUiState>(FactCheckUiState.Idle)
        private set

    var selectedLanguage by mutableStateOf(FactCheckLanguage.DEFAULT)

    /**
     * Ensures the myth cache and the curated gov evidence table both have
     * their seed data. Call once, e.g. from MainActivity's existing seeding
     * block. Kept as one function (same name/signature as before) so the
     * existing call site in MainActivity doesn't need to change.
     */
    fun seedMythCacheIfNeeded() {
        viewModelScope.launch {
            MythSeedData.seedIfEmpty(mythCacheDao)
            GovEvidenceSeedData.seedIfEmpty(govEvidenceDao)
        }
    }

    /**
     * Verifies [claim]. Flow (per the required database-first pipeline):
     * 1. Hand-curated myth cache (instant, offline-capable — unchanged).
     * 2. Dynamic verified-claim cache — claims this app already fact-checked live before.
     * 3. If no cache match: retrieve curated Malaysian government evidence, then call
     *    the LLM with that evidence (or the original general web-grounded fallback if
     *    no official evidence was found for this claim).
     * 4. Save the new live result into the verified-claim cache so an equivalent future
     *    claim is instant next time.
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
                    ?: repository.checkVerifiedClaimCache(trimmed, selectedLanguage)
                if (cached != null) {
                    uiState = FactCheckUiState.Success(cached)
                    return@launch
                }

                val evidence = repository.retrieveEvidence(trimmed)
                val live = repository.checkClaimLive(trimmed, selectedLanguage, evidence.takeIf { !it.isEmpty })
                repository.saveToVerifiedClaimCache(
                    live,
                    evidenceUsed = evidence.matches.takeIf { !evidence.isEmpty }
                        ?.joinToString("; ") { it.evidence.evidenceText }
                )
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
