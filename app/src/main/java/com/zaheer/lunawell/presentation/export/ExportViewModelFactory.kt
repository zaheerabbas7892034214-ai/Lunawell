package com.zaheer.lunawell.presentation.export

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zaheer.lunawell.billing.BillingManager
import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.data.local.LunaWellDatabase
import com.zaheer.lunawell.data.repository.*
import com.zaheer.lunawell.export.BackupExporter
import com.zaheer.lunawell.export.BackupImporter
import com.zaheer.lunawell.export.PDFExporter
import com.zaheer.lunawell.security.EncryptionManager

class ExportViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExportViewModel::class.java)) {
            val database = LunaWellDatabase.getInstance(application)
            val profileRepository = ProfileRepository(database.profileDao())
            val cycleRepository = CycleRepository(database.cycleDao())
            val symptomLogRepository = SymptomLogRepository(database.symptomLogDao())
            val pregnancyRepository = PregnancyRepository(database.pregnancyLogDao())
            val breastHealthRepository = BreastHealthRepository(database.breastLogDao())
            val appointmentRepository = AppointmentRepository(database.appointmentDao())
            val preferencesManager = PreferencesManager(application)
            val entitlementRepository = EntitlementRepository(database.entitlementDao())
            val billingManager = BillingManager(application, entitlementRepository)
            val encryptionManager = EncryptionManager(application)
            
            val pdfExporter = PDFExporter(
                context = application,
                profileRepository = profileRepository,
                cycleRepository = cycleRepository,
                symptomLogRepository = symptomLogRepository,
                pregnancyRepository = pregnancyRepository,
                breastHealthRepository = breastHealthRepository,
                appointmentRepository = appointmentRepository
            )
            
            val backupExporter = BackupExporter(
                context = application,
                database = database,
                encryptionManager = encryptionManager
            )
            
            val backupImporter = BackupImporter(
                context = application,
                database = database,
                encryptionManager = encryptionManager
            )
            
            return ExportViewModel(
                pdfExporter = pdfExporter,
                backupExporter = backupExporter,
                backupImporter = backupImporter,
                profileRepository = profileRepository,
                billingManager = billingManager,
                preferencesManager = preferencesManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
