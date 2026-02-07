package com.zaheer.lunawell.data.local.dao

import androidx.room.*
import com.zaheer.lunawell.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Insert
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)

    @Query("SELECT * FROM appointments WHERE profileId = :profileId ORDER BY date ASC")
    fun getAppointmentsByProfile(profileId: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE profileId = :profileId AND date >= :currentDate ORDER BY date ASC")
    fun getUpcomingAppointments(profileId: Long, currentDate: Long): Flow<List<AppointmentEntity>>
    
    @Query("SELECT * FROM appointments")
    suspend fun getAllAppointmentsOnce(): List<AppointmentEntity>
    
    @Insert
    suspend fun insertAppointments(appointments: List<AppointmentEntity>)
}
