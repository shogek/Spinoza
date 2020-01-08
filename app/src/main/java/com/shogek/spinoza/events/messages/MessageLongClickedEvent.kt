package com.shogek.spinoza.events.messages

import com.shogek.spinoza.models.Message

data class MessageLongClickedEvent(
    val message: Message?
)