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

        val conversations = ConversationCache.getAll(context.contentResolver)
        val conversationId = conversations.find { c -> c.senderPhoneStripped == senderPhone}?.threadId
        if (conversationId != null) {
            MessageNotificationHelper.notify(context, conversationId, senderPhone, text)
            return
        }

        // If we didn't find a 'Conversation' - it means it was cached by the repository.
        val newConversations = ConversationCache.getAll(context.contentResolver, true)
        val newConversationId = newConversations.find { c -> c.senderPhoneStripped == senderPhone }?.threadId
        if (newConversationId == null) {
            Log.e(TAG, "ConversationID not found.")
            return
        }

        MessageNotificationHelper.notify(
            context,
            newConversationId,
            senderPhone,
            text
        )
    }
}