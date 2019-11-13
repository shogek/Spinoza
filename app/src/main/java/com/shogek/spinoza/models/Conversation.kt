package com.shogek.spinoza.models

class Conversation(
    val threadId: Number,
    val senderPhone: String,
    var contact: Contact?,
    var messages: Array<Message>?,
    var message: String,
    val dateTimestamp: Long,
    var wasSeen: Boolean,
    var isOurs: Boolean
    ) {
    fun getDisplayName(): String = contact?.displayName ?: senderPhone
}