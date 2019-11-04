package com.shogek.spinoza.models

import java.time.LocalDateTime

class Conversation(
    val sender: String,
    val message: String,
    val isMyMessage: Boolean,
    val seen: Boolean,
    val date: LocalDateTime,
    val image: Number?
) {
}