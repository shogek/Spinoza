package com.shogek.spinoza.events.messages

import com.shogek.spinoza.models.Message

data class MessageClickedEvent(
    val message: Message?
)