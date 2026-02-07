package com.zaheer.lunawell.domain.usecase

import com.zaheer.lunawell.data.repository.PregnancyRepository
import com.zaheer.lunawell.utils.PregnancyCalculator
import kotlinx.coroutines.flow.firstOrNull

class CalculatePregnancyWeekUseCase(
    private val pregnancyRepository: PregnancyRepository
) {
    suspend operator fun invoke(profileId: Long): Pair<Int, Int>? {
        val pregnancyLog = pregnancyRepository.getCurrentPregnancyLog(profileId).firstOrNull()
        return pregnancyLog?.let {
            when {
                it.lmpDate != null -> PregnancyCalculator.calculateWeekAndDay(it.lmpDate)
                it.dueDate != null -> PregnancyCalculator.calculateWeekAndDayFromDueDate(it.dueDate)
                else -> null
            }
        }
    }
}
