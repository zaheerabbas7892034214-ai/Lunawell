package com.zaheer.lunawell.presentation.reminders

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zaheer.lunawell.billing.BillingManager
import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.data.local.LunaWellDatabase
import com.zaheer.lunawell.data.repository.ProfileRepository
import com.zaheer.lunawell.data.repository.ReminderRepository

class RemindersViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RemindersViewModel::class.java)) {
            val database = LunaWellDatabase.getInstance(application)
            val reminderRepository = ReminderRepository(database.reminderDao())
            val profileRepository = ProfileRepository(database.profileDao())
            val preferencesManager = PreferencesManager(application)
            val entitlementRepository = com.zaheer.lunawell.data.repository.EntitlementRepository(database.entitlementDao())
            val billingManager = BillingManager(application, entitlementRepository)
            
            return RemindersViewModel(
                reminderRepository = reminderRepository,
                profileRepository = profileRepository,
                billingManager = billingManager,
                preferencesManager = preferencesManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
