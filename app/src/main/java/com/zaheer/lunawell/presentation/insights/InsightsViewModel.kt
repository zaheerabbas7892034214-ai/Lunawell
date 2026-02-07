package com.zaheer.lunawell.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.lunawell.billing.BillingManager
import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.data.repository.CycleRepository
import com.zaheer.lunawell.domain.model.Cycle
import com.zaheer.lunawell.domain.usecase.CycleInsights
import com.zaheer.lunawell.domain.usecase.DetectPCOSPatternsUseCase
import com.zaheer.lunawell.domain.usecase.GetCycleInsightsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class InsightsUiState(
    val isPro: Boolean = false,
    val cycleInsights: CycleInsights? = null,
    val recentCycles: List<Cycle> = emptyList(),
    val pcosRisk: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

class InsightsViewModel(
    private val cycleRepository: CycleRepository,
    private val getCycleInsightsUseCase: GetCycleInsightsUseCase,
    private val detectPCOSPatternsUseCase: DetectPCOSPatternsUseCase,
    private val billingManager: BillingManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        loadInsights()
    }

    private fun loadInsights() {
        viewModelScope.launch {
            try {
                combine(
                    billingManager.isPro,
                    preferencesManager.getIsProActive(),
                    preferencesManager.getLastActiveProfileId()
                ) { isPro, isProPref, profileId ->
                    Triple(isPro || isProPref, profileId, profileId)
                }.collectLatest { (isPro, profileId, _) ->
                    if (profileId == null) {
                        _uiState.value = InsightsUiState(
                            isLoading = false,
                            error = "No active profile"
                        )
                        return@collectLatest
                    }

                    // Get cycle limit based on subscription
                    val cycleLimit = if (isPro) null else 2

                    val insights = getCycleInsightsUseCase(profileId, cycleLimit)
                    val cycles = if (isPro) {
                        cycleRepository.getCyclesByProfile(profileId).firstOrNull() ?: emptyList()
                    } else {
                        cycleRepository.getRecentCycles(profileId, 2).firstOrNull() ?: emptyList()
                    }

                    val pcosRisk = if (isPro) {
                        detectPCOSPatternsUseCase(profileId)
                    } else {
                        false
                    }

                    _uiState.value = InsightsUiState(
                        isPro = isPro,
                        cycleInsights = insights,
                        recentCycles = cycles,
                        pcosRisk = pcosRisk,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = InsightsUiState(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun refresh() {
        loadInsights()
    }
}
