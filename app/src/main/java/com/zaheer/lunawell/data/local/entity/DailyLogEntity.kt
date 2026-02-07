package com.zaheer.lunawell.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_logs",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DailyLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "profile_id")
    val profileId: Long,
    
    @ColumnInfo(name = "date")
    val date: Long,
    
    @ColumnInfo(name = "flow_intensity")
    val flowIntensity: String?, // "Light", "Medium", "Heavy"
    
    @ColumnInfo(name = "mood")
    val mood: Int?, // 1-5
    
    @ColumnInfo(name = "sleep")
    val sleep: Float?, // hours
    
    @ColumnInfo(name = "hydration")
    val hydration: Int?, // glasses
    
    @ColumnInfo(name = "exercise")
    val exercise: Int?, // minutes
    
    @ColumnInfo(name = "notes")
    val notes: String?
)
