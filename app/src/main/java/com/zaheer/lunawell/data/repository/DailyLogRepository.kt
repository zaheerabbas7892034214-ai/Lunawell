package com.zaheer.lunawell.data.repository

import com.zaheer.lunawell.data.local.dao.DailyLogDao
import com.zaheer.lunawell.domain.model.DailyLog
import com.zaheer.lunawell.domain.model.toDomain
import com.zaheer.lunawell.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DailyLogRepository(private val dailyLogDao: DailyLogDao) {
    
    suspend fun insertDailyLog(log: DailyLog): Long {
        return dailyLogDao.insertDailyLog(log.toEntity())
    }
    
    fun getDailyLog(profileId: Long, date: Long): Flow<DailyLog?> {
        return dailyLogDao.getDailyLog(profileId, date).map { it?.toDomain() }
    }
    
    fun getDailyLogsInRange(profileId: Long, startDate: Long, endDate: Long): Flow<List<DailyLog>> {
        return dailyLogDao.getDailyLogsInRange(profileId, startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
