package com.zaheer.lunawell.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.lunawell.billing.BillingManager
import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.data.local.LunaWellDatabase
import com.zaheer.lunawell.data.repository.ProfileRepository
import com.zaheer.lunawell.domain.model.Profile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isPro: Boolean = false,
    val profile: Profile? = null,
    val appLockEnabled: Boolean = false,
    val notificationPrivacyEnabled: Boolean = false,
    val appVersion: String = "1.0.0",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class SettingsViewModel(
    private val billingManager: BillingManager,
    private val preferencesManager: PreferencesManager,
    private val profileRepository: ProfileRepository,
    private val database: LunaWellDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                combine(
                    billingManager.isPro,
                    preferencesManager.getIsProActive(),
                    profileRepository.getActiveProfile(),
                    preferencesManager.getAppLockEnabled(),
                    preferencesManager.getNotificationPrivacyEnabled()
                ) { isPro, isProPref, profile, appLockEnabled, notificationPrivacy ->
                    SettingsUiState(
                        isPro = isPro || isProPref,
                        profile = profile,
                        appLockEnabled = appLockEnabled,
                        notificationPrivacyEnabled = notificationPrivacy,
                        appVersion = "1.0.0"
                    )
                }.collectLatest { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load settings"
                )
            }
        }
    }

    fun toggleNotificationPrivacy() {
        viewModelScope.launch {
            try {
                val newValue = !_uiState.value.notificationPrivacyEnabled
                preferencesManager.saveNotificationPrivacyEnabled(newValue)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update setting"
                )
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                billingManager.startConnection()
                val hasActivePurchase = billingManager.queryPurchases()
                
                if (hasActivePurchase) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Purchase restored successfully!"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No active purchases found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to restore purchases"
                )
            }
        }
    }

    suspend fun deleteAllData(): Boolean {
        return try {
            // Clear all database tables
            database.clearAllTables()
            
            // Clear all preferences
            preferencesManager.saveOnboardingCompleted(false)
            preferencesManager.saveIsProActive(false)
            preferencesManager.saveAppLockEnabled(false)
            preferencesManager.saveNotificationPrivacyEnabled(false)
            
            true
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = e.message ?: "Failed to delete data"
            )
            false
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
}
