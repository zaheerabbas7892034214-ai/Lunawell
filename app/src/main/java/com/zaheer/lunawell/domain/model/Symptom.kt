package com.zaheer.lunawell.domain.model

import com.zaheer.lunawell.data.local.entity.SymptomLogEntity

data class Symptom(
    val id: Long = 0,
    val profileId: Long,
    val date: Long,
    val symptomType: String,
    val severity: Int,
    val notes: String?
)

fun SymptomLogEntity.toDomain(): Symptom {
    return Symptom(
        id = id,
        profileId = profileId,
        date = date,
        symptomType = symptomType,
        severity = severity,
        notes = notes
    )
}

fun Symptom.toEntity(): SymptomLogEntity {
    return SymptomLogEntity(
        id = id,
        profileId = profileId,
        date = date,
        symptomType = symptomType,
        severity = severity,
        notes = notes
    )
}
