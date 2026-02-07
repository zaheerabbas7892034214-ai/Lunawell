package com.zaheer.lunawell.data.repository

import com.zaheer.lunawell.data.local.dao.PregnancyLogDao
import com.zaheer.lunawell.domain.model.PregnancyLog
import com.zaheer.lunawell.domain.model.toDomain
import com.zaheer.lunawell.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PregnancyRepository(private val pregnancyLogDao: PregnancyLogDao) {
    
    suspend fun insertPregnancyLog(log: PregnancyLog): Long {
        return pregnancyLogDao.insertPregnancyLog(log.toEntity())
    }
    
    suspend fun updatePregnancyLog(log: PregnancyLog) {
        pregnancyLogDao.updatePregnancyLog(log.toEntity())
    }
    
    fun getCurrentPregnancyLog(profileId: Long): Flow<PregnancyLog?> {
        return pregnancyLogDao.getCurrentPregnancyLog(profileId).map { it?.toDomain() }
    }
    
    fun getPregnancyLogsByProfile(profileId: Long): Flow<List<PregnancyLog>> {
        return pregnancyLogDao.getPregnancyLogsByProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
