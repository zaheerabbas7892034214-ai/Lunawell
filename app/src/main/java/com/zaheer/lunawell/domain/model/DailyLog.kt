package com.zaheer.lunawell.domain.model

import com.zaheer.lunawell.data.local.entity.DailyLogEntity

data class DailyLog(
    val id: Long = 0,
    val profileId: Long,
    val date: Long,
    val flowIntensity: String?,
    val mood: Int?,
    val sleep: Float?,
    val hydration: Int?,
    val exercise: Int?,
    val notes: String?
)

fun DailyLogEntity.toDomain(): DailyLog {
    return DailyLog(
        id = id,
        profileId = profileId,
        date = date,
        flowIntensity = flowIntensity,
        mood = mood,
        sleep = sleep,
        hydration = hydration,
        exercise = exercise,
        notes = notes
    )
}

fun DailyLog.toEntity(): DailyLogEntity {
    return DailyLogEntity(
        id = id,
        profileId = profileId,
        date = date,
        flowIntensity = flowIntensity,
        mood = mood,
        sleep = sleep,
        hydration = hydration,
        exercise = exercise,
        notes = notes
    )
}
