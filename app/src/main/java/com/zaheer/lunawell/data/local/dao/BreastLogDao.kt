package com.zaheer.lunawell.data.local.dao

import androidx.room.*
import com.zaheer.lunawell.data.local.entity.BreastLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BreastLogDao {
    @Insert
    suspend fun insertBreastLog(log: BreastLogEntity): Long

    @Query("SELECT * FROM breast_logs WHERE profileId = :profileId ORDER BY date DESC")
    fun getBreastLogsByProfile(profileId: Long): Flow<List<BreastLogEntity>>

    @Query("SELECT * FROM breast_logs WHERE profileId = :profileId ORDER BY date DESC LIMIT 1")
    fun getLastBreastLog(profileId: Long): Flow<BreastLogEntity?>
    
    @Query("SELECT * FROM breast_logs")
    suspend fun getAllBreastLogsOnce(): List<BreastLogEntity>
    
    @Insert
    suspend fun insertBreastLogs(logs: List<BreastLogEntity>)
}
