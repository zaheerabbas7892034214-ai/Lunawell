package com.zaheer.lunawell.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.lunawell.billing.BillingManager
import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.data.repository.ProfileRepository
import com.zaheer.lunawell.domain.model.Cycle
import com.zaheer.lunawell.domain.model.Profile
import com.zaheer.lunawell.domain.usecase.CalculateFertileWindowUseCase
import com.zaheer.lunawell.domain.usecase.GetCurrentCycleUseCase
import com.zaheer.lunawell.domain.usecase.PredictNextPeriodUseCase
import com.zaheer.lunawell.utils.CycleCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class HomeUiState(
    val profile: Profile? = null,
    val currentCycle: Cycle? = null,
    val cycleDay: Int = 0,
    val nextPeriodDate: Long? = null,
    val daysUntilPeriod: Int = 0,
    val fertileWindowStart: Long? = null,
    val fertileWindowEnd: Long? = null,
    val isFertile: Boolean = false,
    val isPro: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(
    private val profileRepository: ProfileRepository,
    private val getCurrentCycleUseCase: GetCurrentCycleUseCase,
    private val predictNextPeriodUseCase: PredictNextPeriodUseCase,
    private val calculateFertileWindowUseCase: CalculateFertileWindowUseCase,
    private val billingManager: BillingManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
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
                        _uiState.value = HomeUiState(
                            isLoading = false,
                            error = "No active profile found"
                        )
                        return@collectLatest
                    }

                    getCurrentCycleUseCase(profileId).collectLatest { cycle ->
                        if (cycle != null) {
                            val now = System.currentTimeMillis()
                            val cycleDay = calculateCycleDay(cycle.startDate, now)
                            
                            val nextPeriod = CycleCalculator.predictNextPeriod(
                                cycle.startDate,
                                cycle.averageCycleLength
                            )
                            
                            val daysUntil = ((nextPeriod - now) / (1000 * 60 * 60 * 24)).toInt()
                            
                            val fertileWindow = CycleCalculator.calculateFertileWindow(
                                cycle.startDate,
                                cycle.averageCycleLength
                            )
                            
                            val isFertile = fertileWindow?.let { (start, end) ->
                                now in start..end
                            } ?: false

                            _uiState.value = HomeUiState(
                                profile = profile,
                                currentCycle = cycle,
                                cycleDay = cycleDay,
                                nextPeriodDate = nextPeriod,
                                daysUntilPeriod = daysUntil,
                                fertileWindowStart = fertileWindow?.first,
                                fertileWindowEnd = fertileWindow?.second,
                                isFertile = isFertile,
                                isPro = isPro,
                                isLoading = false
                            )
                        } else {
                            _uiState.value = HomeUiState(
                                profile = profile,
                                isPro = isPro,
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    private fun calculateCycleDay(startDate: Long, currentDate: Long): Int {
        val diff = currentDate - startDate
        return (diff / (1000 * 60 * 60 * 24)).toInt() + 1
    }

    fun refresh() {
        loadHomeData()
    }
}
