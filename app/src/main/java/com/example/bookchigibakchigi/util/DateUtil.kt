package com.example.bookchigibakchigi.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtil {
    fun getFormattedToday(): String {
        val dateFormat = SimpleDateFormat("yyyy. MM. dd", Locale.KOREA)
        return dateFormat.format(Date())
    }
} 