package com.zaheer.lunawell.presentation.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.data.repository.DailyLogRepository
import com.zaheer.lunawell.data.repository.SymptomLogRepository
import com.zaheer.lunawell.domain.model.DailyLog
import com.zaheer.lunawell.domain.model.Symptom
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LogUiState(
    val selectedDate: Long = System.currentTimeMillis(),
    val currentTab: Int = 0,
    val profileId: Long? = null,
    val existingLog: DailyLog? = null,
    
    // Period tab
    val flowIntensity: String? = null,
    
    // Symptoms tab
    val selectedSymptoms: Map<String, Int> = emptyMap(), // symptom name to severity (1-5)
    val symptomNotes: String = "",
    
    // Mood & Sleep tab
    val mood: Int? = null,
    val sleep: Float? = null,
    
    // Wellness tab
    val hydration: Int = 0,
    val exercise: Int = 0,
    
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

class LogViewModel(
    private val dailyLogRepository: DailyLogRepository,
    private val symptomLogRepository: SymptomLogRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    init {
        loadExistingLog()
    }

    fun setSelectedDate(dateMillis: Long) {
        _uiState.value = _uiState.value.copy(selectedDate = dateMillis)
        loadExistingLog()
    }

    private fun loadExistingLog() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            preferencesManager.getLastActiveProfileId().collectLatest { profileId ->
                if (profileId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No active profile"
                    )
                    return@collectLatest
                }

                _uiState.value = _uiState.value.copy(profileId = profileId)

                dailyLogRepository.getDailyLog(profileId, _uiState.value.selectedDate)
                    .collectLatest { log ->
                        _uiState.value = _uiState.value.copy(
                            existingLog = log,
                            flowIntensity = log?.flowIntensity,
                            mood = log?.mood,
                            sleep = log?.sleep,
                            hydration = log?.hydration ?: 0,
                            exercise = log?.exercise ?: 0,
                            isLoading = false
                        )
                    }
            }
        }
    }

    fun setTab(tab: Int) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun setFlowIntensity(intensity: String) {
        _uiState.value = _uiState.value.copy(flowIntensity = intensity)
    }

    fun setMood(mood: Int) {
        _uiState.value = _uiState.value.copy(mood = mood)
    }

    fun setSleep(hours: Float) {
        _uiState.value = _uiState.value.copy(sleep = hours)
    }

    fun setHydration(glasses: Int) {
        _uiState.value = _uiState.value.copy(hydration = glasses)
    }

    fun setExercise(minutes: Int) {
        _uiState.value = _uiState.value.copy(exercise = minutes)
    }

    fun setSymptomSeverity(symptom: String, severity: Int) {
        val symptoms = _uiState.value.selectedSymptoms.toMutableMap()
        if (severity > 0) {
            symptoms[symptom] = severity
        } else {
            symptoms.remove(symptom)
        }
        _uiState.value = _uiState.value.copy(selectedSymptoms = symptoms)
    }

    fun setSymptomNotes(notes: String) {
        _uiState.value = _uiState.value.copy(symptomNotes = notes)
    }

    fun saveLog() {
        viewModelScope.launch {
            val state = _uiState.value
            val profileId = state.profileId ?: return@launch

            _uiState.value = state.copy(isSaving = true)

            try {
                // Save daily log
                val dailyLog = DailyLog(
                    id = state.existingLog?.id ?: 0,
                    profileId = profileId,
                    date = state.selectedDate,
                    flowIntensity = state.flowIntensity,
                    mood = state.mood,
                    sleep = state.sleep,
                    hydration = state.hydration,
                    exercise = state.exercise,
                    notes = state.symptomNotes
                )
                
                dailyLogRepository.insertDailyLog(dailyLog)

                // Save symptoms
                state.selectedSymptoms.forEach { (symptomName, severity) ->
                    val symptom = Symptom(
                        profileId = profileId,
                        date = state.selectedDate,
                        symptomType = symptomName,
                        severity = severity,
                        notes = state.symptomNotes
                    )
                    symptomLogRepository.insertSymptomLog(symptom)
                }

                _uiState.value = state.copy(
                    isSaving = false,
                    saveSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isSaving = false,
                    error = e.message
                )
            }
        }
    }

    fun resetSaveState() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}
