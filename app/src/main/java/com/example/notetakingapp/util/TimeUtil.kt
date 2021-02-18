package com.example.notetakingapp.util

import java.time.Instant
import java.time.format.DateTimeFormatter

object TimeUtil {

    fun getCurrentTime(): Long {
        return System.currentTimeMillis()
    }

}