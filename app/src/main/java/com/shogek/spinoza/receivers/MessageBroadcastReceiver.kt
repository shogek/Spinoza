package com.shogek.spinoza.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import com.shogek.spinoza.helpers.MessageNotificationHelper
import com.shogek.spinoza.caches.ConversationCache

class MessageBroadcastReceiver: BroadcastReceiver() {
    companion object {
        val TAG = MessageBroadcastReceiver::class.java.simpleName
    }

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

        var conversationId: Number?
        val conversations = ConversationCache.getAll(context.contentResolver)
        conversationId = conversations.find { c -> c.senderPhoneStripped == senderPhone}?.threadId
        if (conversationId == null) {
            // If we didn't find a 'Conversation' - it means it was cached by the repository.
            val newConversations = ConversationCache.getAll(context.contentResolver, true)
            conversationId = newConversations.find { c -> c.senderPhoneStripped == senderPhone }?.threadId
        }

        MessageNotificationHelper.notify(context, conversationId!!, senderPhone, text)
    }
}