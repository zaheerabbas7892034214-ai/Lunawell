package com.zaheer.lunawell.presentation.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    navController: NavController,
    viewModel: LogViewModel,
    dateMillis: Long? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(dateMillis) {
        dateMillis?.let {
            viewModel.setSelectedDate(it)
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            navController.popBackStack()
            viewModel.resetSaveState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            .format(Date(uiState.selectedDate))
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp)
                        )
                    } else {
                        IconButton(onClick = { viewModel.saveLog() }) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row
            TabRow(selectedTabIndex = uiState.currentTab) {
                Tab(
                    selected = uiState.currentTab == 0,
                    onClick = { viewModel.setTab(0) },
                    text = { Text("Period") }
                )
                Tab(
                    selected = uiState.currentTab == 1,
                    onClick = { viewModel.setTab(1) },
                    text = { Text("Symptoms") }
                )
                Tab(
                    selected = uiState.currentTab == 2,
                    onClick = { viewModel.setTab(2) },
                    text = { Text("Mood & Sleep") }
                )
                Tab(
                    selected = uiState.currentTab == 3,
                    onClick = { viewModel.setTab(3) },
                    text = { Text("Wellness") }
                )
            }

            // Tab content
            when (uiState.currentTab) {
                0 -> PeriodTab(uiState, viewModel)
                1 -> SymptomsTab(uiState, viewModel)
                2 -> MoodSleepTab(uiState, viewModel)
                3 -> WellnessTab(uiState, viewModel)
            }
        }
    }
}

@Composable
fun PeriodTab(uiState: LogUiState, viewModel: LogViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Flow Intensity",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FlowIntensityButton(
            label = "Light",
            icon = "💧",
            isSelected = uiState.flowIntensity == "Light",
            onClick = { viewModel.setFlowIntensity("Light") }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        FlowIntensityButton(
            label = "Medium",
            icon = "💧💧",
            isSelected = uiState.flowIntensity == "Medium",
            onClick = { viewModel.setFlowIntensity("Medium") }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        FlowIntensityButton(
            label = "Heavy",
            icon = "💧💧💧",
            isSelected = uiState.flowIntensity == "Heavy",
            onClick = { viewModel.setFlowIntensity("Heavy") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (uiState.flowIntensity != null) {
            Button(
                onClick = { viewModel.setFlowIntensity("") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear")
            }
        }
    }
}

@Composable
fun FlowIntensityButton(
    label: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SymptomsTab(uiState: LogUiState, viewModel: LogViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Symptoms",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val symptoms = listOf("Cramps", "Headache", "Acne", "Bloating", "Nausea", "Fatigue", "Mood Swings", "Breast Tenderness")
        
        symptoms.forEach { symptom ->
            SymptomItem(
                symptom = symptom,
                severity = uiState.selectedSymptoms[symptom] ?: 0,
                onSeverityChange = { severity ->
                    viewModel.setSymptomSeverity(symptom, severity)
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = uiState.symptomNotes,
            onValueChange = { viewModel.setSymptomNotes(it) },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )
    }
}

@Composable
fun SymptomItem(
    symptom: String,
    severity: Int,
    onSeverityChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = symptom,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (severity > 0) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = if (severity > 0) "$severity/5" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Slider(
            value = severity.toFloat(),
            onValueChange = { onSeverityChange(it.toInt()) },
            valueRange = 0f..5f,
            steps = 4,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun MoodSleepTab(uiState: LogUiState, viewModel: LogViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Mood",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("😢" to 1, "😕" to 2, "😐" to 3, "🙂" to 4, "😄" to 5).forEach { (emoji, value) ->
                Card(
                    onClick = { viewModel.setMood(value) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.mood == value)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Sleep",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hours: ${uiState.sleep?.toString() ?: "0.0"}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            
            OutlinedTextField(
                value = uiState.sleep?.toString() ?: "",
                onValueChange = { 
                    it.toFloatOrNull()?.let { hours ->
                        if (hours in 0f..24f) {
                            viewModel.setSleep(hours)
                        }
                    }
                },
                label = { Text("Hours") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Slider(
            value = uiState.sleep ?: 0f,
            onValueChange = { viewModel.setSleep(it) },
            valueRange = 0f..12f,
            steps = 23,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun WellnessTab(uiState: LogUiState, viewModel: LogViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Hydration",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${uiState.hydration} glasses",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Row {
                IconButton(
                    onClick = { 
                        if (uiState.hydration > 0) {
                            viewModel.setHydration(uiState.hydration - 1)
                        }
                    }
                ) {
                    Text("-", style = MaterialTheme.typography.headlineSmall)
                }
                
                IconButton(
                    onClick = { viewModel.setHydration(uiState.hydration + 1) }
                ) {
                    Text("+", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Visual representation of glasses
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(minOf(uiState.hydration, 10)) {
                Text("💧", style = MaterialTheme.typography.bodyLarge)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Exercise",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = uiState.exercise.toString(),
            onValueChange = { 
                it.toIntOrNull()?.let { minutes ->
                    if (minutes >= 0) {
                        viewModel.setExercise(minutes)
                    }
                }
            },
            label = { Text("Minutes") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Slider(
            value = uiState.exercise.toFloat(),
            onValueChange = { viewModel.setExercise(it.toInt()) },
            valueRange = 0f..120f,
            steps = 23,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "${uiState.exercise} minutes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
