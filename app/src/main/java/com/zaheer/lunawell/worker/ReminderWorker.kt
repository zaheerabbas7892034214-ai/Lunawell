package com.zaheer.lunawell.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.zaheer.lunawell.data.local.LunaWellDatabase
import com.zaheer.lunawell.utils.NotificationUtils
import java.util.concurrent.TimeUnit

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val KEY_REMINDER_ID = "reminder_id"
        const val KEY_REMINDER_TYPE = "reminder_type"
        const val KEY_PROFILE_ID = "profile_id"
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        
        fun scheduleReminder(
            context: Context,
            reminderId: Long,
            timeMillis: Long,
            reminderType: String,
            profileId: Long,
            title: String,
            message: String
        ) {
            val currentTime = System.currentTimeMillis()
            val delay = timeMillis - currentTime
            
            if (delay <= 0) {
                return
            }
            
            val inputData = workDataOf(
                KEY_REMINDER_ID to reminderId,
                KEY_REMINDER_TYPE to reminderType,
                KEY_PROFILE_ID to profileId,
                KEY_TITLE to title,
                KEY_MESSAGE to message
            )
            
            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("reminder_$reminderId")
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "reminder_$reminderId",
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }
    }
    
    override suspend fun doWork(): Result {
        return try {
            val reminderId = inputData.getLong(KEY_REMINDER_ID, -1L)
            val reminderType = inputData.getString(KEY_REMINDER_TYPE) ?: return Result.failure()
            val profileId = inputData.getLong(KEY_PROFILE_ID, -1L)
            val title = inputData.getString(KEY_TITLE) ?: "LunaWell Reminder"
            val message = inputData.getString(KEY_MESSAGE) ?: "You have a reminder"
            
            if (reminderId == -1L || profileId == -1L) {
                return Result.failure()
            }
            
            val database = LunaWellDatabase.getInstance(context)
            
            // Check if reminder is still enabled
            val reminder = database.reminderDao().getReminderByIdOnce(reminderId)
            if (reminder == null || !reminder.isEnabled) {
                return Result.success()
            }
            
            // Determine notification channel based on reminder type
            val channelId = when (reminderType) {
                "Period", "SelfExam" -> NotificationUtils.CHANNEL_ID_PERIOD
                "Medication", "Appointment" -> NotificationUtils.CHANNEL_ID_REMINDER
                else -> NotificationUtils.CHANNEL_ID_REMINDER
            }
            
            // Show notification
            NotificationUtils.showNotification(
                context = context,
                channelId = channelId,
                title = title,
                message = message,
                notificationId = reminderId.toInt()
            )
            
            // If recurring, schedule next reminder
            if (reminder.isRecurring) {
                val nextTime = reminder.time + TimeUnit.DAYS.toMillis(1)
                scheduleReminder(
                    context = context,
                    reminderId = reminderId,
                    timeMillis = nextTime,
                    reminderType = reminderType,
                    profileId = profileId,
                    title = title,
                    message = message
                )
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
