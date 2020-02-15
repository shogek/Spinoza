package com.shogek.spinoza.db

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import com.shogek.spinoza.models.Message

object MessageDatabase {

    private val tag = MessageDatabase::class.java.simpleName

    fun createMessage(
        resolver: ContentResolver,
        threadId: Number,
        body: String,
        sentByUs: Boolean
    ): Message? {
        val messageType = if (sentByUs) Telephony.Sms.MESSAGE_TYPE_SENT else Telephony.Sms.MESSAGE_TYPE_INBOX

        val values = ContentValues()
        values.put(Telephony.Sms.THREAD_ID, threadId.toInt())
        values.put(Telephony.Sms.BODY, body)
        values.put(Telephony.Sms.TYPE, messageType)

        val result = resolver.insert(Telephony.Sms.CONTENT_URI, values)
        if (result == null) {
            Log.e(this.tag, "${::createMessage.name} - failed to create message with threadId=${threadId}, body=${body}, type=${messageType}")
            return null
        }

        return this.getMessage(resolver, result)
    }

    private fun getMessage(
        resolver: ContentResolver,
        uri: Uri
    ): Message? {
        val cursor = resolver.query(uri, null, null, null)
        if (cursor == null) {
            Log.e(this.tag, "${::getMessage.name} - Failed to get message via uri=${uri}")
            return null
        }

        cursor.moveToFirst()
        val id= cursor.getString(cursor.getColumnIndex(Telephony.Sms._ID))
        val body= cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY))
        val date= cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE))
        val type= cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE))
        val message = Message(id, body, date, type == Telephony.Sms.MESSAGE_TYPE_SENT)
        cursor.close()

        return message
    }
}