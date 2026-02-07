package com.zaheer.lunawell.data.repository

import com.zaheer.lunawell.data.local.dao.ProfileDao
import com.zaheer.lunawell.domain.model.Profile
import com.zaheer.lunawell.domain.model.toDomain
import com.zaheer.lunawell.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepository(private val profileDao: ProfileDao) {
    
    suspend fun insertProfile(profile: Profile): Long {
        return profileDao.insertProfile(profile.toEntity())
    }
    
    suspend fun updateProfile(profile: Profile) {
        profileDao.updateProfile(profile.toEntity())
    }
    
    suspend fun deleteProfile(profile: Profile) {
        profileDao.deleteProfile(profile.toEntity())
    }
    
    fun getProfile(id: Long): Flow<Profile?> {
        return profileDao.getProfile(id).map { it?.toDomain() }
    }
    
    fun getActiveProfile(): Flow<Profile?> {
        return profileDao.getActiveProfile().map { it?.toDomain() }
    }
    
    fun getAllProfiles(): Flow<List<Profile>> {
        return profileDao.getAllProfiles().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
