package com.shogek.spinoza.db.message

import android.content.Context
import com.shogek.spinoza.db.ApplicationRoomDatabase
import kotlinx.coroutines.CoroutineScope


class MessageRepository(
    context: Context,
    scope: CoroutineScope
) {

    private val messageDao = ApplicationRoomDatabase.getDatabase(context, scope).messageDao()


    suspend fun get(messageId: Long): Message {
        return messageDao.get(messageId)
    }

    suspend fun insert(message: Message): Long {
        return messageDao.insert(message)
    }

    suspend fun delete(message: Message) {
        messageDao.delete(message)
    }
}