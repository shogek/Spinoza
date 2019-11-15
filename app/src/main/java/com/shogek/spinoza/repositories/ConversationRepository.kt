package com.shogek.spinoza.repositories

import android.content.ContentResolver
import android.provider.Telephony
import com.shogek.spinoza.models.Conversation

object ConversationRepository {
    /** [Telephony.Sms.Conversations.THREAD_ID] returns Conversation */
    private var conversations: HashMap<Int, Conversation>? = null

    fun get(threadId: Int): Conversation? = this.conversations?.get(threadId)

    fun getAll(resolver: ContentResolver): Array<Conversation> {
        // TODO: Bad practice, easy to break something when reusing like this.
        var convos = this.conversations
        if (convos != null) {
            return convos.values.toTypedArray()
        }

        val projection = arrayOf(
            Telephony.Sms.Conversations.ADDRESS     + " as " + Telephony.Sms.Conversations.ADDRESS,
            Telephony.Sms.Conversations.BODY        + " as " + Telephony.Sms.Conversations.BODY,
            Telephony.Sms.Conversations.DATE        + " as " + Telephony.Sms.Conversations.DATE,
            Telephony.Sms.Conversations.TYPE        + " as " + Telephony.Sms.Conversations.TYPE,
            Telephony.Sms.Conversations.READ        + " as " + Telephony.Sms.Conversations.READ,
            Telephony.Sms.Conversations.THREAD_ID   + " as " + Telephony.Sms.Conversations.THREAD_ID
        )

        val selection = null
        val selectionArgs = null
        val sortOrder = null

        val cursor = resolver.query(
            Telephony.Sms.Conversations.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
            ?: return emptyArray()

        convos = HashMap()

        while (cursor.moveToNext()) {
            val address     = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Conversations.ADDRESS))
            val body        = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Conversations.BODY))
            val date        = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.Conversations.DATE))
            val type        = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Conversations.TYPE))
            val read        = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Conversations.READ))
            val threadId    = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Conversations.THREAD_ID))

            val conversation = Conversation(
                threadId,
                address,
                null,
                null,
                body,
                date,
                read == 1,
                type == Telephony.Sms.Conversations.MESSAGE_TYPE_SENT
            )

            convos[threadId] = conversation
        }
        cursor.close()
        this.conversations = convos
        return convos.values.toTypedArray()
    }
}
