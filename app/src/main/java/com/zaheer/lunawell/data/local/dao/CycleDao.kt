package com.zaheer.lunawell.data.local.dao

import androidx.room.*
import com.zaheer.lunawell.data.local.entity.CycleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {
    @Insert
    suspend fun insertCycle(cycle: CycleEntity): Long

    @Query("SELECT * FROM cycles WHERE profileId = :profileId ORDER BY startDate DESC")
    fun getCyclesByProfile(profileId: Long): Flow<List<CycleEntity>>

    @Query("SELECT * FROM cycles WHERE profileId = :profileId ORDER BY startDate DESC LIMIT 1")
    fun getLastCycle(profileId: Long): Flow<CycleEntity?>

    @Query("SELECT * FROM cycles WHERE profileId = :profileId ORDER BY startDate DESC LIMIT :limit")
    fun getRecentCycles(profileId: Long, limit: Int): Flow<List<CycleEntity>>
    
    @Query("SELECT * FROM cycles")
    suspend fun getAllCyclesOnce(): List<CycleEntity>
    
    @Insert
    suspend fun insertCycles(cycles: List<CycleEntity>)
}
