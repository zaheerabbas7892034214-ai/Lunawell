package com.zaheer.lunawell.utils

import com.zaheer.lunawell.data.local.entity.CycleEntity
import com.zaheer.lunawell.utils.DateUtils.addDays
import com.zaheer.lunawell.utils.DateUtils.getDaysBetween
import com.zaheer.lunawell.utils.DateUtils.getCurrentTimestamp
import kotlin.math.roundToInt

object CycleCalculator {
    
    fun calculateCycleLength(startDate: Long, endDate: Long): Int {
        return getDaysBetween(startDate, endDate)
    }
    
    fun calculateAverageCycleLength(cycles: List<CycleEntity>): Float {
        if (cycles.isEmpty()) return 28f
        
        val totalLength = cycles.sumOf { it.cycleLength }
        return totalLength.toFloat() / cycles.size
    }
    
    fun predictNextPeriod(lastPeriodStart: Long, averageCycleLength: Float): Long {
        return addDays(lastPeriodStart, averageCycleLength.roundToInt())
    }
    
    fun calculateFertileWindow(lastPeriodStart: Long, averageCycleLength: Float): Pair<Long, Long> {
        val nextPeriodStart = predictNextPeriod(lastPeriodStart, averageCycleLength)
        val ovulationDay = addDays(nextPeriodStart, -14)
        
        val fertileWindowStart = addDays(ovulationDay, -5)
        val fertileWindowEnd = addDays(ovulationDay, 1)
        
        return Pair(fertileWindowStart, fertileWindowEnd)
    }
    
    fun getCurrentCycleDay(lastPeriodStart: Long, today: Long = getCurrentTimestamp()): Int {
        return getDaysBetween(lastPeriodStart, today) + 1
    }
    
    fun isInFertileWindow(date: Long, lastPeriodStart: Long, averageCycleLength: Float): Boolean {
        val (fertileStart, fertileEnd) = calculateFertileWindow(lastPeriodStart, averageCycleLength)
        return date >= fertileStart && date <= fertileEnd
    }
}
