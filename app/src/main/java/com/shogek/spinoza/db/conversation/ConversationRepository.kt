package com.shogek.spinoza.db.conversation

import androidx.lifecycle.LiveData

class ConversationRepository(private val conversationDao: ConversationDao) {

    val allConversations: LiveData<List<Conversation>> = conversationDao.getAllConversations()

    suspend fun insert(conversation: Conversation): Long {
        return conversationDao.insert(conversation)
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