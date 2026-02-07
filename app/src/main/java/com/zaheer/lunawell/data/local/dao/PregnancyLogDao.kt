package com.zaheer.lunawell.data.local.dao

import androidx.room.*
import com.zaheer.lunawell.data.local.entity.PregnancyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PregnancyLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPregnancyLog(log: PregnancyLogEntity): Long

    @Update
    suspend fun updatePregnancyLog(log: PregnancyLogEntity)

    @Query("SELECT * FROM pregnancy_logs WHERE profileId = :profileId ORDER BY id DESC LIMIT 1")
    fun getCurrentPregnancyLog(profileId: Long): Flow<PregnancyLogEntity?>
    
    @Query("SELECT * FROM pregnancy_logs WHERE profileId = :profileId ORDER BY id DESC")
    fun getPregnancyLogsByProfile(profileId: Long): Flow<List<PregnancyLogEntity>>
    
    @Query("SELECT * FROM pregnancy_logs")
    suspend fun getAllPregnancyLogsOnce(): List<PregnancyLogEntity>
    
    @Insert
    suspend fun insertPregnancyLogs(logs: List<PregnancyLogEntity>)
}
