package com.zaheer.lunawell.presentation.pregnancy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.zaheer.lunawell.presentation.components.LunaButton
import com.zaheer.lunawell.presentation.components.LunaCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractionTimerScreen(
    navController: NavHostController,
    viewModel: PregnancyViewModel = viewModel()
) {
    val contractionState by viewModel.contractionState.collectAsState()
    
    var currentDuration by remember { mutableStateOf(0L) }
    
    LaunchedEffect(contractionState.isTimingContraction, contractionState.currentContractionStart) {
        if (contractionState.isTimingContraction && contractionState.currentContractionStart != null) {
            while (contractionState.isTimingContraction) {
                currentDuration = System.currentTimeMillis() - contractionState.currentContractionStart!!
                kotlinx.coroutines.delay(100)
            }
        } else {
            currentDuration = 0L
        }
    }

    val averageInterval = viewModel.getAverageContractionInterval()
    val showWarning = averageInterval != null && averageInterval < 5 * 60 * 1000 && contractionState.contractions.size >= 3

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contraction Timer") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (contractionState.contractions.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearContractions() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear All")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TimerCard(
                    isTimingContraction = contractionState.isTimingContraction,
                    currentDuration = currentDuration,
                    onStartStop = {
                        if (contractionState.isTimingContraction) {
                            viewModel.stopContraction()
                        } else {
                            viewModel.startContraction()
                        }
                    }
                )
            }

            if (showWarning) {
                item {
                    WarningCard()
                }
            }

            if (contractionState.contractions.isNotEmpty()) {
                item {
                    StatsCard(
                        contractions = contractionState.contractions,
                        averageInterval = averageInterval
                    )
                }

                item {
                    ContractionsListSection(
                        contractions = contractionState.contractions
                    )
                }
            } else {
                item {
                    InfoCard()
                }
            }
        }
    }
}

@Composable
private fun TimerCard(
    isTimingContraction: Boolean,
    currentDuration: Long,
    onStartStop: () -> Unit
) {
    LunaCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isTimingContraction) "Timing Contraction" else "Ready to Time",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = formatDuration(currentDuration),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (isTimingContraction) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            LunaButton(
                text = if (isTimingContraction) "Stop Contraction" else "Start Contraction",
                onClick = onStartStop,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WarningCard() {
    LunaCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "Frequent Contractions Detected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Your contractions are less than 5 minutes apart. " +
                            "Contact your healthcare provider or go to the hospital if you're at full term.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun StatsCard(
    contractions: List<Contraction>,
    averageInterval: Long?
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Total",
                    value = contractions.size.toString()
                )
                
                StatItem(
                    label = "Avg Duration",
                    value = formatDuration(contractions.map { it.duration }.average().toLong())
                )
                
                if (averageInterval != null) {
                    StatItem(
                        label = "Avg Interval",
                        value = formatDuration(averageInterval)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContractionsListSection(
    contractions: List<Contraction>
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Contractions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                contractions.forEachIndexed { index, contraction ->
                    val interval = if (index < contractions.size - 1) {
                        contractions[index + 1].startTime - contraction.endTime
                    } else null
                    
                    ContractionItem(
                        contraction = contraction,
                        interval = interval,
                        number = contractions.size - index
                    )
                }
            }
        }
    }
}

@Composable
private fun ContractionItem(
    contraction: Contraction,
    interval: Long?,
    number: Int
) {
    val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "#$number",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = timeFormat.format(Date(contraction.startTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Duration: ${contraction.durationSeconds}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                interval?.let {
                    Text(
                        text = "Interval: ${formatDuration(it)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Divider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun InfoCard() {
    LunaCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "How to Use",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "• Press 'Start Contraction' when a contraction begins\n" +
                        "• Press 'Stop Contraction' when it ends\n" +
                        "• Track multiple contractions to see patterns\n" +
                        "• Contact your provider if contractions are regular and < 5 min apart\n" +
                        "• If at full term and contractions are strong and regular, go to hospital",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatDuration(milliseconds: Long): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    val hours = (milliseconds / (1000 * 60 * 60))
    
    return when {
        hours > 0 -> String.format("%dh %dm", hours, minutes)
        minutes > 0 -> String.format("%dm %ds", minutes, seconds)
        else -> String.format("%ds", seconds)
    }
}
