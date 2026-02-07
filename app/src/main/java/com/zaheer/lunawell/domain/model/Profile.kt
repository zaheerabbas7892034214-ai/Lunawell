package com.zaheer.lunawell.domain.model

import com.zaheer.lunawell.data.local.entity.ProfileEntity

data class Profile(
    val id: Long = 0,
    val name: String,
    val dateOfBirth: Long,
    val mode: String,
    val isActive: Boolean
)

fun ProfileEntity.toDomain(): Profile {
    return Profile(
        id = id,
        name = name,
        dateOfBirth = dateOfBirth,
        mode = mode,
        isActive = isActive
    )
}

fun Profile.toEntity(): ProfileEntity {
    return ProfileEntity(
        id = id,
        name = name,
        dateOfBirth = dateOfBirth,
        mode = mode,
        isActive = isActive
    )
}
