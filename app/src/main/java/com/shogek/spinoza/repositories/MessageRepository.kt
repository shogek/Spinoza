package com.shogek.spinoza.repositories

import android.content.ContentResolver
import android.provider.Telephony
import com.shogek.spinoza.models.Message

object MessageRepository {

    fun getAll(resolver: ContentResolver,
               threadId: Number
    ) : Array<Message> {
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
        ) ?: return arrayOf()

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

        return messages.toTypedArray()
    }

    /** Will only work if the app is set as the default messaging application */
    fun delete(resolver: ContentResolver,
               id: String
    ): Number {
        val selection = "${Telephony.Sms._ID} = ?"
        val selectionArgs = arrayOf(id)

        return resolver.delete(
            Telephony.Sms.CONTENT_URI,
            selection,
            selectionArgs
        )
    }
}
