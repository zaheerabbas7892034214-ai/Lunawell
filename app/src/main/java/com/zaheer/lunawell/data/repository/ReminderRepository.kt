package com.zaheer.lunawell.data.repository

import com.zaheer.lunawell.data.local.dao.ReminderDao
import com.zaheer.lunawell.domain.model.Reminder
import com.zaheer.lunawell.domain.model.toDomain
import com.zaheer.lunawell.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepository(private val reminderDao: ReminderDao) {
    
    suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder.toEntity())
    }
    
    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder.toEntity())
    }
    
    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder.toEntity())
    }
    
    fun getRemindersByProfile(profileId: Long): Flow<List<Reminder>> {
        return reminderDao.getRemindersByProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun getActiveReminders(profileId: Long): Flow<List<Reminder>> {
        return reminderDao.getActiveReminders(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
