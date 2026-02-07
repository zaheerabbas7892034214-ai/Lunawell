package com.zaheer.lunawell.data.local.dao

import androidx.room.*
import com.zaheer.lunawell.data.local.entity.DailyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyLog(log: DailyLogEntity): Long

    @Query("SELECT * FROM daily_logs WHERE profileId = :profileId AND date = :date")
    fun getDailyLog(profileId: Long, date: Long): Flow<DailyLogEntity?>

    @Query("SELECT * FROM daily_logs WHERE profileId = :profileId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getDailyLogsInRange(profileId: Long, startDate: Long, endDate: Long): Flow<List<DailyLogEntity>>
}
