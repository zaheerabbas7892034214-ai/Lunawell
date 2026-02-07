package com.zaheer.lunawell.data.repository

import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.data.local.dao.EntitlementDao
import com.zaheer.lunawell.data.local.entity.EntitlementEntity
import kotlinx.coroutines.flow.Flow

class EntitlementRepository(
    private val entitlementDao: EntitlementDao,
    private val preferencesManager: PreferencesManager
) {
    
    suspend fun saveEntitlement(isProActive: Boolean, expiryTimestamp: Long?, purchaseToken: String?) {
        val entitlement = EntitlementEntity(
            isProActive = isProActive,
            expiryTimestamp = expiryTimestamp,
            purchaseToken = purchaseToken,
            lastCheckedTimestamp = System.currentTimeMillis()
        )
        entitlementDao.insertEntitlement(entitlement)
        preferencesManager.saveIsProActive(isProActive)
    }
    
    fun getEntitlement(): Flow<EntitlementEntity?> {
        return entitlementDao.getEntitlement()
    }
    
    suspend fun clearEntitlements() {
        entitlementDao.clearEntitlements()
        preferencesManager.saveIsProActive(false)
    }
}
