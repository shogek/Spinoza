package com.shogek.spinoza.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    /** Convert from "1571762897000" to "2019-10-22 09:42" */
    fun getDateTime(timestamp: String?): String {
        if (timestamp == null) return ""
        // TODO: Parses hours badly
        val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val netDate = Date(timestamp.toLong())
        return formatter.format(netDate)
    }
}