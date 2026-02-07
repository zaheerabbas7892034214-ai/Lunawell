package com.zaheer.lunawell.presentation.applock

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.security.AppBiometricManager

class AppLockViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppLockViewModel::class.java)) {
            val preferencesManager = PreferencesManager(application)
            val biometricManager = AppBiometricManager(application)
            
            return AppLockViewModel(
                preferencesManager = preferencesManager,
                biometricManager = biometricManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
