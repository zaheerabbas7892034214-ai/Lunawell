package com.zaheer.lunawell.data.repository

import com.zaheer.lunawell.data.local.dao.AppointmentDao
import com.zaheer.lunawell.domain.model.Appointment
import com.zaheer.lunawell.domain.model.toDomain
import com.zaheer.lunawell.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppointmentRepository(private val appointmentDao: AppointmentDao) {
    
    suspend fun insertAppointment(appointment: Appointment): Long {
        return appointmentDao.insertAppointment(appointment.toEntity())
    }
    
    suspend fun updateAppointment(appointment: Appointment) {
        appointmentDao.updateAppointment(appointment.toEntity())
    }
    
    suspend fun deleteAppointment(appointment: Appointment) {
        appointmentDao.deleteAppointment(appointment.toEntity())
    }
    
    fun getAppointmentsByProfile(profileId: Long): Flow<List<Appointment>> {
        return appointmentDao.getAppointmentsByProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun getUpcomingAppointments(profileId: Long, currentDate: Long): Flow<List<Appointment>> {
        return appointmentDao.getUpcomingAppointments(profileId, currentDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
