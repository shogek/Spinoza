package com.shogek.spinoza.services

import android.content.*
import android.telephony.SmsManager
import com.shogek.spinoza.models.Message
import com.shogek.spinoza.repositories.MessageRepository

object MessageService {

    fun send(
        recipient: String,
        message: String
    ) {
        SmsManager
            .getDefault()
            .sendTextMessage(recipient, null, message, null, null)
    }

    fun delete(resolver: ContentResolver, message: Message) {
        MessageRepository.delete(resolver, message.id)
    }
}