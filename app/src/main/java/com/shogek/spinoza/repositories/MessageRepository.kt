package com.shogek.spinoza.repositories

import android.content.ContentResolver
import android.provider.Telephony
import com.shogek.spinoza.models.Message

object MessageRepository {
    /** [Telephony.Sms.Conversations.THREAD_ID] returns a list of that conversation Messages */
    private val messages: HashMap<Number, MutableList<Message>?> = HashMap()

    /** Queries for the latest message in the conversationReturns 'Message' if true, else null */
    fun checkIfMessageSent(resolver: ContentResolver, threadId: Number, textSent: String): Message? {
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        val selection = "${Telephony.Sms.THREAD_ID} = ? AND ${Telephony.Sms.BODY} = ?"
        val selectionArgs = arrayOf(threadId.toString(), textSent)
        val sortOrder = "${Telephony.Sms.DATE} DESC LIMIT 1"

        val cursor = resolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        ) ?: return null

        if (!cursor.moveToFirst()) {
            return null
        }

        val id      = cursor.getString(cursor.getColumnIndex(Telephony.Sms._ID))
        val body    = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY))
        val date    = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE))
        val message = Message(id, body, date, true)

        // We have the thread ID but not its messages? Someone (me) fucked up something
        val messages = this.messages[threadId]
        messages!!.add(message)

        return message
    }

    fun get(resolver: ContentResolver, threadId: Number): MutableList<Message>? {
        val allMessages = this.messages[threadId]
        if (allMessages != null) {
            return allMessages
        }

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )

        val selection = "${Telephony.Sms.THREAD_ID} = ?"
        val selectionArgs = arrayOf(threadId.toString())
        val sortOrder = "${Telephony.Sms.DATE} ASC"

        val cursor = resolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        ) ?: return mutableListOf()

        val messages = mutableListOf<Message>()

        while (cursor.moveToNext()) {
            val id      = cursor.getString(cursor.getColumnIndex(Telephony.Sms._ID))
            val body    = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY))
            val date    = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE))
            val type    = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE))
            val message = Message(id, body, date, type == Telephony.Sms.MESSAGE_TYPE_SENT)

            messages.add(message)
        }
        cursor.close()

        this.messages[threadId] = messages
        return messages
    }
}
