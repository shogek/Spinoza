package com.shogek.spinoza.db.message

import android.content.Context
import androidx.lifecycle.LiveData
import com.shogek.spinoza.db.ApplicationRoomDatabase
import kotlinx.coroutines.CoroutineScope


class MessageRepository(
    context: Context,
    scope: CoroutineScope
) {

    private val messageDao = ApplicationRoomDatabase.getDatabase(context, scope).messageDao()


    fun getAllObservable(conversationId: Long): LiveData<List<Message>> {
        return messageDao.getAllObservable(conversationId)
    }

    suspend fun getAllAndroid(): List<Message> {
        return messageDao.getAllAndroid()
    }

    suspend fun insert(message: Message) {
        messageDao.insert(message)
    }

    suspend fun insertAll(messages: List<Message>) {
        if (messages.isNotEmpty()) {
            messageDao.insertAll(messages)
        }
    }

    suspend fun deleteAll(messages: List<Message>) {
        if (messages.isNotEmpty()) {
            messageDao.deleteAll(messages)
        }
    }

    suspend fun deleteAllByConversationIds(conversationIds: List<Long>) {
        if (conversationIds.isNotEmpty()) {
            messageDao.deleteAllByConversationIds(conversationIds)
        }
    }
}