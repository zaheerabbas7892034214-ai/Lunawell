package com.zaheer.lunawell.utils

import com.zaheer.lunawell.utils.DateUtils.addDays
import com.zaheer.lunawell.utils.DateUtils.getDaysBetween
import com.zaheer.lunawell.utils.DateUtils.getCurrentTimestamp

object PregnancyCalculator {
    
    private const val PREGNANCY_DURATION_DAYS = 280
    
    fun calculateWeekAndDay(lmpDate: Long, currentDate: Long = getCurrentTimestamp()): Pair<Int, Int> {
        val totalDays = getDaysBetween(lmpDate, currentDate)
        val weeks = totalDays / 7
        val days = totalDays % 7
        return Pair(weeks, days)
    }
    
    fun calculateWeekAndDayFromDueDate(dueDate: Long, currentDate: Long = getCurrentTimestamp()): Pair<Int, Int> {
        val lmpDate = calculateLMP(dueDate)
        return calculateWeekAndDay(lmpDate, currentDate)
    }
    
    fun calculateDueDate(lmpDate: Long): Long {
        return addDays(lmpDate, PREGNANCY_DURATION_DAYS)
    }
    
    fun calculateLMP(dueDate: Long): Long {
        return addDays(dueDate, -PREGNANCY_DURATION_DAYS)
    }
    
    fun getTrimester(week: Int): Int {
        return when {
            week <= 13 -> 1
            week <= 27 -> 2
            else -> 3
        }
    }
    
    fun getDaysUntilDueDate(dueDate: Long, currentDate: Long = getCurrentTimestamp()): Int {
        return getDaysBetween(currentDate, dueDate)
    }
}
