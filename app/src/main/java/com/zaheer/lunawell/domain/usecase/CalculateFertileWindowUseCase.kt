package com.zaheer.lunawell.domain.usecase

import com.zaheer.lunawell.data.repository.CycleRepository
import com.zaheer.lunawell.utils.CycleCalculator
import kotlinx.coroutines.flow.firstOrNull

class CalculateFertileWindowUseCase(
    private val cycleRepository: CycleRepository
) {
    suspend operator fun invoke(profileId: Long): Pair<Long, Long>? {
        val currentCycle = cycleRepository.getLastCycle(profileId).firstOrNull()
        return currentCycle?.let {
            CycleCalculator.calculateFertileWindow(it.startDate, it.averageCycleLength)
        }
    }
}
