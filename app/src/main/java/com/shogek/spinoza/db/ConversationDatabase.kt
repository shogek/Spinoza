package com.shogek.spinoza.db

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import com.shogek.spinoza.models.Conversation

object ConversationDatabase {

    private val tag = ConversationDatabase::class.java.simpleName

    fun createConversation1(
        context: Context,
        resolver: ContentResolver,
        senderPhoneNumber: String,
        latestMessageText: String,
        latestMessageTimestamp: Long,
        latestMessageIsOurs: Boolean,
        latestMessageWasRead: Boolean
    ): Conversation? {

//        val threads = this.retrieveAllThreads(resolver)



//        MessageDatabase.createMessage(resolver, 13, "TESTAS", true)

//        val what = ContentUris.withAppendedId(Telephony.Sms.Conversations.CONTENT_URI, 13)
//        val allConversations = this.retrieveAllConversations(resolver)

//        val path = Telephony.Sms.Conversations.CONTENT_URI.buildUpon().appendPath("16").build()
//        var test2 = this.getConversation(resolver, path)
//        return null
        val isOurs = if (latestMessageIsOurs) Telephony.Sms.Conversations.MESSAGE_TYPE_SENT else Telephony.Sms.Conversations.MESSAGE_TYPE_INBOX
        val wasRead = if (latestMessageWasRead) 1 else 0

//        val values = ContentValues()
//        values.put(Telephony.Sms.Conversations.ADDRESS, senderPhoneNumber)
//        values.put(Telephony.Sms.Conversations.BODY, latestMessageText)
//        values.put(Telephony.Sms.Conversations.DATE, latestMessageTimestamp)
//        values.put(Telephony.Sms.Conversations.TYPE, isOurs)
//        values.put(Telephony.Sms.Conversations.READ, wasRead)

        val values = ContentValues()
        values.put(Telephony.Sms.Conversations.ADDRESS, "6505551277")
        values.put(Telephony.Sms.Conversations.BODY, "Testing")
        values.put(Telephony.Sms.Conversations.DATE, System.currentTimeMillis())
        values.put(Telephony.Sms.Conversations.TYPE, Telephony.Sms.Conversations.MESSAGE_TYPE_INBOX)
        values.put(Telephony.Sms.Conversations.READ, 1)

//        val test = Telephony.Threads.getOrCreateThreadId(context, senderPhoneNumber)

//        val threadId = Telephony.Threads.getOrCreateThreadId(context, "+37063392222")
//        values.put(Telephony.Threads._ID, threadId)

        val result = context.contentResolver.insert(Telephony.Sms.Conversations.CONTENT_URI, values)
        return null

        // Telephony.MmsSms.CONTENT_CONVERSATIONS_URI
        // java.lang.UnsupportedOperationException: MmsSmsProvider does not support deletes, inserts, or updates for this URI.content://mms-sms/conversations

        // Telephony.Sms.Conversations.CONTENT_URI ("content://sms/conversations")
        // java.lang.NullPointerException: Uri must not be null



//        if (result == null) {
//            // TODO: Log function arguments
//            Log.e(this.tag, "${::createConversation.name} - failed to create conversation with address=${senderPhoneNumber}, body=${latestMessageText}, date=${latestMessageTimestamp}, type=${isOurs}, read=${wasRead}")
//            return null
//        }
//
//        return this.getConversation(resolver, result)
    }

