package com.shogek.spinoza.utils

object PhoneUtils {
    private val regex: Regex = "[\\s()]".toRegex() // "784 (54) " -> "78454"

    /** Removes spaces and parentheses. */
    fun getStrippedPhone(phone: String): String = phone.replace(this.regex, "")
}