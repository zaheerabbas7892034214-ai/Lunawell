package com.zaheer.lunawell.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.data.repository.CycleRepository
import com.zaheer.lunawell.data.repository.DailyLogRepository
import com.zaheer.lunawell.domain.model.Cycle
import com.zaheer.lunawell.domain.model.DailyLog
import com.zaheer.lunawell.domain.usecase.CalculateFertileWindowUseCase
import com.zaheer.lunawell.domain.usecase.PredictNextPeriodUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class CalendarDayInfo(
    val date: Long,
    val isPeriodDay: Boolean = false,
    val isPredictedPeriod: Boolean = false,
    val isFertileDay: Boolean = false,
    val hasSymptoms: Boolean = false
)

data class CalendarUiState(
    val currentMonth: Calendar = Calendar.getInstance(),
    val days: List<CalendarDayInfo> = emptyList(),
    val selectedDate: Long? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class CalendarViewModel(
    private val cycleRepository: CycleRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val predictNextPeriodUseCase: PredictNextPeriodUseCase,
    private val calculateFertileWindowUseCase: CalculateFertileWindowUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadCalendarData()
    }

    private fun loadCalendarData() {
        viewModelScope.launch {
            try {
                preferencesManager.getLastActiveProfileId().collectLatest { profileId ->
                    if (profileId == null) {
                        _uiState.value = CalendarUiState(
                            isLoading = false,
                            error = "No active profile"
                        )
                        return@collectLatest
                    }

                    combine(
                        cycleRepository.getRecentCycles(profileId, 6),
                        dailyLogRepository.getDailyLogsInRange(
                            profileId,
                            getMonthStartDate(_uiState.value.currentMonth),
                            getMonthEndDate(_uiState.value.currentMonth)
                        )
                    ) { cycles, dailyLogs ->
                        Pair(cycles, dailyLogs)
                    }.collectLatest { (cycles, dailyLogs) ->
                        val days = generateCalendarDays(cycles, dailyLogs)
                        _uiState.value = _uiState.value.copy(
                            days = days,
                            isLoading = false
                        )
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

    private fun generateCalendarDays(
        cycles: List<Cycle>,
        dailyLogs: List<DailyLog>
    ): List<CalendarDayInfo> {
        val days = mutableListOf<CalendarDayInfo>()
        val calendar = _uiState.value.currentMonth.clone() as Calendar
        
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Add empty days for alignment
        repeat(firstDayOfWeek - 1) {
            days.add(CalendarDayInfo(date = 0))
        }
        
        // Get period dates from cycles
        val periodDates = mutableSetOf<Long>()
        cycles.forEach { cycle ->
            val cycleStart = Calendar.getInstance().apply { timeInMillis = cycle.startDate }
            // Assume 5 days period length
            for (i in 0 until 5) {
                periodDates.add(cycleStart.timeInMillis)
                cycleStart.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        // Predict next period
        val lastCycle = cycles.maxByOrNull { it.startDate }
        val predictedPeriodStart = lastCycle?.let {
            predictNextPeriodUseCase(it.startDate, it.averageCycleLength.toInt())
        }
        
        val predictedPeriodDates = mutableSetOf<Long>()
        if (predictedPeriodStart != null) {
            val predStart = Calendar.getInstance().apply { timeInMillis = predictedPeriodStart }
            for (i in 0 until 5) {
                predictedPeriodDates.add(predStart.timeInMillis)
                predStart.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        // Calculate fertile window
        val fertileWindow = lastCycle?.let {
            calculateFertileWindowUseCase(it.startDate, it.averageCycleLength.toInt())
        }
        
        val fertileDates = mutableSetOf<Long>()
        if (fertileWindow != null) {
            val fertileStart = Calendar.getInstance().apply { timeInMillis = fertileWindow.first }
            val fertileEnd = Calendar.getInstance().apply { timeInMillis = fertileWindow.second }
            while (fertileStart.timeInMillis <= fertileEnd.timeInMillis) {
                fertileDates.add(fertileStart.timeInMillis)
                fertileStart.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        // Map daily logs
        val logsMap = dailyLogs.associateBy { normalizeDate(it.date) }
        
        // Generate days
        for (day in 1..daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val dateMillis = normalizeDate(calendar.timeInMillis)
            
            days.add(
                CalendarDayInfo(
                    date = dateMillis,
                    isPeriodDay = periodDates.any { normalizeDate(it) == dateMillis },
                    isPredictedPeriod = predictedPeriodDates.any { normalizeDate(it) == dateMillis },
                    isFertileDay = fertileDates.any { normalizeDate(it) == dateMillis },
                    hasSymptoms = logsMap.containsKey(dateMillis)
                )
            )
        }
        
        return days
    }

    private fun normalizeDate(timeInMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun getMonthStartDate(calendar: Calendar): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getMonthEndDate(calendar: Calendar): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun selectDate(date: Long) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    fun previousMonth() {
        val newMonth = _uiState.value.currentMonth.clone() as Calendar
        newMonth.add(Calendar.MONTH, -1)
        _uiState.value = _uiState.value.copy(currentMonth = newMonth)
        loadCalendarData()
    }

    fun nextMonth() {
        val newMonth = _uiState.value.currentMonth.clone() as Calendar
        newMonth.add(Calendar.MONTH, 1)
        _uiState.value = _uiState.value.copy(currentMonth = newMonth)
        loadCalendarData()
    }
}
