package com.shogek.spinoza.models

class Message {
    var id: String = ""
    var sender: String = ""
    var senderId: String? = null
    var text: String = ""
    var dateSent: String = ""
    var isRead: Boolean = false
    var isSeen: Boolean = false
    var threadId: String = ""
    var type: MessageType = MessageType.ALL

    /** Indicates whether the message was sent by the user. */
    fun isSentByUs(): Boolean {return this.type == MessageType.SENT}
}

enum class MessageType {
    ALL,
    INBOX,
    SENT,
    DRAFT,
    OUTBOX,
    FAILED,
    QUEUED
}
