package com.shogek.spinoza.db.conversation

import androidx.lifecycle.LiveData

class ConversationRepository(private val conversationDao: ConversationDao) {

    suspend fun delete(conversation: Conversation) {
        conversationDao.delete(conversation)
    }

    suspend fun deleteAll() {
        conversationDao.deleteAll()
    }

    fun getAll(): LiveData<List<Conversation>> {
        return conversationDao.getAll()
    }

    suspend fun get(id: Long): Conversation {
        return conversationDao.get(id)
    }

    suspend fun insert(conversation: Conversation): Long {
        return conversationDao.insert(conversation)
    }

    suspend fun insertAll(conversations: List<Conversation>): List<Long> {
        return conversationDao.insertAll(conversations)
    }

    suspend fun update(conversation: Conversation) {
        conversationDao.update(conversation)
    }

    suspend fun updateAll(conversations: List<Conversation>) {
        conversationDao.updateAll(conversations)
    }

    suspend fun update(
        id: Long,
        snippet: String,
        snippetTimestamp: Long,
        snippetIsOurs: Boolean,
        snippetWasRead: Boolean
    ) {
        conversationDao.update(id, snippet, snippetTimestamp, snippetIsOurs, snippetWasRead)
    }
}