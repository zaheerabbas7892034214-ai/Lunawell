package com.zaheer.lunawell.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "profile_id")
    val profileId: Long,
    
    @ColumnInfo(name = "type")
    val type: String, // "Period", "Medication", "SelfExam", "Appointment"
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "time")
    val time: Long,
    
    @ColumnInfo(name = "is_recurring")
    val isRecurring: Boolean,
    
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean
)
