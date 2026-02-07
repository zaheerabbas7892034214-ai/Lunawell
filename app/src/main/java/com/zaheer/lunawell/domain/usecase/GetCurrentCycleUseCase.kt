package com.zaheer.lunawell.domain.usecase

import com.zaheer.lunawell.data.repository.CycleRepository
import com.zaheer.lunawell.domain.model.Cycle
import kotlinx.coroutines.flow.Flow

class GetCurrentCycleUseCase(
    private val cycleRepository: CycleRepository
) {
    operator fun invoke(profileId: Long): Flow<Cycle?> {
        return cycleRepository.getLastCycle(profileId)
    }
}
