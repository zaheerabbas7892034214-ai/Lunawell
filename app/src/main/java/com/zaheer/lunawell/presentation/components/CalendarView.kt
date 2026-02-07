package com.zaheer.lunawell.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zaheer.lunawell.presentation.theme.FertileGreen
import com.zaheer.lunawell.presentation.theme.FertileGreenLight
import com.zaheer.lunawell.presentation.theme.PeriodRed
import com.zaheer.lunawell.presentation.theme.PeriodRedLight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarView(
    currentMonth: Long,
    periodDays: Set<Long>,
    predictedDays: Set<Long>,
    fertileDays: Set<Long>,
    selectedDay: Long?,
    onDayClick: (Long) -> Unit,
    onMonthChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = currentMonth
        set(Calendar.DAY_OF_MONTH, 1)
    }
    
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val monthTitle = monthFormat.format(calendar.time)
    
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    
    val days = mutableListOf<Int>()
    repeat(firstDayOfWeek) { days.add(0) }
    for (day in 1..daysInMonth) {
        days.add(day)
    }
    
    Column(modifier = modifier) {
        // Month Header with navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val prevMonth = Calendar.getInstance().apply {
                    timeInMillis = currentMonth
                    add(Calendar.MONTH, -1)
                }
                onMonthChange(prevMonth.timeInMillis)
            }) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Month"
                )
            }
            
            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = {
                val nextMonth = Calendar.getInstance().apply {
                    timeInMillis = currentMonth
                    add(Calendar.MONTH, 1)
                }
                onMonthChange(nextMonth.timeInMillis)
            }) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Month"
                )
            }
        }
        
        // Week day labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
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
        
        // Calendar Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(days) { day ->
                if (day > 0) {
                    val dayCalendar = Calendar.getInstance().apply {
                        timeInMillis = currentMonth
                        set(Calendar.DAY_OF_MONTH, day)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val dayMillis = dayCalendar.timeInMillis
                    
                    val isPeriod = periodDays.contains(dayMillis)
                    val isPredicted = predictedDays.contains(dayMillis)
                    val isFertile = fertileDays.contains(dayMillis)
                    val isSelected = selectedDay == dayMillis
                    
                    CalendarDay(
                        day = day,
                        isPeriod = isPeriod,
                        isPredicted = isPredicted,
                        isFertile = isFertile,
                        isSelected = isSelected,
                        onClick = { onDayClick(dayMillis) }
                    )
                } else {
                    Box(modifier = Modifier.aspectRatio(1f))
                }
            }
        }
        
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = PeriodRed, label = "Period")
            LegendItem(color = PeriodRedLight, label = "Predicted")
            LegendItem(color = FertileGreen, label = "Fertile")
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int,
    isPeriod: Boolean,
    isPredicted: Boolean,
    isFertile: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isPeriod -> PeriodRed
        isPredicted -> PeriodRedLight
        isFertile -> FertileGreenLight
        else -> Color.Transparent
    }
    
    val textColor = when {
        isPeriod -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
