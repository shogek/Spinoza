package com.shogek.spinoza.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.telephony.SmsMessage
import androidx.core.os.postDelayed
import com.shogek.spinoza.helpers.MessageNotificationHelper
import com.shogek.spinoza.caches.ConversationCache
import com.shogek.spinoza.repositories.ConversationRepository

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

        var conversationId: Number?
        val conversations = ConversationRepository(context).getAll().value!!
        conversationId = conversations.find { c -> c.senderPhoneStripped == senderPhone}?.threadId
        if (conversationId == null) {
            // TODO: [Bug] Being the default SMS messaging app means we need to create the conversation for unknown numbers
            val newConversations = ConversationCache.getAll(context.contentResolver, true)
            conversationId = newConversations.find { c -> c.senderPhoneStripped == senderPhone }?.threadId
        }

        // TODO: [Bug] Being the default SMS messaging app means we need to create the message records
        Handler().postDelayed(1000) {
            MessageNotificationHelper.notify(context, conversationId!!, senderPhone, text)
        }
    }
}