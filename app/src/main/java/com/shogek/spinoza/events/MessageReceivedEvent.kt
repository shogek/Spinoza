package com.shogek.spinoza.events

import com.shogek.spinoza.models.Message

data class MessageReceivedEvent(
    val conversationId: Number,
    val message: Message
)