package com.shogek.spinoza.repositories

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.provider.Telephony
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shogek.spinoza.models.Conversation

class ConversationRepository(
    private val context: Context
) {

    private companion object {
        private var conversations: MutableLiveData<List<Conversation>> = MutableLiveData()
    }

    fun get(
        threadId: Number
    ): Conversation {
        if (conversations.value.isNullOrEmpty()) {
            this.initData()
        }
        return conversations.value!!.first { it.threadId == threadId }
    }

    fun getAll(): LiveData<List<Conversation>> {
        if (conversations.value.isNullOrEmpty()) {
            this.initData()
        }
        return conversations
    }

    fun messageReceived(
        threadId: Number,
        text: String,
        timestamp: Long,
        isOurs: Boolean
    ) {
        // TODO: [Bug] Breaks if an unknown number messages us
        val tempConversations = conversations.value!!.toMutableList()
        val conversation = tempConversations.find { it.threadId == threadId }!!
        conversation.latestMessageText = text
        conversation.latestMessageIsOurs = isOurs
        conversation.latestMessageWasRead = isOurs
        conversation.latestMessageTimestamp = timestamp

        conversations.value = tempConversations
    }

    private fun initData() {
        conversations.value = this.retrieveAllConversations(context.contentResolver)
    }

    private fun retrieveAllConversations(resolver: ContentResolver): List<Conversation> {
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
            ?: return listOf()

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
        return conversations
    }

    // TODO: [Test] As default message provider
    // TODO: [Async] Make it
    fun markAsRead(
        threadId: Int
    ) : Number {
        // Local change
        val tempConversations = conversations.value!!.toMutableList()
        val conversation = tempConversations.find { it.threadId == threadId }!!
        conversation.latestMessageWasRead = true
        conversations.value = tempConversations

        // Database change
        val contentValues = ContentValues()
        contentValues.put(Telephony.Sms.Conversations.READ, 1)

        val where = "${Telephony.Sms.Conversations._ID}=${threadId} AND ${Telephony.Sms.Conversations.READ}=0"

        return this.context.contentResolver.update(
            Telephony.Sms.Conversations.CONTENT_URI,
            contentValues,
            where,
            null
        )
    }
}
