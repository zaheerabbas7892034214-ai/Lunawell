package com.zaheer.lunawell.data.repository

import com.zaheer.lunawell.data.local.dao.CycleDao
import com.zaheer.lunawell.domain.model.Cycle
import com.zaheer.lunawell.domain.model.toDomain
import com.zaheer.lunawell.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CycleRepository(private val cycleDao: CycleDao) {
    
    suspend fun insertCycle(cycle: Cycle): Long {
        return cycleDao.insertCycle(cycle.toEntity())
    }
    
    fun getCyclesByProfile(profileId: Long): Flow<List<Cycle>> {
        return cycleDao.getCyclesByProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun getLastCycle(profileId: Long): Flow<Cycle?> {
        return cycleDao.getLastCycle(profileId).map { it?.toDomain() }
    }
    
    fun getRecentCycles(profileId: Long, limit: Int): Flow<List<Cycle>> {
        return cycleDao.getRecentCycles(profileId, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
