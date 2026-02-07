package com.zaheer.lunawell.billing

sealed class SubscriptionStatus {
    data class Active(val expiryDate: Long) : SubscriptionStatus()
    data object Pending : SubscriptionStatus()
    data class Expired(val expiredDate: Long) : SubscriptionStatus()
    data object None : SubscriptionStatus()
}
