package com.zaheer.lunawell.domain.model

import com.zaheer.lunawell.data.local.entity.PregnancyLogEntity

data class PregnancyLog(
    val id: Long = 0,
    val profileId: Long,
    val dueDate: Long?,
    val lmpDate: Long?,
    val currentWeek: Int,
    val currentDay: Int,
    val weight: Float?,
    val notes: String?
)

fun PregnancyLogEntity.toDomain(): PregnancyLog {
    return PregnancyLog(
        id = id,
        profileId = profileId,
        dueDate = dueDate,
        lmpDate = lmpDate,
        currentWeek = currentWeek,
        currentDay = currentDay,
        weight = weight,
        notes = notes
    )
}

fun PregnancyLog.toEntity(): PregnancyLogEntity {
    return PregnancyLogEntity(
        id = id,
        profileId = profileId,
        dueDate = dueDate,
        lmpDate = lmpDate,
        currentWeek = currentWeek,
        currentDay = currentDay,
        weight = weight,
        notes = notes
    )
}
