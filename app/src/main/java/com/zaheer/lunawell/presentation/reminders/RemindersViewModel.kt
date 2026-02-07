package com.zaheer.lunawell.presentation.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.lunawell.billing.BillingManager
import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.data.repository.ProfileRepository
import com.zaheer.lunawell.data.repository.ReminderRepository
import com.zaheer.lunawell.domain.model.Reminder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RemindersUiState(
    val reminders: List<Reminder> = emptyList(),
    val isPro: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val editingReminder: Reminder? = null
)

data class ReminderFormState(
    val title: String = "",
    val time: Long = System.currentTimeMillis(),
    val type: String = "Custom",
    val isRecurring: Boolean = true,
    val isEnabled: Boolean = true
)

class RemindersViewModel(
    private val reminderRepository: ReminderRepository,
    private val profileRepository: ProfileRepository,
    private val billingManager: BillingManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemindersUiState())
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(ReminderFormState())
    val formState: StateFlow<ReminderFormState> = _formState.asStateFlow()

    init {
        loadReminders()
    }

    private fun loadReminders() {
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
                        _uiState.value = RemindersUiState(
                            isLoading = false,
                            error = "No active profile found"
                        )
                        return@collectLatest
                    }

                    reminderRepository.getRemindersByProfile(profileId).collect { reminders ->
                        _uiState.value = RemindersUiState(
                            reminders = reminders.sortedBy { it.time },
                            isPro = isPro,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = RemindersUiState(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun toggleReminderEnabled(reminder: Reminder) {
        viewModelScope.launch {
            try {
                val updatedReminder = reminder.copy(isEnabled = !reminder.isEnabled)
                reminderRepository.updateReminder(updatedReminder)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to update reminder")
            }
        }
    }

    fun showAddDialog() {
        _formState.value = ReminderFormState()
        _uiState.value = _uiState.value.copy(showAddDialog = true, editingReminder = null)
    }

    fun showEditDialog(reminder: Reminder) {
        _formState.value = ReminderFormState(
            title = reminder.title,
            time = reminder.time,
            type = reminder.type,
            isRecurring = reminder.isRecurring,
            isEnabled = reminder.isEnabled
        )
        _uiState.value = _uiState.value.copy(showAddDialog = true, editingReminder = reminder)
    }

    fun hideDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, editingReminder = null)
        _formState.value = ReminderFormState()
    }

    fun updateFormTitle(title: String) {
        _formState.value = _formState.value.copy(title = title)
    }

    fun updateFormTime(time: Long) {
        _formState.value = _formState.value.copy(time = time)
    }

    fun updateFormType(type: String) {
        _formState.value = _formState.value.copy(type = type)
    }

    fun updateFormRecurring(isRecurring: Boolean) {
        _formState.value = _formState.value.copy(isRecurring = isRecurring)
    }

    fun saveReminder() {
        viewModelScope.launch {
            try {
                val profile = profileRepository.getActiveProfile().first()
                if (profile == null) {
                    _uiState.value = _uiState.value.copy(error = "No active profile")
                    return@launch
                }

                val form = _formState.value
                
                if (form.title.isBlank()) {
                    _uiState.value = _uiState.value.copy(error = "Title cannot be empty")
                    return@launch
                }

                val editingReminder = _uiState.value.editingReminder
                
                if (editingReminder != null) {
                    // Update existing reminder
                    val updatedReminder = editingReminder.copy(
                        title = form.title,
                        time = form.time,
                        type = form.type,
                        isRecurring = form.isRecurring
                    )
                    reminderRepository.updateReminder(updatedReminder)
                } else {
                    // Create new reminder
                    val newReminder = Reminder(
                        profileId = profile.id,
                        title = form.title,
                        time = form.time,
                        type = form.type,
                        isRecurring = form.isRecurring,
                        isEnabled = true
                    )
                    reminderRepository.insertReminder(newReminder)
                }

                hideDialog()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to save reminder")
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            try {
                reminderRepository.deleteReminder(reminder)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to delete reminder")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
