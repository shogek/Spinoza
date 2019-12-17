package com.shogek.spinoza.services

import android.app.PendingIntent
import android.content.ContentResolver
import android.telephony.SmsManager
import com.shogek.spinoza.models.Message
import com.shogek.spinoza.repositories.MessageRepository

object MessageService {

    fun send(recipient: String,
             message: String,
             sentIntent: PendingIntent? = null,
             deliveryIntent: PendingIntent? = null
    ) {
        SmsManager
            .getDefault()
            .sendTextMessage(recipient, null, message, sentIntent, deliveryIntent)
    }

    fun delete(resolver: ContentResolver, message: Message) {
        MessageRepository.delete(resolver, message.id)
    }
}