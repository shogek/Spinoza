package com.shogek.spinoza.db.message

import android.content.ContentResolver
import android.provider.Telephony
import android.util.Log
import com.shogek.spinoza.db.conversation.Conversation


object MessageDatabaseHelper {

    private val TAG: String = MessageDatabaseHelper::class.java.simpleName


    // TODO: [Doc] Explain
    fun retrieveMessagesForPhoneConversations(
        resolver: ContentResolver,
        conversations: List<Conversation>
    ): List<Message> {
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )

        val conversationIds = conversations.map { it.androidId }
        val selection = Telephony.Sms.THREAD_ID + " IN " + "(" + conversationIds.joinToString(",") + ")"

        val cursor = resolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            selection,
            null,
            null
        )
        if (cursor == null) {
            Log.e(TAG, "Cursor is null")
            return listOf()
        }

        val messages = mutableListOf<Message>()
        val ourConversations = conversations.associateBy({it.androidId}, {it})

        while (cursor.moveToNext()) {
            val androidId = cursor.getLong(cursor.getColumnIndex(Telephony.Sms._ID))
            val threadId = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.THREAD_ID))
            val body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY))
            val date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE))
            val type = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE))

            // TODO: Explain this
            val ourConversation = ourConversations.getValue(threadId)
            val message = Message(androidId, ourConversation.id, body, date, type == Telephony.Sms.MESSAGE_TYPE_SENT)
            messages.add(message)
        }

        cursor.close()
        return messages
    }
}