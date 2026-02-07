package com.zaheer.lunawell.data.local.dao

import androidx.room.*
import com.zaheer.lunawell.data.local.entity.EntitlementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntitlementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntitlement(entitlement: EntitlementEntity)

    @Query("SELECT * FROM entitlements ORDER BY id DESC LIMIT 1")
    fun getEntitlement(): Flow<EntitlementEntity?>

    @Query("DELETE FROM entitlements")
    suspend fun clearEntitlements()
    
    @Query("SELECT * FROM entitlements")
    suspend fun getAllEntitlementsOnce(): List<EntitlementEntity>
    
    @Insert
    suspend fun insertEntitlements(entitlements: List<EntitlementEntity>)
}
