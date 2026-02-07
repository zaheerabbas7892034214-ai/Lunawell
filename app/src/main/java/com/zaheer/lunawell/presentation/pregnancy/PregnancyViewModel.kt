package com.zaheer.lunawell.presentation.pregnancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.data.repository.AppointmentRepository
import com.zaheer.lunawell.data.repository.DailyLogRepository
import com.zaheer.lunawell.data.repository.PregnancyRepository
import com.zaheer.lunawell.data.repository.SymptomLogRepository
import com.zaheer.lunawell.domain.model.Appointment
import com.zaheer.lunawell.domain.model.PregnancyLog
import com.zaheer.lunawell.domain.model.Symptom
import com.zaheer.lunawell.domain.usecase.CalculatePregnancyWeekUseCase
import com.zaheer.lunawell.utils.DateUtils
import com.zaheer.lunawell.utils.PregnancyCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PregnancyUiState(
    val currentWeek: Int = 0,
    val currentDay: Int = 0,
    val trimester: Int = 1,
    val pregnancyLog: PregnancyLog? = null,
    val appointments: List<Appointment> = emptyList(),
    val symptoms: List<Symptom> = emptyList(),
    val weightHistory: List<Pair<Long, Float>> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isProActive: Boolean = false
)

data class KickCounterState(
    val count: Int = 0,
    val startTime: Long? = null,
    val isActive: Boolean = false,
    val sessions: List<KickSession> = emptyList()
)

data class KickSession(
    val date: Long,
    val count: Int,
    val duration: Long
)

data class ContractionState(
    val contractions: List<Contraction> = emptyList(),
    val currentContractionStart: Long? = null,
    val isTimingContraction: Boolean = false
)

data class Contraction(
    val startTime: Long,
    val endTime: Long,
    val duration: Long
) {
    val durationSeconds: Long get() = duration / 1000
}

