package com.zaheer.lunawell.export

import android.content.Context
import android.net.Uri
import com.zaheer.lunawell.data.local.LunaWellDatabase
import com.zaheer.lunawell.security.EncryptionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BackupExporter(
    private val context: Context,
    private val database: LunaWellDatabase,
    private val encryptionManager: EncryptionManager
) {
    
    suspend fun exportBackup(outputUri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val backupData = JSONObject()
                backupData.put("version", 1)
                backupData.put("timestamp", System.currentTimeMillis())
                
                // Export Profiles
                val profiles = database.profileDao().getAllProfilesOnce()
                val profilesArray = JSONArray()
                profiles.forEach { profile ->
                    val profileObj = JSONObject().apply {
                        put("id", profile.id)
                        put("name", profile.name)
                        put("dateOfBirth", profile.dateOfBirth)
                        put("mode", profile.mode)
                        put("isActive", profile.isActive)
                    }
                    profilesArray.put(profileObj)
                }
                backupData.put("profiles", profilesArray)
                
                // Export Cycles
                val cycles = database.cycleDao().getAllCyclesOnce()
                val cyclesArray = JSONArray()
                cycles.forEach { cycle ->
                    val cycleObj = JSONObject().apply {
                        put("id", cycle.id)
                        put("profileId", cycle.profileId)
                        put("startDate", cycle.startDate)
                        put("endDate", cycle.endDate)
                        put("cycleLength", cycle.cycleLength)
                        put("averageCycleLength", cycle.averageCycleLength.toDouble())
                    }
                    cyclesArray.put(cycleObj)
                }
                backupData.put("cycles", cyclesArray)
                
                // Export Daily Logs
                val dailyLogs = database.dailyLogDao().getAllDailyLogsOnce()
                val dailyLogsArray = JSONArray()
                dailyLogs.forEach { log ->
                    val logObj = JSONObject().apply {
                        put("id", log.id)
                        put("profileId", log.profileId)
                        put("date", log.date)
                        put("flowIntensity", log.flowIntensity)
                        put("mood", log.mood)
                        put("sleep", log.sleep?.toDouble())
                        put("hydration", log.hydration)
                        put("exercise", log.exercise)
                        put("notes", log.notes)
                    }
                    dailyLogsArray.put(logObj)
                }
                backupData.put("dailyLogs", dailyLogsArray)
                
                // Export Symptom Logs
                val symptomLogs = database.symptomLogDao().getAllSymptomLogsOnce()
                val symptomLogsArray = JSONArray()
                symptomLogs.forEach { log ->
                    val logObj = JSONObject().apply {
                        put("id", log.id)
                        put("profileId", log.profileId)
                        put("date", log.date)
                        put("symptomType", log.symptomType)
                        put("severity", log.severity)
                        put("notes", log.notes)
                    }
                    symptomLogsArray.put(logObj)
                }
                backupData.put("symptomLogs", symptomLogsArray)
                
                // Export Pregnancy Logs
                val pregnancyLogs = database.pregnancyLogDao().getAllPregnancyLogsOnce()
                val pregnancyLogsArray = JSONArray()
                pregnancyLogs.forEach { log ->
                    val logObj = JSONObject().apply {
                        put("id", log.id)
                        put("profileId", log.profileId)
                        put("dueDate", log.dueDate)
                        put("lmpDate", log.lmpDate)
                        put("currentWeek", log.currentWeek)
                        put("currentDay", log.currentDay)
                        put("weight", log.weight?.toDouble())
                        put("notes", log.notes)
                    }
                    pregnancyLogsArray.put(logObj)
                }
                backupData.put("pregnancyLogs", pregnancyLogsArray)
                
                // Export Breast Logs
                val breastLogs = database.breastLogDao().getAllBreastLogsOnce()
                val breastLogsArray = JSONArray()
                breastLogs.forEach { log ->
                    val logObj = JSONObject().apply {
                        put("id", log.id)
                        put("profileId", log.profileId)
                        put("date", log.date)
                        put("symptomType", log.symptomType)
                        put("notes", log.notes)
                        put("imageUri", log.imageUri)
                    }
                    breastLogsArray.put(logObj)
                }
                backupData.put("breastLogs", breastLogsArray)
                
                // Export Reminders
                val reminders = database.reminderDao().getAllRemindersOnce()
                val remindersArray = JSONArray()
                reminders.forEach { reminder ->
                    val reminderObj = JSONObject().apply {
                        put("id", reminder.id)
                        put("profileId", reminder.profileId)
                        put("type", reminder.type)
                        put("title", reminder.title)
                        put("time", reminder.time)
                        put("isRecurring", reminder.isRecurring)
                        put("isEnabled", reminder.isEnabled)
                    }
                    remindersArray.put(reminderObj)
                }
                backupData.put("reminders", remindersArray)
                
                // Export Appointments
                val appointments = database.appointmentDao().getAllAppointmentsOnce()
                val appointmentsArray = JSONArray()
                appointments.forEach { appointment ->
                    val appointmentObj = JSONObject().apply {
                        put("id", appointment.id)
                        put("profileId", appointment.profileId)
                        put("date", appointment.date)
                        put("doctorName", appointment.doctorName)
                        put("notes", appointment.notes)
                        put("reminderEnabled", appointment.reminderEnabled)
                    }
                    appointmentsArray.put(appointmentObj)
                }
                backupData.put("appointments", appointmentsArray)
                
                // Export Entitlements
                val entitlements = database.entitlementDao().getAllEntitlementsOnce()
                val entitlementsArray = JSONArray()
                entitlements.forEach { entitlement ->
                    val entitlementObj = JSONObject().apply {
                        put("id", entitlement.id)
                        put("isProActive", entitlement.isProActive)
                        put("expiryTimestamp", entitlement.expiryTimestamp)
                        put("purchaseToken", entitlement.purchaseToken)
                        put("lastCheckedTimestamp", entitlement.lastCheckedTimestamp)
                    }
                    entitlementsArray.put(entitlementObj)
                }
                backupData.put("entitlements", entitlementsArray)
                
                // Convert to JSON string and encrypt
                val jsonString = backupData.toString()
                val encryptedData = encryptionManager.encryptData(jsonString.toByteArray())
                
                // Write to output URI
                context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                    outputStream.write(encryptedData)
                    outputStream.flush()
                }
                
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
