package com.zaheer.lunawell.data.local.dao

import androidx.room.*
import com.zaheer.lunawell.data.local.entity.SymptomLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomLogDao {
    @Insert
    suspend fun insertSymptomLog(log: SymptomLogEntity): Long

    @Query("SELECT * FROM symptom_logs WHERE profileId = :profileId ORDER BY date DESC")
    fun getSymptomsByProfile(profileId: Long): Flow<List<SymptomLogEntity>>

    @Query("SELECT * FROM symptom_logs WHERE profileId = :profileId AND date BETWEEN :startDate AND :endDate")
    fun getSymptomsInRange(profileId: Long, startDate: Long, endDate: Long): Flow<List<SymptomLogEntity>>
    
    @Query("SELECT * FROM symptom_logs")
    suspend fun getAllSymptomLogsOnce(): List<SymptomLogEntity>
    
    @Insert
    suspend fun insertSymptomLogs(logs: List<SymptomLogEntity>)
}
