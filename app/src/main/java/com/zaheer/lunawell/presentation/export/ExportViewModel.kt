package com.zaheer.lunawell.presentation.export

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.lunawell.billing.BillingManager
import com.zaheer.lunawell.data.datastore.PreferencesManager
import com.zaheer.lunawell.data.repository.ProfileRepository
import com.zaheer.lunawell.export.BackupExporter
import com.zaheer.lunawell.export.BackupImporter
import com.zaheer.lunawell.export.PDFExporter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ExportUiState(
    val exportHistory: List<ExportHistoryItem> = emptyList(),
    val isPro: Boolean = false,
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

data class ExportHistoryItem(
    val type: String,
    val timestamp: Long,
    val fileName: String
)

class ExportViewModel(
    private val pdfExporter: PDFExporter,
    private val backupExporter: BackupExporter,
    private val backupImporter: BackupImporter,
    private val profileRepository: ProfileRepository,
    private val billingManager: BillingManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    private val exportHistory = mutableListOf<ExportHistoryItem>()

    init {
        loadExportState()
    }

    private fun loadExportState() {
        viewModelScope.launch {
            try {
                combine(
                    billingManager.isPro,
                    preferencesManager.getIsProActive()
                ) { isPro, isProPref ->
                    isPro || isProPref
                }.collect { isPro ->
                    _uiState.value = ExportUiState(
                        exportHistory = exportHistory.takeLast(10),
                        isPro = isPro,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ExportUiState(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun exportPDF(outputUri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isExporting = true, error = null)

                val profile = profileRepository.getActiveProfile().first()
                if (profile == null) {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        error = "No active profile found"
                    )
                    return@launch
                }

                val success = pdfExporter.exportHealthSummary(profile.id, outputUri)
                
                if (success) {
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                    val timestamp = System.currentTimeMillis()
                    exportHistory.add(
                        ExportHistoryItem(
                            type = "PDF Export",
                            timestamp = timestamp,
                            fileName = "lunawell_summary_${dateFormat.format(Date(timestamp))}.pdf"
                        )
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportHistory = exportHistory.takeLast(10),
                        successMessage = "PDF exported successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        error = "Failed to export PDF"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = e.message ?: "Failed to export PDF"
                )
            }
        }
    }

    fun exportBackup(outputUri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isExporting = true, error = null)

                val success = backupExporter.exportBackup(outputUri)
                
                if (success) {
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                    val timestamp = System.currentTimeMillis()
                    exportHistory.add(
                        ExportHistoryItem(
                            type = "Encrypted Backup",
                            timestamp = timestamp,
                            fileName = "lunawell_backup_${dateFormat.format(Date(timestamp))}.lwb"
                        )
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportHistory = exportHistory.takeLast(10),
                        successMessage = "Backup exported successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        error = "Failed to export backup"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = e.message ?: "Failed to export backup"
                )
            }
        }
    }

    fun importBackup(inputUri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isImporting = true, error = null)

                val success = backupImporter.importBackup(inputUri)
                
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        successMessage = "Backup imported successfully. Please restart the app."
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        error = "Failed to import backup. The file may be corrupted or incompatible."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = e.message ?: "Failed to import backup"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
