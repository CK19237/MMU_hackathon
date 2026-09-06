package com.colleen.s36349879.medtrack.data.factcheck

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.colleen.s36349879.medtrack.data.MedTrackDatabase
import com.colleen.s36349879.medtrack.data.patient.PatientDao
import com.colleen.s36349879.medtrack.data.medication.MedicationDao
import com.colleen.s36349879.medtrack.data.doctorreview.FlaggedItem
import com.colleen.s36349879.medtrack.data.doctorreview.FlaggedItemDao
import com.colleen.s36349879.medtrack.data.doctorreview.FlaggedSourceTab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** UI state for the Fact-Check tab. Mirrors the sealed-state pattern already used by [com.colleen.s36349879.medtrack.data.GenAiUiState]. */
sealed class FactCheckUiState {
    object Idle : FactCheckUiState()
    object Loading : FactCheckUiState()
    data class Success(
        val result: FactCheckResult,
        val safetyWarnings: List<MedicationSafetyWarning> = emptyList(),
        val medicationInteractionCheckAvailable: Boolean = false,
        val hasRecordedMedications: Boolean = false
    ) : FactCheckUiState()
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
enum class MedicationSafetyWarning {
    ALLERGY
}

/**
 * The app currently has no verified pairwise drug-interaction source. We therefore
 * deliberately do not infer interactions from medicine names or from the LLM.
 * This value documents that limitation so a future verified interaction provider
 * can be plugged into this ViewModel without changing the UI contract.
 */
private const val MEDICATION_INTERACTION_CHECK_AVAILABLE = false

class FactCheckViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext
    private val mythCacheDao: MythCacheDao = MedTrackDatabase.getDatabase(appContext).mythCacheDao()
    private val flaggedItemDao: FlaggedItemDao = MedTrackDatabase.getDatabase(appContext).flaggedItemDao()
    private val govEvidenceDao: GovEvidenceDao = MedTrackDatabase.getDatabase(appContext).govEvidenceDao()
    private val verifiedClaimCacheDao: VerifiedClaimCacheDao = MedTrackDatabase.getDatabase(appContext).verifiedClaimCacheDao()
    private val historyDao: FactCheckHistoryDao = MedTrackDatabase.getDatabase(appContext).factCheckHistoryDao()
    private val patientDao: PatientDao = MedTrackDatabase.getDatabase(appContext).patientDao()
    private val medicationDao: MedicationDao = MedTrackDatabase.getDatabase(appContext).medicationDao()
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
     *
     * Every completed check (cache hit or live) is also recorded into this patient's
     * fact-check History — see [recordHistory] — regardless of which path answered it,
     * so History reflects everything the patient submitted, not just fresh API calls.
     *
     * @param patientId The logged-in patient submitting this claim, used only to tag the
     *   History record. Pass blank to skip writing a History record (e.g. if called before
     *   a patient is known) — the verification logic itself is unaffected either way.
     */
    fun verifyClaim(claim: String, patientId: String = "") {
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
                    uiState = FactCheckUiState.Success(
                        result = cached,
                        safetyWarnings = getSafetyWarnings(patientId, trimmed),
                        medicationInteractionCheckAvailable = MEDICATION_INTERACTION_CHECK_AVAILABLE,
                        hasRecordedMedications = hasRecordedMedications(patientId)
                    )
                    recordHistory(patientId, cached)
                    return@launch
                }

                val evidence = repository.retrieveEvidence(trimmed)
                val live = repository.checkClaimLive(trimmed, selectedLanguage, evidence.takeIf { !it.isEmpty })
                repository.saveToVerifiedClaimCache(
                    live,
                    evidenceUsed = evidence.matches.takeIf { !evidence.isEmpty }
                        ?.joinToString("; ") { it.evidence.evidenceText }
                )
                uiState = FactCheckUiState.Success(
                    result = live,
                    safetyWarnings = getSafetyWarnings(patientId, trimmed),
                    medicationInteractionCheckAvailable = MEDICATION_INTERACTION_CHECK_AVAILABLE,
                    hasRecordedMedications = hasRecordedMedications(patientId)
                )
                recordHistory(patientId, live)
            } catch (e: Exception) {
                uiState = FactCheckUiState.Error(
                    "Couldn't reach the verification service. Please check your connection and try again."
                )
            }
        }
    }


    /**
     * Builds patient-specific safety warnings without inventing interaction rules.
     * Allergy matching is intentionally limited to the medicine names explicitly
     * recorded by the patient. Pairwise medication conflicts remain UNKNOWN until
     * a verified interaction source is configured.
     */
    private suspend fun getSafetyWarnings(patientId: String, checkedText: String): List<MedicationSafetyWarning> {
        if (patientId.isBlank()) return emptyList()
        val patient = patientDao.getPatientById(patientId) ?: return emptyList()
        val allergies = patient.medicineAllergies
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        val normalizedChecked = normalizeMedicineText(checkedText)
        val allergyMatch = allergies.any { allergy ->
            val normalizedAllergy = normalizeMedicineText(allergy)
            normalizedAllergy.isNotBlank() && normalizedChecked.contains(normalizedAllergy)
        }
        return if (allergyMatch) listOf(MedicationSafetyWarning.ALLERGY) else emptyList()
    }


    private suspend fun hasRecordedMedications(patientId: String): Boolean {
        if (patientId.isBlank()) return false
        return medicationDao.getMedicationsForPatient(patientId).first().isNotEmpty()
    }

    private fun normalizeMedicineText(value: String): String =
        value.lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()

    /** Writes one History row for a completed [result], unless [patientId] is blank. */
    private suspend fun recordHistory(patientId: String, result: FactCheckResult) {
        if (patientId.isBlank()) return
        val title = result.claim.take(60).let { if (result.claim.length > 60) "$it…" else it }
        historyDao.insert(
            FactCheckHistory(
                patientId = patientId,
                title = title,
                originalMessage = result.claim,
                verdict = result.verdict.name,
                explanation = result.explanation,
                sourceNames = result.sources.joinToString(";") { it.name },
                sourceUrls = result.sources.joinToString(";") { it.url ?: "" },
                sourceTypes = result.sources.joinToString(";") { it.sourceType },
                languageCode = result.languageCode,
                officialEvidenceFound = result.officialEvidenceFound,
                checkedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    /** This patient's past fact-check submissions, newest first, for the History screen. */
    fun getHistory(patientId: String): Flow<List<FactCheckHistory>> =
        historyDao.getHistoryForPatient(patientId)

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
