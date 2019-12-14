package com.shogek.spinoza.repositories

import android.content.ContentResolver
import android.provider.Telephony
import com.shogek.spinoza.models.Conversation

object ConversationRepository {

    fun getAll(resolver: ContentResolver): Array<Conversation> {
        val projection = arrayOf(
            Telephony.Sms.Conversations.ADDRESS     + " as " + Telephony.Sms.Conversations.ADDRESS,
            Telephony.Sms.Conversations.BODY        + " as " + Telephony.Sms.Conversations.BODY,
            Telephony.Sms.Conversations.DATE        + " as " + Telephony.Sms.Conversations.DATE,
            Telephony.Sms.Conversations.TYPE        + " as " + Telephony.Sms.Conversations.TYPE,
            Telephony.Sms.Conversations.READ        + " as " + Telephony.Sms.Conversations.READ,
            Telephony.Sms.Conversations.THREAD_ID   + " as " + Telephony.Sms.Conversations.THREAD_ID
        )

        val cursor = resolver.query(
            Telephony.Sms.Conversations.CONTENT_URI,
            projection,
            null,
            null,
            null
        )
            ?: return arrayOf()

        val conversations = mutableListOf<Conversation>()

        while (cursor.moveToNext()) {
            val address     = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Conversations.ADDRESS))
            val body        = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Conversations.BODY))
            val date        = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.Conversations.DATE))
            val type        = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Conversations.TYPE))
            val read        = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Conversations.READ))
            val threadId    = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Conversations.THREAD_ID))

            val isOurMessage = type == Telephony.Sms.Conversations.MESSAGE_TYPE_SENT

            val conversation = Conversation(threadId, address, null, null, body, date, read == 1, isOurMessage)
            conversations.add(conversation)
        }

        cursor.close()
        return conversations.toTypedArray()
    }
}
