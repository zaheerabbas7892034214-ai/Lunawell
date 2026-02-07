package com.zaheer.lunawell.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entitlements")
data class EntitlementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "is_pro_active")
    val isProActive: Boolean,
    
    @ColumnInfo(name = "expiry_timestamp")
    val expiryTimestamp: Long?,
    
    @ColumnInfo(name = "purchase_token")
    val purchaseToken: String?,
    
    @ColumnInfo(name = "last_checked_timestamp")
    val lastCheckedTimestamp: Long
)
