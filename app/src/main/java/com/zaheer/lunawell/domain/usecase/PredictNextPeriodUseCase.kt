package com.zaheer.lunawell.domain.usecase

import com.zaheer.lunawell.data.repository.CycleRepository
import com.zaheer.lunawell.utils.CycleCalculator
import kotlinx.coroutines.flow.firstOrNull

class PredictNextPeriodUseCase(
    private val cycleRepository: CycleRepository
) {
    suspend operator fun invoke(profileId: Long): Long? {
        val currentCycle = cycleRepository.getLastCycle(profileId).firstOrNull()
        return currentCycle?.let {
            CycleCalculator.predictNextPeriod(it.startDate, it.averageCycleLength)
        }
    }
}
