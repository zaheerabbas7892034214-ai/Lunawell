package com.zaheer.lunawell.presentation.breasthealth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.lunawell.billing.BillingManager
import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.data.repository.BreastHealthRepository
import com.zaheer.lunawell.data.repository.ProfileRepository
import com.zaheer.lunawell.domain.model.BreastLog
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class BreastHealthUiState(
    val lastBreastLog: BreastLog? = null,
    val breastLogs: List<BreastLog> = emptyList(),
    val upcomingExamDate: Long? = null,
    val isPro: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

data class BreastLogFormState(
    val date: Long = System.currentTimeMillis(),
    val lumpFound: Boolean = false,
    val lumpSeverity: Int = 1,
    val pain: Boolean = false,
    val painSeverity: Int = 1,
    val discharge: Boolean = false,
    val dischargeSeverity: Int = 1,
    val skinChanges: Boolean = false,
    val skinChangesSeverity: Int = 1,
    val other: Boolean = false,
    val otherSeverity: Int = 1,
    val notes: String = "",
    val imageUri: String? = null
)

class BreastHealthViewModel(
    private val breastHealthRepository: BreastHealthRepository,
    private val profileRepository: ProfileRepository,
    private val billingManager: BillingManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BreastHealthUiState())
    val uiState: StateFlow<BreastHealthUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(BreastLogFormState())
    val formState: StateFlow<BreastLogFormState> = _formState.asStateFlow()

    init {
        loadBreastHealthData()
    }

    private fun loadBreastHealthData() {
        viewModelScope.launch {
            try {
                combine(
                    profileRepository.getActiveProfile(),
                    billingManager.isPro,
                    preferencesManager.getIsProActive()
                ) { profile, isPro, isProPref ->
                    Triple(profile, isPro || isProPref, profile?.id)
                }.collectLatest { (profile, isPro, profileId) ->
                    if (profile == null || profileId == null) {
                        _uiState.value = BreastHealthUiState(
                            isLoading = false,
                            error = "No active profile found"
                        )
                        return@collectLatest
                    }

                    combine(
                        breastHealthRepository.getLastBreastLog(profileId),
                        breastHealthRepository.getBreastLogsByProfile(profileId)
                    ) { lastLog, allLogs ->
                        val upcomingExam = calculateUpcomingExamDate(lastLog?.date)
                        
                        BreastHealthUiState(
                            lastBreastLog = lastLog,
                            breastLogs = allLogs.sortedByDescending { it.date },
                            upcomingExamDate = upcomingExam,
                            isPro = isPro,
                            isLoading = false
                        )
                    }.collect { state ->
                        _uiState.value = state
                    }
                }
            } catch (e: Exception) {
                _uiState.value = BreastHealthUiState(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    private fun calculateUpcomingExamDate(lastExamDate: Long?): Long {
        val calendar = Calendar.getInstance()
        if (lastExamDate != null) {
            calendar.timeInMillis = lastExamDate
            calendar.add(Calendar.MONTH, 1)
        } else {
            calendar.add(Calendar.MONTH, 1)
        }
        return calendar.timeInMillis
    }

    fun updateFormDate(date: Long) {
        _formState.value = _formState.value.copy(date = date)
    }

    fun updateLumpFound(checked: Boolean) {
        _formState.value = _formState.value.copy(lumpFound = checked)
    }

    fun updateLumpSeverity(severity: Int) {
        _formState.value = _formState.value.copy(lumpSeverity = severity)
    }

    fun updatePain(checked: Boolean) {
        _formState.value = _formState.value.copy(pain = checked)
    }

    fun updatePainSeverity(severity: Int) {
        _formState.value = _formState.value.copy(painSeverity = severity)
    }

    fun updateDischarge(checked: Boolean) {
        _formState.value = _formState.value.copy(discharge = checked)
    }

    fun updateDischargeSeverity(severity: Int) {
        _formState.value = _formState.value.copy(dischargeSeverity = severity)
    }

    fun updateSkinChanges(checked: Boolean) {
        _formState.value = _formState.value.copy(skinChanges = checked)
    }

    fun updateSkinChangesSeverity(severity: Int) {
        _formState.value = _formState.value.copy(skinChangesSeverity = severity)
    }

    fun updateOther(checked: Boolean) {
        _formState.value = _formState.value.copy(other = checked)
    }

    fun updateOtherSeverity(severity: Int) {
        _formState.value = _formState.value.copy(otherSeverity = severity)
    }

    fun updateNotes(notes: String) {
        _formState.value = _formState.value.copy(notes = notes)
    }

    fun updateImageUri(uri: Uri?) {
        _formState.value = _formState.value.copy(imageUri = uri?.toString())
    }

    fun saveBreastLog() {
        viewModelScope.launch {
            try {
                val profile = profileRepository.getActiveProfile().first()
                if (profile == null) {
                    _uiState.value = _uiState.value.copy(error = "No active profile")
                    return@launch
                }

                val form = _formState.value
                val symptoms = buildList {
                    if (form.lumpFound) add("Lump (${form.lumpSeverity}/5)")
                    if (form.pain) add("Pain (${form.painSeverity}/5)")
                    if (form.discharge) add("Discharge (${form.dischargeSeverity}/5)")
                    if (form.skinChanges) add("Skin Changes (${form.skinChangesSeverity}/5)")
                    if (form.other) add("Other (${form.otherSeverity}/5)")
                }.joinToString(", ")

                val breastLog = BreastLog(
                    profileId = profile.id,
                    date = form.date,
                    symptomType = symptoms.ifEmpty { "Normal - No symptoms" },
                    notes = form.notes.ifBlank { null },
                    imageUri = form.imageUri
                )

                breastHealthRepository.insertBreastLog(breastLog)
                
                _uiState.value = _uiState.value.copy(saveSuccess = true)
                resetForm()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to save")
            }
        }
    }

    fun resetForm() {
        _formState.value = BreastLogFormState()
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
