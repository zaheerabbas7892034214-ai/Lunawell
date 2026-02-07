package com.zaheer.lunawell.data.repository

import com.zaheer.lunawell.data.local.dao.BreastLogDao
import com.zaheer.lunawell.domain.model.BreastLog
import com.zaheer.lunawell.domain.model.toDomain
import com.zaheer.lunawell.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BreastHealthRepository(private val breastLogDao: BreastLogDao) {
    
    suspend fun insertBreastLog(log: BreastLog): Long {
        return breastLogDao.insertBreastLog(log.toEntity())
    }
    
    fun getBreastLogsByProfile(profileId: Long): Flow<List<BreastLog>> {
        return breastLogDao.getBreastLogsByProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun getLastBreastLog(profileId: Long): Flow<BreastLog?> {
        return breastLogDao.getLastBreastLog(profileId).map { it?.toDomain() }
    }
}
