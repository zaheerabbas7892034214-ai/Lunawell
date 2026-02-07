package com.zaheer.lunawell.presentation.pregnancy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.zaheer.lunawell.domain.model.Appointment
import com.zaheer.lunawell.domain.model.Symptom
import com.zaheer.lunawell.presentation.components.ErrorState
import com.zaheer.lunawell.presentation.components.LoadingState
import com.zaheer.lunawell.presentation.components.LunaButton
import com.zaheer.lunawell.presentation.components.LunaCard
import com.zaheer.lunawell.presentation.components.ProFeatureLock
import com.zaheer.lunawell.presentation.navigation.Route
import com.zaheer.lunawell.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyLogsScreen(
    navController: NavHostController,
    viewModel: PregnancyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pregnancy Logs") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.fillMaxSize())
            }
            uiState.error != null -> {
                ErrorState(
                    message = uiState.error ?: "Unknown error",
                    onRetry = { },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        AppointmentsSection(
                            appointments = uiState.appointments
                        )
                    }

                    item {
                        WeightHistorySection(
                            weightHistory = uiState.weightHistory
                        )
                    }

                    item {
                        SymptomsSection(
                            symptoms = uiState.symptoms
                        )
                    }

                    item {
                        ExportSection(
                            isProActive = uiState.isProActive,
                            onExportClick = { },
                            onUpgradeClick = { navController.navigate(Route.Paywall.route) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentsSection(
    appointments: List<Appointment>
) {
    LunaCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Appointments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (appointments.isEmpty()) {
                Text(
                    text = "No appointments scheduled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    appointments.take(5).forEach { appointment ->
                        AppointmentItem(appointment)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentItem(appointment: Appointment) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = appointment.doctorName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = dateFormat.format(Date(appointment.date)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        appointment.notes?.let { notes ->
            if (notes.isNotEmpty()) {
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Divider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun WeightHistorySection(
    weightHistory: List<Pair<Long, Float>>
) {
    LunaCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Weight History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (weightHistory.isEmpty()) {
                Text(
                    text = "No weight data recorded",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    weightHistory.take(10).forEach { (date, weight) ->
                        WeightItem(date, weight)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightItem(date: Long, weight: Float) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateFormat.format(Date(date)),
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = String.format("%.1f kg", weight),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SymptomsSection(
    symptoms: List<Symptom>
) {
    LunaCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Symptom History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (symptoms.isEmpty()) {
                Text(
                    text = "No symptoms recorded",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    symptoms.take(10).forEach { symptom ->
                        SymptomItem(symptom)
                    }
                }
            }
        }
    }
}

@Composable
private fun SymptomItem(symptom: Symptom) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = symptom.symptomType,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "Severity: ${symptom.severity}/10",
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    symptom.severity >= 7 -> MaterialTheme.colorScheme.error
                    symptom.severity >= 4 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
            )
        }
        
        Text(
            text = dateFormat.format(Date(symptom.date)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        symptom.notes?.let { notes ->
            if (notes.isNotEmpty()) {
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Divider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun ExportSection(
    isProActive: Boolean,
    onExportClick: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    if (isProActive) {
        LunaCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Export Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Export your pregnancy logs to PDF",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LunaButton(
                    text = "Export to PDF",
                    onClick = onExportClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        ProFeatureLock(
            featureName = "Export Pregnancy Logs",
            onUpgradeClick = onUpgradeClick
        )
    }
}
