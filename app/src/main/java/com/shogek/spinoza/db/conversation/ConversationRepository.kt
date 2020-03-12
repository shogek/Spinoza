package com.shogek.spinoza.db.conversation

import android.content.Context
import androidx.lifecycle.LiveData
import com.shogek.spinoza.db.ApplicationRoomDatabase
import kotlinx.coroutines.CoroutineScope


class ConversationRepository(
    context: Context,
    scope: CoroutineScope
) {

    private val dao = ApplicationRoomDatabase.getDatabase(context, scope).conversationDao()

    suspend fun delete(conversation: Conversation) {
        dao.delete(conversation)
    }

    suspend fun deleteAll(conversations: List<Conversation>) {
        dao.deleteAll(conversations)
    }

    suspend fun getAll(): List<Conversation> {
        return dao.getAll()
    }

    suspend fun getAll(ids: List<Long>): List<Conversation> {
        return dao.getAll(ids)
    }

    fun getAllObservable(): LiveData<List<Conversation>> {
        return dao.getAllObservable()
    }

    suspend fun get(id: Long): Conversation {
        return dao.get(id)
    }

    fun getObservable(id: Long): LiveData<Conversation> {
        return dao.getObservable(id)
    }

    suspend fun insert(conversation: Conversation): Long {
        return dao.insert(conversation)
    }

    suspend fun insertAll(conversations: List<Conversation>): List<Long> {
        return dao.insertAll(conversations)
    }

    suspend fun update(conversation: Conversation) {
        dao.update(conversation)
    }

    suspend fun updateAll(conversations: List<Conversation>) {
        dao.updateAll(conversations)
    }

    suspend fun update(
        id: Long,
        snippet: String,
        snippetTimestamp: Long,
        snippetIsOurs: Boolean,
        snippetWasRead: Boolean
    ) {
        dao.update(id, snippet, snippetTimestamp, snippetIsOurs, snippetWasRead)
    }
}