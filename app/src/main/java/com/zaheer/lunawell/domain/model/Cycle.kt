package com.zaheer.lunawell.domain.model

import com.zaheer.lunawell.data.local.entity.CycleEntity

data class Cycle(
    val id: Long = 0,
    val profileId: Long,
    val startDate: Long,
    val endDate: Long?,
    val cycleLength: Int,
    val averageCycleLength: Float
)

fun CycleEntity.toDomain(): Cycle {
    return Cycle(
        id = id,
        profileId = profileId,
        startDate = startDate,
        endDate = endDate,
        cycleLength = cycleLength,
        averageCycleLength = averageCycleLength
    )
}

fun Cycle.toEntity(): CycleEntity {
    return CycleEntity(
        id = id,
        profileId = profileId,
        startDate = startDate,
        endDate = endDate,
        cycleLength = cycleLength,
        averageCycleLength = averageCycleLength
    )
}
