package com.shogek.spinoza.models

class Conversation(
    val threadId: Number,
    val senderPhone: String,
    var contact: Contact?,
    var messages: MutableList<Message>?,
    var latestMessageText: String,
    var latestMessageTimestamp: Long,
    var wasRead: Boolean,
    var isOurs: Boolean
    ) {
    fun getDisplayName(): String = contact?.displayName ?: senderPhone
}