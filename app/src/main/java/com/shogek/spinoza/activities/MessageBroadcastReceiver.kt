package com.shogek.spinoza.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import com.shogek.spinoza.repositories.ContactRepository
import com.shogek.spinoza.repositories.ConversationRepository
import com.shogek.spinoza.helpers.ConversationHelper

class MessageBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            return
        }

        val pdus = (intent.extras?.get("pdus") as Array<*>).filterIsInstance<ByteArray>()
        val format = intent.extras?.get("format") as String?
        if (pdus.isEmpty() || format == null)
            return

        val senderPhone = SmsMessage.createFromPdu(pdus.first(), format).originatingAddress
            ?: return

        val text = pdus.fold("") { acc, bytes -> acc + SmsMessage.createFromPdu(bytes, format).displayMessageBody }

        val tempConversations = ConversationRepository.getAll(context.contentResolver) // load conversations
        val tempContacts = ContactRepository.getAll(context.contentResolver) // load contacts
        ConversationHelper.matchContactsWithConversations(tempConversations, tempContacts.toMutableList())
        val threadId = tempConversations.find { c -> c.contact?.strippedPhone == senderPhone }?.threadId

        MessageReceivedNotification.notify(context, threadId, senderPhone, text)
    }
}