package com.zaheer.lunawell.presentation.reminders

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.zaheer.lunawell.domain.model.Reminder
import com.zaheer.lunawell.presentation.components.LunaButton
import com.zaheer.lunawell.presentation.components.LunaCard
import com.zaheer.lunawell.presentation.components.ProBadge
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    navController: NavController,
    viewModel: RemindersViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.isPro) {
                        viewModel.showAddDialog()
                    } else {
                        // Check if trying to add custom reminder (not built-in)
                        navController.navigate("paywall")
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.reminders.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No reminders yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap + to add your first reminder",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Built-in reminders section
                        val builtInReminders = uiState.reminders.filter { 
                            it.type == "Period" || it.type == "Self-Exam" 
                        }
                        
                        if (builtInReminders.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Built-in Reminders",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            
                            items(builtInReminders) { reminder ->
                                ReminderItem(
                                    reminder = reminder,
                                    onToggle = { viewModel.toggleReminderEnabled(reminder) },
                                    onEdit = { viewModel.showEditDialog(reminder) },
                                    onDelete = { viewModel.deleteReminder(reminder) }
                                )
                            }
                        }

                        // Custom reminders section
                        val customReminders = uiState.reminders.filter { 
                            it.type != "Period" && it.type != "Self-Exam" 
                        }
                        
                        if (customReminders.isNotEmpty()) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                                ) {
                                    Text(
                                        text = "Custom Reminders",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ProBadge()
                                }
                            }
                            
                            items(customReminders) { reminder ->
                                ReminderItem(
                                    reminder = reminder,
                                    onToggle = { viewModel.toggleReminderEnabled(reminder) },
                                    onEdit = { viewModel.showEditDialog(reminder) },
                                    onDelete = { viewModel.deleteReminder(reminder) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        ReminderDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.hideDialog() }
        )
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LunaCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeFormat.format(Date(reminder.time)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reminder.type,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (reminder.isRecurring) {
                        Text(
                            text = " • Recurring",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggle() }
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Reminder") },
            text = { Text("Are you sure you want to delete this reminder?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ReminderDialog(
    viewModel: RemindersViewModel,
    onDismiss: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    
    val reminderTypes = listOf("Custom", "Medication", "Appointment", "Period", "Self-Exam")
    var expandedTypeDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(if (uiState.editingReminder != null) "Edit Reminder" else "Add Reminder") 
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = formState.title,
                    onValueChange = { viewModel.updateFormTitle(it) },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Time Picker
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = formState.time
                        
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val newCalendar = Calendar.getInstance()
                                newCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                newCalendar.set(Calendar.MINUTE, minute)
                                viewModel.updateFormTime(newCalendar.timeInMillis)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(timeFormat.format(Date(formState.time)))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedTypeDropdown,
                    onExpandedChange = { expandedTypeDropdown = it }
                ) {
                    OutlinedTextField(
                        value = formState.type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTypeDropdown,
                        onDismissRequest = { expandedTypeDropdown = false }
                    ) {
                        reminderTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    viewModel.updateFormType(type)
                                    expandedTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recurring Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recurring")
                    Checkbox(
                        checked = formState.isRecurring,
                        onCheckedChange = { viewModel.updateFormRecurring(it) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.saveReminder() }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
