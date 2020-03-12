package com.shogek.spinoza.db.message

import android.content.Context
import androidx.lifecycle.LiveData
import com.shogek.spinoza.db.ApplicationRoomDatabase
import kotlinx.coroutines.CoroutineScope


class MessageRepository(
    context: Context,
    scope: CoroutineScope
) {

    private val dao = ApplicationRoomDatabase.getDatabase(context, scope).messageDao()


    fun getAllObservable(conversationId: Long): LiveData<List<Message>> {
        return dao.getAllObservable(conversationId)
    }

    suspend fun insert(message: Message) {
        dao.insert(message)
    }

    suspend fun insertAll(messages: List<Message>) {
        dao.insertAll(messages)
    }
}