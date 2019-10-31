package com.shogek.spinoza

import java.time.LocalDateTime

class DummyConversation(
    val sender: String,
    val message: String,
    val isMyMessage: Boolean,
    val seen: Boolean,
    val date: LocalDateTime,
    val image: Number?
) {
}