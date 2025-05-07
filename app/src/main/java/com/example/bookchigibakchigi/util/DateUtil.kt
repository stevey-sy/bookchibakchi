package com.example.bookchigibakchigi.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtil {
    fun getFormattedToday(): String {
        val dateFormat = SimpleDateFormat("yyyy. MM. dd", Locale.KOREA)
        return dateFormat.format(Date())
    }

    fun formatDateFromMillis(timeMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyyy. MM. dd", Locale.KOREA)
        return dateFormat.format(timeMillis)
    }

    // getMillisFromDate
    fun getMillisFromDate(dateString: String): Long {
        val dateFormat = SimpleDateFormat("yyyy. MM. dd", Locale.KOREA)
        val date = dateFormat.parse(dateString)
        return date?.time ?: 0L
    }
} 