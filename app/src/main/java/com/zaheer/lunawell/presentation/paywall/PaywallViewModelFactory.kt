package com.zaheer.lunawell.presentation.paywall

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zaheer.lunawell.billing.BillingManager
import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.data.local.LunaWellDatabase
import com.zaheer.lunawell.data.repository.EntitlementRepository

class PaywallViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaywallViewModel::class.java)) {
            val database = LunaWellDatabase.getInstance(application)
            val entitlementRepository = EntitlementRepository(
                database.entitlementDao(),
                PreferencesManager(application)
            )
            val billingManager = BillingManager(application, entitlementRepository)
            val preferencesManager = PreferencesManager(application)
            
            return PaywallViewModel(
                billingManager = billingManager,
                preferencesManager = preferencesManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
