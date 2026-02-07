package com.zaheer.lunawell.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "pregnancy_logs",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PregnancyLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "profile_id")
    val profileId: Long,
    
    @ColumnInfo(name = "due_date")
    val dueDate: Long?,
    
    @ColumnInfo(name = "lmp_date")
    val lmpDate: Long?,
    
    @ColumnInfo(name = "current_week")
    val currentWeek: Int,
    
    @ColumnInfo(name = "current_day")
    val currentDay: Int,
    
    @ColumnInfo(name = "weight")
    val weight: Float?, // kg
    
    @ColumnInfo(name = "notes")
    val notes: String?
)
