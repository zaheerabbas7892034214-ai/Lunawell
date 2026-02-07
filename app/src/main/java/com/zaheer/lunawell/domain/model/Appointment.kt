package com.zaheer.lunawell.domain.model

import com.zaheer.lunawell.data.local.entity.AppointmentEntity

data class Appointment(
    val id: Long = 0,
    val profileId: Long,
    val date: Long,
    val doctorName: String,
    val notes: String?,
    val reminderEnabled: Boolean
)

fun AppointmentEntity.toDomain(): Appointment {
    return Appointment(
        id = id,
        profileId = profileId,
        date = date,
        doctorName = doctorName,
        notes = notes,
        reminderEnabled = reminderEnabled
    )
}

fun Appointment.toEntity(): AppointmentEntity {
    return AppointmentEntity(
        id = id,
        profileId = profileId,
        date = date,
        doctorName = doctorName,
        notes = notes,
        reminderEnabled = reminderEnabled
    )
}
