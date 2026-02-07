package com.zaheer.lunawell.domain.usecase

import com.zaheer.lunawell.data.repository.EntitlementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ValidateSubscriptionUseCase(
    private val entitlementRepository: EntitlementRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return entitlementRepository.getEntitlement().map { entitlement ->
            entitlement?.let {
                val isActive = it.isProActive
                val isNotExpired = it.expiryTimestamp == null || 
                    it.expiryTimestamp > System.currentTimeMillis()
                isActive && isNotExpired
            } ?: false
        }
    }
}