class PregnancyViewModel(
    private val pregnancyRepository: PregnancyRepository,
    private val appointmentRepository: AppointmentRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val symptomLogRepository: SymptomLogRepository,
    private val calculatePregnancyWeekUseCase: CalculatePregnancyWeekUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PregnancyUiState())
    val uiState: StateFlow<PregnancyUiState> = _uiState.asStateFlow()

    private val _kickCounterState = MutableStateFlow(KickCounterState())
    val kickCounterState: StateFlow<KickCounterState> = _kickCounterState.asStateFlow()

    private val _contractionState = MutableStateFlow(ContractionState())
    val contractionState: StateFlow<ContractionState> = _contractionState.asStateFlow()

    init {
        loadPregnancyData()
        observeProStatus()
    }

    private fun observeProStatus() {
        viewModelScope.launch {
            preferencesManager.getIsProActive().collectLatest { isPro ->
                _uiState.value = _uiState.value.copy(isProActive = isPro)
            }
        }
    }

    private fun loadPregnancyData() {
        viewModelScope.launch {
            try {
                preferencesManager.getLastActiveProfileId().collectLatest { profileId ->
                    if (profileId == null) {
                        _uiState.value = PregnancyUiState(
                            isLoading = false,
                            error = "No active profile"
                        )
                        return@collectLatest
                    }

                    combine(
                        pregnancyRepository.getCurrentPregnancyLog(profileId),
                        appointmentRepository.getAppointmentsByProfile(profileId),
                        symptomLogRepository.getSymptomsByProfile(profileId)
                    ) { log, appointments, symptoms ->
                        Triple(log, appointments, symptoms)
                    }.collectLatest { (log, appointments, symptoms) ->
                        if (log != null) {
                            val weekDay = calculatePregnancyWeekUseCase(profileId) ?: Pair(0, 0)
                            val trimester = PregnancyCalculator.getTrimester(weekDay.first)
                            
                            val weightHistory = loadWeightHistory(profileId)
                            
                            _uiState.value = PregnancyUiState(
                                currentWeek = weekDay.first,
                                currentDay = weekDay.second,
                                trimester = trimester,
                                pregnancyLog = log,
                                appointments = appointments,
                                symptoms = symptoms,
                                weightHistory = weightHistory,
                                isLoading = false,
                                isProActive = _uiState.value.isProActive
                            )
                        } else {
                            _uiState.value = PregnancyUiState(
                                isLoading = false,
                                error = "No pregnancy log found"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private suspend fun loadWeightHistory(profileId: Long): List<Pair<Long, Float>> {
        val thirtyDaysAgo = DateUtils.addDays(DateUtils.getCurrentTimestamp(), -30)
        val logs = dailyLogRepository.getDailyLogsInRange(
            profileId,
            thirtyDaysAgo,
            DateUtils.getCurrentTimestamp()
        ).firstOrNull() ?: emptyList()
        
        return logs.mapNotNull { log ->
            _uiState.value.pregnancyLog?.weight?.let { weight ->
                Pair(log.date, weight)
            }
        }
    }

    fun incrementKickCount() {
        val state = _kickCounterState.value
        if (!state.isActive) {
            _kickCounterState.value = state.copy(
                count = 1,
                startTime = System.currentTimeMillis(),
                isActive = true
            )
        } else {
            _kickCounterState.value = state.copy(count = state.count + 1)
        }
    }

    fun resetKickCounter() {
        val state = _kickCounterState.value
        if (state.isActive && state.startTime != null) {
            val session = KickSession(
                date = System.currentTimeMillis(),
                count = state.count,
                duration = System.currentTimeMillis() - state.startTime
            )
            _kickCounterState.value = KickCounterState(
                sessions = listOf(session) + state.sessions.take(9)
            )
            saveKickSession(session)
        } else {
            _kickCounterState.value = KickCounterState()
        }
    }

    fun stopKickCounter() {
        val state = _kickCounterState.value
        if (state.isActive && state.startTime != null && state.count > 0) {
            val session = KickSession(
                date = System.currentTimeMillis(),
                count = state.count,
                duration = System.currentTimeMillis() - state.startTime
            )
            _kickCounterState.value = state.copy(
                isActive = false,
                sessions = listOf(session) + state.sessions.take(9)
            )
            saveKickSession(session)
        }
    }

    private fun saveKickSession(session: KickSession) {
        viewModelScope.launch {
            try {
                val profileId = preferencesManager.getLastActiveProfileId().firstOrNull() ?: return@launch
                val log = dailyLogRepository.getDailyLog(profileId, session.date).firstOrNull()
                
                val notes = buildString {
                    log?.notes?.let { append(it).append("\n") }
                    append("Kick count: ${session.count} in ${session.duration / 60000} minutes")
                }
                
                dailyLogRepository.insertDailyLog(
                    (log ?: com.zaheer.lunawell.domain.model.DailyLog(
                        profileId = profileId,
                        date = session.date,
                        flowIntensity = null,
                        mood = null,
                        sleep = null,
                        hydration = null,
                        exercise = null,
                        notes = null
                    )).copy(notes = notes)
                )
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    fun startContraction() {
        _contractionState.value = _contractionState.value.copy(
            currentContractionStart = System.currentTimeMillis(),
            isTimingContraction = true
        )
    }

    fun stopContraction() {
        val state = _contractionState.value
        val startTime = state.currentContractionStart ?: return
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        val contraction = Contraction(
            startTime = startTime,
            endTime = endTime,
            duration = duration
        )
        
        _contractionState.value = state.copy(
            contractions = listOf(contraction) + state.contractions,
            currentContractionStart = null,
            isTimingContraction = false
        )
        
        saveContraction(contraction)
    }

    fun clearContractions() {
        _contractionState.value = ContractionState()
    }

    private fun saveContraction(contraction: Contraction) {
        viewModelScope.launch {
            try {
                val profileId = preferencesManager.getLastActiveProfileId().firstOrNull() ?: return@launch
                val log = dailyLogRepository.getDailyLog(profileId, contraction.startTime).firstOrNull()
                
                val notes = buildString {
                    log?.notes?.let { append(it).append("\n") }
                    append("Contraction: ${contraction.durationSeconds}s")
                }
                
                dailyLogRepository.insertDailyLog(
                    (log ?: com.zaheer.lunawell.domain.model.DailyLog(
                        profileId = profileId,
                        date = contraction.startTime,
                        flowIntensity = null,
                        mood = null,
                        sleep = null,
                        hydration = null,
                        exercise = null,
                        notes = null
                    )).copy(notes = notes)
                )
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    fun getAverageContractionInterval(): Long? {
        val contractions = _contractionState.value.contractions
        if (contractions.size < 2) return null
        
        val intervals = contractions.zipWithNext { a, b ->
            b.startTime - a.endTime
        }
        
        return intervals.average().toLong()
    }

    fun getDevelopmentInfo(week: Int): String {
        return when (week) {
            in 1..4 -> "Your baby is just beginning to form. The neural tube is developing."
            in 5..8 -> "Your baby's heart is beating and major organs are forming."
            in 9..12 -> "Your baby is now a fetus. Fingers and toes are forming."
            in 13..16 -> "Your baby can make sucking motions and may start hiccupping."
            in 17..20 -> "You might start feeling baby's movements. Hair is growing."
            in 21..24 -> "Baby's hearing is developing. They can hear your voice."
            in 25..28 -> "Baby's eyes can open and close. They're gaining weight."
            in 29..32 -> "Baby's bones are fully developed but still soft and flexible."
            in 33..36 -> "Baby is getting into position for birth. Lungs are maturing."
            in 37..40 -> "Baby is full term and ready for birth. See you soon!"
            else -> "Congratulations on your pregnancy journey!"
        }
    }
}
