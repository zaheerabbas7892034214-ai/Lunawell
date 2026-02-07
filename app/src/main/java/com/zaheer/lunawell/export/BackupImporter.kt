package com.zaheer.lunawell.export

import android.content.Context
import android.net.Uri
import com.zaheer.lunawell.data.local.LunaWellDatabase
import com.zaheer.lunawell.data.local.entity.AppointmentEntity
import com.zaheer.lunawell.data.local.entity.BreastLogEntity
import com.zaheer.lunawell.data.local.entity.CycleEntity
import com.zaheer.lunawell.data.local.entity.DailyLogEntity
import com.zaheer.lunawell.data.local.entity.EntitlementEntity
import com.zaheer.lunawell.data.local.entity.PregnancyLogEntity
import com.zaheer.lunawell.data.local.entity.ProfileEntity
import com.zaheer.lunawell.data.local.entity.ReminderEntity
import com.zaheer.lunawell.data.local.entity.SymptomLogEntity
import com.zaheer.lunawell.security.EncryptionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class BackupImporter(
    private val context: Context,
    private val database: LunaWellDatabase,
    private val encryptionManager: EncryptionManager
) {
    
    suspend fun importBackup(inputUri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Read encrypted data
                val encryptedData = context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                    inputStream.readBytes()
                } ?: return@withContext false
                
                // Decrypt data
                val decryptedData = encryptionManager.decryptData(encryptedData)
                val jsonString = String(decryptedData)
                val backupData = JSONObject(jsonString)
                
                // Validate backup format
                if (!backupData.has("version") || !backupData.has("timestamp")) {
                    return@withContext false
                }
                
                // Clear existing data (optional - you can modify this behavior)
                database.clearAllTables()
                
                // Import Profiles
                if (backupData.has("profiles")) {
                    val profilesArray = backupData.getJSONArray("profiles")
                    val profiles = mutableListOf<ProfileEntity>()
                    for (i in 0 until profilesArray.length()) {
                        val obj = profilesArray.getJSONObject(i)
                        profiles.add(
                            ProfileEntity(
                                id = obj.getLong("id"),
                                name = obj.getString("name"),
                                dateOfBirth = obj.getLong("dateOfBirth"),
                                mode = obj.getString("mode"),
                                isActive = obj.getBoolean("isActive")
                            )
                        )
                    }
                    database.profileDao().insertProfiles(profiles)
                }
                
                // Import Cycles
                if (backupData.has("cycles")) {
                    val cyclesArray = backupData.getJSONArray("cycles")
                    val cycles = mutableListOf<CycleEntity>()
                    for (i in 0 until cyclesArray.length()) {
                        val obj = cyclesArray.getJSONObject(i)
                        cycles.add(
                            CycleEntity(
                                id = obj.getLong("id"),
                                profileId = obj.getLong("profileId"),
                                startDate = obj.getLong("startDate"),
                                endDate = if (obj.isNull("endDate")) null else obj.getLong("endDate"),
                                cycleLength = obj.getInt("cycleLength"),
                                averageCycleLength = obj.getDouble("averageCycleLength").toFloat()
                            )
                        )
                    }
                    database.cycleDao().insertCycles(cycles)
                }
                
                // Import Daily Logs
                if (backupData.has("dailyLogs")) {
                    val dailyLogsArray = backupData.getJSONArray("dailyLogs")
                    val dailyLogs = mutableListOf<DailyLogEntity>()
                    for (i in 0 until dailyLogsArray.length()) {
                        val obj = dailyLogsArray.getJSONObject(i)
                        dailyLogs.add(
                            DailyLogEntity(
                                id = obj.getLong("id"),
                                profileId = obj.getLong("profileId"),
                                date = obj.getLong("date"),
                                flowIntensity = if (obj.isNull("flowIntensity")) null else obj.getString("flowIntensity"),
                                mood = if (obj.isNull("mood")) null else obj.getInt("mood"),
                                sleep = if (obj.isNull("sleep")) null else obj.getDouble("sleep").toFloat(),
                                hydration = if (obj.isNull("hydration")) null else obj.getInt("hydration"),
                                exercise = if (obj.isNull("exercise")) null else obj.getInt("exercise"),
                                notes = if (obj.isNull("notes")) null else obj.getString("notes")
                            )
                        )
                    }
                    database.dailyLogDao().insertDailyLogs(dailyLogs)
                }
                
                // Import Symptom Logs
                if (backupData.has("symptomLogs")) {
                    val symptomLogsArray = backupData.getJSONArray("symptomLogs")
                    val symptomLogs = mutableListOf<SymptomLogEntity>()
                    for (i in 0 until symptomLogsArray.length()) {
                        val obj = symptomLogsArray.getJSONObject(i)
                        symptomLogs.add(
                            SymptomLogEntity(
                                id = obj.getLong("id"),
                                profileId = obj.getLong("profileId"),
                                date = obj.getLong("date"),
                                symptomType = obj.getString("symptomType"),
                                severity = obj.getInt("severity"),
                                notes = if (obj.isNull("notes")) null else obj.getString("notes")
                            )
                        )
                    }
                    database.symptomLogDao().insertSymptomLogs(symptomLogs)
                }
                
                // Import Pregnancy Logs
                if (backupData.has("pregnancyLogs")) {
                    val pregnancyLogsArray = backupData.getJSONArray("pregnancyLogs")
                    val pregnancyLogs = mutableListOf<PregnancyLogEntity>()
                    for (i in 0 until pregnancyLogsArray.length()) {
                        val obj = pregnancyLogsArray.getJSONObject(i)
                        pregnancyLogs.add(
                            PregnancyLogEntity(
                                id = obj.getLong("id"),
                                profileId = obj.getLong("profileId"),
                                dueDate = if (obj.isNull("dueDate")) null else obj.getLong("dueDate"),
                                lmpDate = if (obj.isNull("lmpDate")) null else obj.getLong("lmpDate"),
                                currentWeek = obj.getInt("currentWeek"),
                                currentDay = obj.getInt("currentDay"),
                                weight = if (obj.isNull("weight")) null else obj.getDouble("weight").toFloat(),
                                notes = if (obj.isNull("notes")) null else obj.getString("notes")
                            )
                        )
                    }
                    database.pregnancyLogDao().insertPregnancyLogs(pregnancyLogs)
                }
                
                // Import Breast Logs
                if (backupData.has("breastLogs")) {
                    val breastLogsArray = backupData.getJSONArray("breastLogs")
                    val breastLogs = mutableListOf<BreastLogEntity>()
                    for (i in 0 until breastLogsArray.length()) {
                        val obj = breastLogsArray.getJSONObject(i)
                        breastLogs.add(
                            BreastLogEntity(
                                id = obj.getLong("id"),
                                profileId = obj.getLong("profileId"),
                                date = obj.getLong("date"),
                                symptomType = obj.getString("symptomType"),
                                notes = if (obj.isNull("notes")) null else obj.getString("notes"),
                                imageUri = if (obj.isNull("imageUri")) null else obj.getString("imageUri")
                            )
                        )
                    }
                    database.breastLogDao().insertBreastLogs(breastLogs)
                }
                
                // Import Reminders
                if (backupData.has("reminders")) {
                    val remindersArray = backupData.getJSONArray("reminders")
                    val reminders = mutableListOf<ReminderEntity>()
                    for (i in 0 until remindersArray.length()) {
                        val obj = remindersArray.getJSONObject(i)
                        reminders.add(
                            ReminderEntity(
                                id = obj.getLong("id"),
                                profileId = obj.getLong("profileId"),
                                type = obj.getString("type"),
                                title = obj.getString("title"),
                                time = obj.getLong("time"),
                                isRecurring = obj.getBoolean("isRecurring"),
                                isEnabled = obj.getBoolean("isEnabled")
                            )
                        )
                    }
                    database.reminderDao().insertReminders(reminders)
                }
                
                // Import Appointments
                if (backupData.has("appointments")) {
                    val appointmentsArray = backupData.getJSONArray("appointments")
                    val appointments = mutableListOf<AppointmentEntity>()
                    for (i in 0 until appointmentsArray.length()) {
                        val obj = appointmentsArray.getJSONObject(i)
                        appointments.add(
                            AppointmentEntity(
                                id = obj.getLong("id"),
                                profileId = obj.getLong("profileId"),
                                date = obj.getLong("date"),
                                doctorName = obj.getString("doctorName"),
                                notes = if (obj.isNull("notes")) null else obj.getString("notes"),
                                reminderEnabled = obj.getBoolean("reminderEnabled")
                            )
                        )
                    }
                    database.appointmentDao().insertAppointments(appointments)
                }
                
                // Import Entitlements
                if (backupData.has("entitlements")) {
                    val entitlementsArray = backupData.getJSONArray("entitlements")
                    val entitlements = mutableListOf<EntitlementEntity>()
                    for (i in 0 until entitlementsArray.length()) {
                        val obj = entitlementsArray.getJSONObject(i)
                        entitlements.add(
                            EntitlementEntity(
                                id = obj.getLong("id"),
                                isProActive = obj.getBoolean("isProActive"),
                                expiryTimestamp = if (obj.isNull("expiryTimestamp")) null else obj.getLong("expiryTimestamp"),
                                purchaseToken = if (obj.isNull("purchaseToken")) null else obj.getString("purchaseToken"),
                                lastCheckedTimestamp = obj.getLong("lastCheckedTimestamp")
                            )
                        )
                    }
                    database.entitlementDao().insertEntitlements(entitlements)
                }
                
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
