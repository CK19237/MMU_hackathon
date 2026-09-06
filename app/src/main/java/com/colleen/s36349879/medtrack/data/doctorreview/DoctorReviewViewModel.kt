package com.colleen.s36349879.medtrack.data.doctorreview

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.colleen.s36349879.medtrack.data.MedTrackDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel for the clinician-only Doctor Review screen (reached from the
 * Clinician Dashboard, consistent with how [com.colleen.s36349879.medtrack.data.clinician.ClinicianPasswordViewModel]
 * gates the existing clinician dashboard route).
 */
class DoctorReviewViewModel(context: Context) : ViewModel() {

    private val dao: FlaggedItemDao = MedTrackDatabase.getDatabase(context.applicationContext).flaggedItemDao()

    val allFlaggedItems: Flow<List<FlaggedItem>> = dao.getAll()

    fun markReviewed(id: Int, note: String? = null) {
        viewModelScope.launch { dao.updateStatus(id, ReviewStatus.REVIEWED, note) }
    }

    fun markCorrected(id: Int, note: String) {
        viewModelScope.launch { dao.updateStatus(id, ReviewStatus.CORRECTED, note) }
    }

    class DoctorReviewViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DoctorReviewViewModel(context) as T
        }
    }
}
