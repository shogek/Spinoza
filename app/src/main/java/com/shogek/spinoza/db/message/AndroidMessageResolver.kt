package com.shogek.spinoza.db.message

import android.content.ContentResolver
import android.provider.Telephony
import android.util.Log
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.message.MessageType.Companion.toInt


object AndroidMessageResolver {

    private val TAG: String = AndroidMessageResolver::class.java.simpleName


    /** Retrieve conversation messages saved in the phone by other messaging applications. */
    fun retrieveAllAndroidMessages(
        resolver: ContentResolver,
        androidConversations: List<Conversation>
    ): List<Message> {
        if (androidConversations.isEmpty()) {
            return listOf()
        }

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )

        val cursor = resolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            null,
            null,
            null
        )
        if (cursor == null) {
            Log.e(TAG, "Cursor is null")
            return listOf()
        }

        val messages = mutableListOf<Message>()
        val conversationTable = androidConversations.associateBy({it.androidId}, {it})

        while (cursor.moveToNext()) {
            val androidId = cursor.getLong(cursor.getColumnIndex(Telephony.Sms._ID))
            val threadId = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.THREAD_ID))
            val body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY))
            val date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE))
            val type = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE))

            val ourConversation = conversationTable.getValue(threadId)
            val message = Message(androidId, ourConversation.id, body, date, type == Telephony.Sms.MESSAGE_TYPE_SENT, MessageType.SENT.toInt())
            messages.add(message)
        }

        cursor.close()
        return messages
    }
}