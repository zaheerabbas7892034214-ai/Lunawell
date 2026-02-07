package com.zaheer.lunawell.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    
    fun Long.toFormattedDate(pattern: String = "MMM dd, yyyy"): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(Date(this))
    }
    
    fun Long.toFormattedTime(pattern: String = "hh:mm a"): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(Date(this))
    }
    
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }
    
    fun getDaysBetween(start: Long, end: Long): Int {
        val millisecondsInDay = 24 * 60 * 60 * 1000
        return ((end - start) / millisecondsInDay).toInt()
    }
    
    fun addDays(timestamp: Long, days: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.add(Calendar.DAY_OF_MONTH, days)
        return calendar.timeInMillis
    }
    
    fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}
