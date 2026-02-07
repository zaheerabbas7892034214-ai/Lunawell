package com.zaheer.lunawell.domain.model

import com.zaheer.lunawell.data.local.entity.BreastLogEntity

data class BreastLog(
    val id: Long = 0,
    val profileId: Long,
    val date: Long,
    val symptomType: String,
    val notes: String?,
    val imageUri: String?
)

fun BreastLogEntity.toDomain(): BreastLog {
    return BreastLog(
        id = id,
        profileId = profileId,
        date = date,
        symptomType = symptomType,
        notes = notes,
        imageUri = imageUri
    )
}

fun BreastLog.toEntity(): BreastLogEntity {
    return BreastLogEntity(
        id = id,
        profileId = profileId,
        date = date,
        symptomType = symptomType,
        notes = notes,
        imageUri = imageUri
    )
}
