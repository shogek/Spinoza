package com.shogek.spinoza.repositories

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.provider.Telephony
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shogek.spinoza.models.Message

class MessageRepository(
    private val context: Context
) {

    private companion object {
        private val TAG = MessageRepository::class.java.simpleName
        private var messages: HashMap<Number, MutableLiveData<List<Message>>> = HashMap()
    }

    fun getAll(threadId: Number): LiveData<List<Message>> {
        if (messages.containsKey(threadId)) {
            return messages[threadId]!!
        }

        val retrievedMessages = retrieveConversationMessages(threadId)
        messages[threadId] = MutableLiveData(retrievedMessages)
        return messages[threadId]!!
    }

    fun messageSent(
        threadId: Number,
        messageText: String
    ): Message {
        val newestMessage = this.getLatest(threadId, messageText)

        if (messages.containsKey(threadId)) {
            // TODO: Test it
            val tempMessages = messages[threadId]!!.value!!.toMutableList()
            tempMessages.add(newestMessage)
            messages[threadId]!!.value = tempMessages
        } else {
            // TODO: Test it
            val allMessages = this.getAll(threadId).value!!
            allMessages.toMutableList().add(newestMessage)
            messages[threadId] = MutableLiveData(allMessages)
        }

        return newestMessage
    }

    fun messageReceived(
        threadId: Number,
        messageText: String
    ): Message {
        val newestMessage = this.getLatest(threadId, messageText)

        if (messages.containsKey(threadId)) {
            // TODO: Test it
            val tempMessages = messages[threadId]!!.value!!.toMutableList()
            tempMessages.add(newestMessage)
            messages[threadId]!!.value = tempMessages
        } else {
            // TODO: Test it
            val allMessages = this.getAll(threadId).value!!
            allMessages.toMutableList().add(newestMessage)
            messages[threadId] = MutableLiveData(allMessages)
        }

        return newestMessage
    }

    private fun retrieveConversationMessages(threadId: Number): List<Message> {
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )

        val selection = "${Telephony.Sms.THREAD_ID} = ?"
        val selectionArgs = arrayOf(threadId.toString())
        val sortOrder = "${Telephony.Sms.DATE} ASC"

        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        // TODO: Test it
        if (cursor == null) {
            Log.e(TAG, "Conversation does not exist!")
            return listOf()
        }

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
        return messages
    }

    fun markAsRead(threadId: Number): Number {
        val contentValues = ContentValues()
        contentValues.put(Telephony.Sms.READ, 1)

        val where = "${Telephony.Sms.READ}=0 AND ${Telephony.Sms.Conversations.THREAD_ID}=${threadId}"

        return context.contentResolver.update(
            Telephony.Sms.Inbox.CONTENT_URI,
            contentValues,
            where,
            null
        )
    }

//    fun getAll(
//        resolver: ContentResolver,
//        threadId: Number
//    ) : Array<Message> {
//        val projection = arrayOf(
//            Telephony.Sms._ID,
//            Telephony.Sms.BODY,
//            Telephony.Sms.DATE,
//            Telephony.Sms.TYPE
//        )
//
//        val selection = "${Telephony.Sms.THREAD_ID} = ?"
//        val selectionArgs = arrayOf(threadId.toString())
//        val sortOrder = "${Telephony.Sms.DATE} ASC"
//
//        val cursor = resolver.query(
//            Telephony.Sms.CONTENT_URI,
//            projection,
//            selection,
//            selectionArgs,
//            sortOrder
//        ) ?: return arrayOf()
//
//        val messages = mutableListOf<Message>()
//
//        while (cursor.moveToNext()) {
//            val id      = cursor.getString(cursor.getColumnIndex(Telephony.Sms._ID))
//            val body    = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY))
//            val date    = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE))
//            val type    = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE))
//            val message = Message(id, body, date, type == Telephony.Sms.MESSAGE_TYPE_SENT)
//
//            messages.add(message)
//        }
//        cursor.close()
//
//        return messages.toTypedArray()
//    }

    private fun getLatest(
        threadId: Number,
        text: String
    ) : Message {
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )

        val selection = "${Telephony.Sms.THREAD_ID} = ?" + " AND " + "${Telephony.Sms.BODY} = ?"
        val selectionArgs = arrayOf(threadId.toString(), text)
        val sortOrder = "${Telephony.Sms.DATE} DESC"

        val cursor = this.context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )!!

        cursor.moveToFirst()

        val id      = cursor.getString(cursor.getColumnIndex(Telephony.Sms._ID))
        val body    = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY))
        val date    = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE))
        val type    = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE))
        val message = Message(id, body, date, type == Telephony.Sms.MESSAGE_TYPE_SENT)

        cursor.close()

        return message
    }

    /** Will only work if the app is set as the default messaging application */
    fun delete(
        resolver: ContentResolver,
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
