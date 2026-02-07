package com.zaheer.lunawell.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.data.repository.CycleRepository
import com.zaheer.lunawell.data.repository.ProfileRepository
import com.zaheer.lunawell.domain.model.Cycle
import com.zaheer.lunawell.domain.model.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class OnboardingUiState(
    val currentStep: Int = 0,
    val selectedMode: String = "Cycle", // Cycle, Pregnancy, Wellness
    val lastPeriodDate: Long? = null,
    val dueDate: Long? = null,
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false
)

class OnboardingViewModel(
    private val profileRepository: ProfileRepository,
    private val cycleRepository: CycleRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun nextStep() {
        _uiState.value = _uiState.value.copy(
            currentStep = _uiState.value.currentStep + 1
        )
    }

    fun previousStep() {
        if (_uiState.value.currentStep > 0) {
            _uiState.value = _uiState.value.copy(
                currentStep = _uiState.value.currentStep - 1
            )
        }
    }

    fun setMode(mode: String) {
        _uiState.value = _uiState.value.copy(selectedMode = mode)
    }

    fun setLastPeriodDate(date: Long) {
        _uiState.value = _uiState.value.copy(lastPeriodDate = date)
    }

    fun setDueDate(date: Long) {
        _uiState.value = _uiState.value.copy(dueDate = date)
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Create default profile
                val profile = Profile(
                    name = "My Profile",
                    dateOfBirth = Calendar.getInstance().timeInMillis,
                    mode = _uiState.value.selectedMode,
                    isActive = true
                )
                
                val profileId = profileRepository.insertProfile(profile)
                preferencesManager.saveLastActiveProfileId(profileId)
                
                // Create initial cycle if in Cycle mode and date is provided
                if (_uiState.value.selectedMode == "Cycle" && _uiState.value.lastPeriodDate != null) {
                    val cycle = Cycle(
                        profileId = profileId,
                        startDate = _uiState.value.lastPeriodDate!!,
                        endDate = null,
                        cycleLength = 28,
                        averageCycleLength = 28f
                    )
                    cycleRepository.insertCycle(cycle)
                }
                
                // Mark onboarding as completed
                preferencesManager.saveOnboardingCompleted(true)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isCompleted = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
