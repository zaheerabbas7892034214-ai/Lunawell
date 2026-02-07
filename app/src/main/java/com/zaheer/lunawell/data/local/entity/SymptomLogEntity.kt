package com.zaheer.lunawell.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "symptom_logs",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SymptomLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "profile_id")
    val profileId: Long,
    
    @ColumnInfo(name = "date")
    val date: Long,
    
    @ColumnInfo(name = "symptom_type")
    val symptomType: String, // e.g., "Cramps", "Headache", "Acne", "Bloating"
    
    @ColumnInfo(name = "severity")
    val severity: Int, // 1-5
    
    @ColumnInfo(name = "notes")
    val notes: String?
)
