package com.shogek.spinoza.utils

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId

object DateUtils {
    fun getUTCLocalDateTime(timestamp: Long): LocalDateTime {
        val instant = Timestamp(timestamp).toInstant()
        return LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))
    }
}