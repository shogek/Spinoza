package com.shogek.spinoza.db.message

import android.content.ContentResolver
import android.provider.Telephony
import android.util.Log
import com.shogek.spinoza.db.conversation.Conversation


object AndroidMessageHelper {

    private val TAG: String = AndroidMessageHelper::class.java.simpleName


    /** Retrieves messages form the phone that are not in our database. */
    fun retrieveNewAndroidMessages(
        resolver: ContentResolver,
        androidMessages: List<Message>,
        androidConversations: List<Conversation>
    ): List<Message> {
        if (androidMessages.isEmpty() || androidConversations.isEmpty()) {
            return listOf()
        }

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )

        // Find new phone messages by matching their IDs against the ones that we have
        val existingIds = androidMessages.map { it.androidId!! }
        val selection = Telephony.Sms._ID + " NOT IN " + "(" + existingIds.joinToString(",") + ")"

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

        val conversationTable = androidConversations.associateBy({it.androidId!!}, {it})
        val newMessages = mutableListOf<Message>()

        while (cursor.moveToNext()) {
            val androidId = cursor.getLong(cursor.getColumnIndex(Telephony.Sms._ID))
            val threadId = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.THREAD_ID))
            val body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY))
            val date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE))
            val type = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE))

            val ourConversation = conversationTable.getValue(threadId)
            val message = Message(androidId, ourConversation.id, body, date, type == Telephony.Sms.MESSAGE_TYPE_SENT)
            newMessages.add(message)
        }

        cursor.close()
        return newMessages
    }

    /** Return passed in messages that were not found in the phone. */
    fun retrieveDeletedAndroidMessages(
        resolver: ContentResolver,
        androidMessages: List<Message>
    ): List<Message> {
        if (androidMessages.isEmpty()) {
            return listOf()
        }

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )

        // If the passed in message ID was not returned - it was removed
        val androidIds = androidMessages.map { it.androidId!! }
        val selection = Telephony.Sms._ID + " IN " + "(" + androidIds.joinToString(",") + ")"

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

        val foundIds = mutableListOf<Long>()

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex(Telephony.Sms._ID))
            foundIds.add(id)
        }

        cursor.close()
        return androidMessages.filter { !foundIds.contains(it.androidId) }
    }

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
            val message = Message(androidId, ourConversation.id, body, date, type == Telephony.Sms.MESSAGE_TYPE_SENT)
            messages.add(message)
        }

        cursor.close()
        return messages
    }
}