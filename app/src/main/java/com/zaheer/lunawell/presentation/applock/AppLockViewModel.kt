package com.zaheer.lunawell.presentation.applock

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.security.AppBiometricManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.MessageDigest

data class AppLockUiState(
    val isSetupMode: Boolean = true,
    val isAppLockEnabled: Boolean = false,
    val hasSavedPin: Boolean = false,
    val pinInput: String = "",
    val confirmPinInput: String = "",
    val step: SetupStep = SetupStep.ENTER_PIN,
    val autoLockTimeout: AutoLockTimeout = AutoLockTimeout.IMMEDIATELY,
    val isBiometricAvailable: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

enum class SetupStep {
    ENTER_PIN,
    CONFIRM_PIN
}

enum class AutoLockTimeout(val label: String, val minutes: Int) {
    IMMEDIATELY("Immediately", 0),
    ONE_MINUTE("1 minute", 1),
    FIVE_MINUTES("5 minutes", 5),
    FIFTEEN_MINUTES("15 minutes", 15)
}

class AppLockViewModel(
    private val preferencesManager: PreferencesManager,
    private val biometricManager: AppBiometricManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppLockUiState())
    val uiState: StateFlow<AppLockUiState> = _uiState.asStateFlow()

    init {
        loadAppLockStatus()
    }

    private fun loadAppLockStatus() {
        viewModelScope.launch {
            combine(
                preferencesManager.getAppLockEnabled(),
                preferencesManager.getAppLockPin()
            ) { enabled, pin ->
                Pair(enabled, pin)
            }.collectLatest { (enabled, pin) ->
                _uiState.value = _uiState.value.copy(
                    isAppLockEnabled = enabled,
                    hasSavedPin = !pin.isNullOrEmpty(),
                    isSetupMode = pin.isNullOrEmpty(),
                    isBiometricAvailable = biometricManager.isBiometricAvailable()
                )
            }
        }
    }

    fun updatePinInput(input: String) {
        if (input.length <= 6 && input.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                pinInput = input,
                error = null
            )
        }
    }

    fun updateConfirmPinInput(input: String) {
        if (input.length <= 6 && input.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                confirmPinInput = input,
                error = null
            )
        }
    }

    fun proceedToConfirmPin() {
        val pin = _uiState.value.pinInput
        
        if (pin.length < 4) {
            _uiState.value = _uiState.value.copy(
                error = "PIN must be at least 4 digits"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            step = SetupStep.CONFIRM_PIN
        )
    }

    fun goBackToEnterPin() {
        _uiState.value = _uiState.value.copy(
            step = SetupStep.ENTER_PIN,
            confirmPinInput = "",
            error = null
        )
    }

    fun savePin() {
        viewModelScope.launch {
            val pin = _uiState.value.pinInput
            val confirmPin = _uiState.value.confirmPinInput

            if (pin.length < 4) {
                _uiState.value = _uiState.value.copy(
                    error = "PIN must be at least 4 digits"
                )
                return@launch
            }

            if (pin != confirmPin) {
                _uiState.value = _uiState.value.copy(
                    error = "PINs do not match"
                )
                return@launch
            }

            try {
                val hashedPin = hashPin(pin)
                preferencesManager.saveAppLockPin(hashedPin)
                preferencesManager.saveAppLockEnabled(true)
                
                _uiState.value = _uiState.value.copy(
                    successMessage = "PIN set successfully",
                    pinInput = "",
                    confirmPinInput = "",
                    step = SetupStep.ENTER_PIN,
                    isAppLockEnabled = true,
                    hasSavedPin = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to save PIN"
                )
            }
        }
    }

    fun verifyPin(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val inputPin = _uiState.value.pinInput
            
            if (inputPin.length < 4) {
                _uiState.value = _uiState.value.copy(
                    error = "PIN must be at least 4 digits"
                )
                return@launch
            }

            try {
                val savedPin = preferencesManager.getAppLockPin().first()
                val hashedInputPin = hashPin(inputPin)

                if (hashedInputPin == savedPin) {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Unlocked successfully",
                        pinInput = ""
                    )
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Incorrect PIN",
                        pinInput = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to verify PIN"
                )
            }
        }
    }

    fun authenticateWithBiometric(
        activity: FragmentActivity,
        onSuccess: () -> Unit
    ) {
        biometricManager.authenticate(
            fragmentActivity = activity,
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    successMessage = "Authenticated successfully"
                )
                onSuccess()
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(
                    error = error
                )
            }
        )
    }

    fun toggleAppLock() {
        viewModelScope.launch {
            try {
                val newValue = !_uiState.value.isAppLockEnabled
                preferencesManager.saveAppLockEnabled(newValue)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to toggle app lock"
                )
            }
        }
    }

    fun clearPin() {
        viewModelScope.launch {
            try {
                preferencesManager.saveAppLockPin("")
                preferencesManager.saveAppLockEnabled(false)
                _uiState.value = _uiState.value.copy(
                    successMessage = "PIN cleared successfully",
                    pinInput = "",
                    confirmPinInput = "",
                    step = SetupStep.ENTER_PIN,
                    isAppLockEnabled = false,
                    hasSavedPin = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to clear PIN"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
