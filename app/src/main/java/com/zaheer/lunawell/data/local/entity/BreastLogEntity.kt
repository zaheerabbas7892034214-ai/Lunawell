package com.zaheer.lunawell.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "breast_logs",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BreastLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "profile_id")
    val profileId: Long,
    
    @ColumnInfo(name = "date")
    val date: Long,
    
    @ColumnInfo(name = "symptom_type")
    val symptomType: String, // e.g., "Lump", "Pain", "Discharge", "SkinChanges"
    
    @ColumnInfo(name = "notes")
    val notes: String?,
    
    @ColumnInfo(name = "image_uri")
    val imageUri: String? // local file URI
)