    private fun getConversation(
        resolver: ContentResolver,
        uri: Uri
    ): Conversation? {
        val cursor = resolver.query(uri, null, null, null)
        if (cursor == null) {
            Log.e(this.tag, "${::getConversation.name} - failed to get conversation via uri=${uri}")
            return null
        }

        cursor.moveToFirst()
        val address= cursor.getString(cursor.getColumnIndex(Telephony.Sms.Conversations.ADDRESS))
        val body= cursor.getString(cursor.getColumnIndex(Telephony.Sms.Conversations.BODY))
        val date= cursor.getLong(cursor.getColumnIndex(Telephony.Sms.Conversations.DATE))
        val type= cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Conversations.TYPE))
        val read= cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Conversations.READ))
        val threadId= cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Conversations.THREAD_ID))
        val isOurMessage= type == Telephony.Sms.Conversations.MESSAGE_TYPE_SENT
        val conversation = Conversation(threadId, address, null, null, body, date, read == 1, isOurMessage)
        cursor.close()

        return conversation
    }

    private fun retrieveAllConversations(resolver: ContentResolver): List<Conversation> {
        val projection = arrayOf(
            Telephony.Sms.Conversations._ID + " as " + Telephony.Sms.Conversations._ID,
            Telephony.Sms.Conversations.ADDRESS + " as " + Telephony.Sms.Conversations.ADDRESS,
            Telephony.Sms.Conversations.BODY + " as " + Telephony.Sms.Conversations.BODY,
            Telephony.Sms.Conversations.DATE + " as " + Telephony.Sms.Conversations.DATE,
            Telephony.Sms.Conversations.TYPE + " as " + Telephony.Sms.Conversations.TYPE,
            Telephony.Sms.Conversations.READ + " as " + Telephony.Sms.Conversations.READ,
            Telephony.Sms.Conversations.THREAD_ID + " as " + Telephony.Sms.Conversations.THREAD_ID
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
            val id = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Conversations._ID))
            val address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Conversations.ADDRESS))
            val body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Conversations.BODY))
            val date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.Conversations.DATE))
            val type = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Conversations.TYPE))
            val read = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Conversations.READ))
            val threadId =
                cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Conversations.THREAD_ID))

            val isOurMessage = type == Telephony.Sms.Conversations.MESSAGE_TYPE_SENT

            val conversation =
                Conversation(threadId, address, null, null, body, date, read == 1, isOurMessage)
            conversations.add(conversation)
        }

        cursor.close()
        return conversations
    }

    private fun retrieveAllThreads(resolver: ContentResolver): List<Conversation> {
        val projection = arrayOf(
            Telephony.ThreadsColumns._ID,
            Telephony.ThreadsColumns.DATE,
//            Telephony.ThreadsColumns.SNIPPET,
            Telephony.ThreadsColumns.RECIPIENT_IDS,
            Telephony.ThreadsColumns.MESSAGE_COUNT,
            Telephony.ThreadsColumns.ERROR,
            Telephony.ThreadsColumns.READ,
            Telephony.ThreadsColumns.TYPE,
            Telephony.ThreadsColumns.ARCHIVED
        )

        val cursor = resolver.query(
            Telephony.Threads.CONTENT_URI,
            projection,
            null,
            null,
            null
        )
            ?: return listOf()

        val conversations = mutableListOf<Conversation>()

        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndex(Telephony.ThreadsColumns._ID))
            val date = cursor.getString(cursor.getColumnIndex(Telephony.ThreadsColumns.DATE))
            val snippet = cursor.getString(cursor.getColumnIndex(Telephony.ThreadsColumns.SNIPPET))
            val recipientIds = cursor.getString(cursor.getColumnIndex(Telephony.ThreadsColumns.RECIPIENT_IDS))
            val count = cursor.getString(cursor.getColumnIndex(Telephony.ThreadsColumns.MESSAGE_COUNT))
            val error = cursor.getString(cursor.getColumnIndex(Telephony.ThreadsColumns.ERROR))
            val read = cursor.getString(cursor.getColumnIndex(Telephony.ThreadsColumns.READ))
            val type = cursor.getString(cursor.getColumnIndex(Telephony.ThreadsColumns.TYPE))
            val archived = cursor.getString(cursor.getColumnIndex(Telephony.ThreadsColumns.ARCHIVED))
            val test = archived

//            val body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Conversations.BODY))
//            val type = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Conversations.TYPE))
//            val read = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Conversations.READ))
//            val threadId =
//                cursor.getInt(cursor.getColumnIndex(Telephony.Sms.Conversations.THREAD_ID))
//
//            val isOurMessage = type == Telephony.Sms.Conversations.MESSAGE_TYPE_SENT
//
//            val conversation =
//                Conversation(threadId, address, null, null, body, date, read == 1, isOurMessage)
//            conversations.add(conversation)
        }

        cursor.close()
        return conversations
    }



    fun createConversation(
        context: Context,
        resolver: ContentResolver,
        senderPhoneNumber: String,
        latestMessageText: String,
        latestMessageTimestamp: Long,
        latestMessageIsOurs: Boolean,
        latestMessageWasRead: Boolean
    ): Conversation? {
        val isOurs = if (latestMessageIsOurs) Telephony.Sms.Conversations.MESSAGE_TYPE_SENT else Telephony.Sms.Conversations.MESSAGE_TYPE_INBOX
        val wasRead = if (latestMessageWasRead) 1 else 0

        val values = ContentValues()
        values.put(Telephony.Sms.Conversations.ADDRESS, senderPhoneNumber)
        values.put(Telephony.Sms.Conversations.BODY, latestMessageText)
        values.put(Telephony.Sms.Conversations.DATE, latestMessageTimestamp)
        values.put(Telephony.Sms.Conversations.TYPE, isOurs)
        values.put(Telephony.Sms.Conversations.READ, wasRead)
//        val result = resolver.insert(Uri.parse("content://sms/conversations"), values)!!
        val threadId = Telephony.Threads.getOrCreateThreadId(context, "6505551299")

//        val result1 = resolver.update(
//            Telephony.Sms.Conversations.CONTENT_URI,
//            values,
//            "${Telephony.Sms.Conversations.THREAD_ID}=${threadId}",
//            null
//        )


        val values1 = ContentValues()
        values1.put(Telephony.Threads.SNIPPET, latestMessageText)
        values1.put(Telephony.Threads.DATE, latestMessageTimestamp)
        values1.put(Telephony.Threads.READ, 0)
        values1.put(Telephony.Threads.TYPE, isOurs)
//        values1.put(Telephony.Sms.Conversations.READ, wasRead)
        val result2 = resolver.update(
            Telephony.Threads.CONTENT_URI.buildUpon().appendQueryParameter(Telephony.Threads._ID, threadId.toString()).build(),
            values1,
            null,
            null
        )

        return null
        return this.getConversation(resolver, Telephony.Sms.Conversations.CONTENT_URI)
    }
}

// Telephony.MmsSms.CONTENT_CONVERSATIONS_URI
// java.lang.UnsupportedOperationException: MmsSmsProvider does not support deletes, inserts, or updates for this URI.content://mms-sms/conversations

// Telephony.Sms.Conversations.CONTENT_URI ("content://sms/conversations")
// java.lang.NullPointerException: Uri must not be null