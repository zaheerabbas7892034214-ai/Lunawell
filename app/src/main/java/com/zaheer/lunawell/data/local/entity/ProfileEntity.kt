package com.zaheer.lunawell.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "date_of_birth")
    val dateOfBirth: Long,
    
    @ColumnInfo(name = "mode")
    val mode: String, // "Cycle", "Pregnancy", "Wellness"
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean
)
