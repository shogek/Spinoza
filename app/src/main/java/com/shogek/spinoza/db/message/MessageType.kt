package com.shogek.spinoza.db.message

enum class MessageType {

    SENDING,
    FAILED_TO_SEND,
    SENT;

    companion object {
        fun MessageType.toInt(): Int {
            return when (this) {
                SENDING ->        0
                FAILED_TO_SEND -> 1
                SENT ->           2
                else -> throw IllegalArgumentException("No possible mapping found!")
            }
        }
    }
}