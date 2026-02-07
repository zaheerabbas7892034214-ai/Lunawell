package com.zaheer.lunawell.data.repository

import com.zaheer.lunawell.data.local.dao.SymptomLogDao
import com.zaheer.lunawell.domain.model.Symptom
import com.zaheer.lunawell.domain.model.toDomain
import com.zaheer.lunawell.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SymptomLogRepository(private val symptomLogDao: SymptomLogDao) {
    
    suspend fun insertSymptomLog(log: Symptom): Long {
        return symptomLogDao.insertSymptomLog(log.toEntity())
    }
    
    fun getSymptomsByProfile(profileId: Long): Flow<List<Symptom>> {
        return symptomLogDao.getSymptomsByProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun getSymptomsInRange(profileId: Long, startDate: Long, endDate: Long): Flow<List<Symptom>> {
        return symptomLogDao.getSymptomsInRange(profileId, startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
