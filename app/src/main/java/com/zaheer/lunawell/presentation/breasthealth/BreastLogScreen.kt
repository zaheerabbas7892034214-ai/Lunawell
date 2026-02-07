package com.zaheer.lunawell.presentation.breasthealth

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zaheer.lunawell.presentation.components.LunaButton
import com.zaheer.lunawell.presentation.components.LunaCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreastLogScreen(
    navController: NavController,
    viewModel: BreastHealthViewModel
) {
    val formState by viewModel.formState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.updateImageUri(uri)
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Breast health log saved successfully")
            viewModel.clearSaveSuccess()
            navController.navigateUp()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Breast Exam") },
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.resetForm()
                        navController.navigateUp() 
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Date Selector
            LunaCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = formState.date
                    
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val newCalendar = Calendar.getInstance()
                            newCalendar.set(year, month, dayOfMonth)
                            viewModel.updateFormDate(newCalendar.timeInMillis)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dateFormat.format(Date(formState.date)),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Symptoms",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lump Found
            SymptomCheckbox(
                label = "Lump Found",
                checked = formState.lumpFound,
                onCheckedChange = { viewModel.updateLumpFound(it) },
                severity = formState.lumpSeverity,
                onSeverityChange = { viewModel.updateLumpSeverity(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pain
            SymptomCheckbox(
                label = "Pain",
                checked = formState.pain,
                onCheckedChange = { viewModel.updatePain(it) },
                severity = formState.painSeverity,
                onSeverityChange = { viewModel.updatePainSeverity(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Discharge
            SymptomCheckbox(
                label = "Discharge",
                checked = formState.discharge,
                onCheckedChange = { viewModel.updateDischarge(it) },
                severity = formState.dischargeSeverity,
                onSeverityChange = { viewModel.updateDischargeSeverity(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Skin Changes
            SymptomCheckbox(
                label = "Skin Changes",
                checked = formState.skinChanges,
                onCheckedChange = { viewModel.updateSkinChanges(it) },
                severity = formState.skinChangesSeverity,
                onSeverityChange = { viewModel.updateSkinChangesSeverity(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Other
            SymptomCheckbox(
                label = "Other",
                checked = formState.other,
                onCheckedChange = { viewModel.updateOther(it) },
                severity = formState.otherSeverity,
                onSeverityChange = { viewModel.updateOtherSeverity(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notes
            OutlinedTextField(
                value = formState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Notes (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Attach Image Button
            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (formState.imageUri != null) Icons.Default.CheckCircle else Icons.Default.Image,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (formState.imageUri != null) "Image Attached" else "Attach Image (Optional)"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            LunaButton(
                text = "Save Exam Log",
                onClick = { viewModel.saveBreastLog() }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SymptomCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    severity: Int,
    onSeverityChange: (Int) -> Unit
) {
    LunaCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge
                )
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            }

            if (checked) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Severity: $severity / 5",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Slider(
                    value = severity.toFloat(),
                    onValueChange = { onSeverityChange(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
