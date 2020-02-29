package com.shogek.spinoza.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Handler
import android.telephony.SmsMessage
import android.util.Log
import androidx.core.os.postDelayed
import com.shogek.spinoza.db.ConversationDatabase
import com.shogek.spinoza.db.MessageDatabase
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationDao
import com.shogek.spinoza.db.conversation.ConversationRoomDatabase
import com.shogek.spinoza.helpers.MessageNotificationHelper
import com.shogek.spinoza.repositories.ConversationRepository
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import kotlin.coroutines.coroutineContext

class MessageBroadcastReceiver: BroadcastReceiver() {

    private companion object {
        val LOG = MessageBroadcastReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Log.e(LOG, "'context' or 'intent' is null")
            return
        }

        val timestamp = System.currentTimeMillis() // when was message received

        val pdus = (intent.extras?.get("pdus") as Array<*>).filterIsInstance<ByteArray>()
        val format = intent.extras?.get("format") as String?
        if (pdus.isEmpty() || format == null) {
            Log.e(LOG, "Failed to parse received SMS message")
            return
        }
        val messageText = pdus.fold("") { acc, bytes -> acc + SmsMessage.createFromPdu(bytes, format).displayMessageBody }

        val senderPhone = SmsMessage.createFromPdu(pdus.first(), format).originatingAddress
        if (senderPhone == null) {
            Log.e(LOG, "Failed to parse received SMS message's sender phone number")
            return
        }

        val dao = ConversationRoomDatabase.getDatabase(context).conversationDao()
        val allConversations = dao.getAllConversations().value
        if (allConversations == null || allConversations.isEmpty()) {
            // TODO: [Bug] Check if message was received while in a conversation (so we can mark message as read)
            val newConversation = Conversation(senderPhone, messageText, timestamp, snippetIsOurs = false, snippetWasRead = false)
            // TODO: [Refactor] Use async
            runBlocking { dao.insert(newConversation) }
        }
//        val allConversations = repository.allConversations.value!!
//        val currentConversation = allConversations.find { it.phone == senderPhone }
//        if (currentConversation == null) {
//            val what = Executors.newSingleThreadExecutor().execute {
//                dao.insert(newConversation)
//            }
//            AsyncTask.execute { dao.insert(newConversation) }
//            run {
//                dao.insert(newConversation)
//            }
//        }

//        var conversationId: Number?
//        val conversations = ConversationRepository(context).getAll().value!!
//        conversationId = conversations.find { c -> c.senderPhoneStripped == senderPhone}?.threadId
//        if (conversationId == null) {
//            // TODO: [Bug] Being the default SMS messaging app means we need to create the conversation for unknown numbers
//            val newConversations = ConversationRepository(context).getAll().value!!
//            conversationId = newConversations.find { c -> c.senderPhoneStripped == senderPhone }?.threadId
//        }

//        val conversation = ConversationDatabase.createConversation(
//            context,
//            context.contentResolver,
//            senderPhone,
//            text,
//            System.currentTimeMillis(),
//            latestMessageIsOurs = false,
//            latestMessageWasRead = false
//        )
//        return
//        val result = MessageDatabase.createMessage(context.contentResolver, 14, text, false)


        // TODO: [Bug] Being the default SMS messaging app means we need to create the message records
//        Handler().postDelayed(1000) {
//            MessageNotificationHelper.notify(context, 14, senderPhone, text)
//        }
    }
}