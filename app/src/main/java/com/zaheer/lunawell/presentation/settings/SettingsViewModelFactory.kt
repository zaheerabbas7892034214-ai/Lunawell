package com.zaheer.lunawell.presentation.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zaheer.lunawell.billing.BillingManager
import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.data.local.LunaWellDatabase
import com.zaheer.lunawell.data.repository.EntitlementRepository
import com.zaheer.lunawell.data.repository.ProfileRepository

class SettingsViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            val database = LunaWellDatabase.getInstance(application)
            val preferencesManager = PreferencesManager(application)
            val entitlementRepository = EntitlementRepository(
                database.entitlementDao(),
                preferencesManager
            )
            val billingManager = BillingManager(application, entitlementRepository)
            val profileRepository = ProfileRepository(database.profileDao())
            
            return SettingsViewModel(
                billingManager = billingManager,
                preferencesManager = preferencesManager,
                profileRepository = profileRepository,
                database = database
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
