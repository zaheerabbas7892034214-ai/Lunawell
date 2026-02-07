package com.zaheer.lunawell.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zaheer.lunawell.presentation.home.BottomNavigationBar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.selectedDate) {
        uiState.selectedDate?.let { date ->
            navController.navigate("log/$date")
            viewModel.selectDate(0) // Reset selection
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "calendar",
                onNavigate = { route -> 
                    if (route != "calendar") navController.navigate(route) 
                }
            )
        }
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
                // Month selector
                MonthSelector(
                    month = uiState.currentMonth,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() }
                )
                
                // Legend
                CalendarLegend()
                
                // Week day headers
                WeekDayHeaders()
                
                // Calendar grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.days) { dayInfo ->
                        CalendarDay(
                            dayInfo = dayInfo,
                            onClick = { 
                                if (dayInfo.date > 0) {
                                    viewModel.selectDate(dayInfo.date)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthSelector(
    month: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
            }
            
            Text(
                text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(month.time),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
            }
        }
    }
}

@Composable
fun CalendarLegend() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(
                color = MaterialTheme.colorScheme.error,
                label = "Period"
            )
            LegendItem(
                color = MaterialTheme.colorScheme.tertiary,
                label = "Fertile"
            )
            LegendItem(
                color = MaterialTheme.colorScheme.errorContainer,
                label = "Predicted"
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = MaterialTheme.shapes.small)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun WeekDayHeaders() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CalendarDay(
    dayInfo: CalendarDayInfo,
    onClick: () -> Unit
) {
    if (dayInfo.date == 0L) {
        Box(modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp))
        return
    }

    val calendar = Calendar.getInstance().apply { timeInMillis = dayInfo.date }
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    
    val backgroundColor = when {
        dayInfo.isPeriodDay -> MaterialTheme.colorScheme.error
        dayInfo.isPredictedPeriod -> MaterialTheme.colorScheme.errorContainer
        dayInfo.isFertileDay -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
        else -> Color.Transparent
    }
    
    val textColor = when {
        dayInfo.isPeriodDay -> MaterialTheme.colorScheme.onError
        dayInfo.isPredictedPeriod -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(backgroundColor, shape = MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = if (dayInfo.isPeriodDay || dayInfo.isPredictedPeriod) 
                    FontWeight.Bold 
                else 
                    FontWeight.Normal
            )
            
            if (dayInfo.hasSymptoms) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
