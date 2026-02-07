package com.zaheer.lunawell.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "cycles",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CycleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "profile_id")
    val profileId: Long,
    
    @ColumnInfo(name = "start_date")
    val startDate: Long,
    
    @ColumnInfo(name = "end_date")
    val endDate: Long?,
    
    @ColumnInfo(name = "cycle_length")
    val cycleLength: Int,
    
    @ColumnInfo(name = "average_cycle_length")
    val averageCycleLength: Float
)
