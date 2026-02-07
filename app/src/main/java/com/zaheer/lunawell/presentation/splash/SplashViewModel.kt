package com.zaheer.lunawell.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.lunawell.billing.BillingManager
import com.zaheer.lunawell.data.datastore.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class SplashDestination {
    object Onboarding : SplashDestination()
    object Home : SplashDestination()
}

data class SplashUiState(
    val isLoading: Boolean = true,
    val destination: SplashDestination? = null
)

class SplashViewModel(
    private val preferencesManager: PreferencesManager,
    private val billingManager: BillingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkInitialState()
    }

    private fun checkInitialState() {
        viewModelScope.launch {
            // Check subscription status
            billingManager.queryPurchases()
            
            // Small delay for splash screen visibility
            delay(1500)
            
            // Check if onboarding completed
            val onboardingCompleted = preferencesManager.getOnboardingCompleted().first()
            
            val destination = if (onboardingCompleted) {
                SplashDestination.Home
            } else {
                SplashDestination.Onboarding
            }
            
            _uiState.value = SplashUiState(
                isLoading = false,
                destination = destination
            )
        }
    }
}
