package com.zaheer.lunawell.presentation.breasthealth

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zaheer.lunawell.billing.BillingManager
import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.data.local.LunaWellDatabase
import com.zaheer.lunawell.data.repository.BreastHealthRepository
import com.zaheer.lunawell.data.repository.ProfileRepository

class BreastHealthViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BreastHealthViewModel::class.java)) {
            val database = LunaWellDatabase.getInstance(application)
            val breastHealthRepository = BreastHealthRepository(database.breastLogDao())
            val profileRepository = ProfileRepository(database.profileDao())
            val preferencesManager = PreferencesManager(application)
            val entitlementRepository = com.zaheer.lunawell.data.repository.EntitlementRepository(database.entitlementDao())
            val billingManager = BillingManager(application, entitlementRepository)
            
            return BreastHealthViewModel(
                breastHealthRepository = breastHealthRepository,
                profileRepository = profileRepository,
                billingManager = billingManager,
                preferencesManager = preferencesManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
