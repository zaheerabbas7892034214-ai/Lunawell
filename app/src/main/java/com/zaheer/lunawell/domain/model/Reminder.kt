package com.zaheer.lunawell.domain.model

import com.zaheer.lunawell.data.local.entity.ReminderEntity

data class Reminder(
    val id: Long = 0,
    val profileId: Long,
    val type: String,
    val title: String,
    val time: Long,
    val isRecurring: Boolean,
    val isEnabled: Boolean
)

fun ReminderEntity.toDomain(): Reminder {
    return Reminder(
        id = id,
        profileId = profileId,
        type = type,
        title = title,
        time = time,
        isRecurring = isRecurring,
        isEnabled = isEnabled
    )
}

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        profileId = profileId,
        type = type,
        title = title,
        time = time,
        isRecurring = isRecurring,
        isEnabled = isEnabled
    )
}
