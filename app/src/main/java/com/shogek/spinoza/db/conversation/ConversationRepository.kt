package com.shogek.spinoza.db.conversation

import androidx.lifecycle.LiveData

class ConversationRepository(private val conversationDao: ConversationDao) {

    val allConversations: LiveData<List<Conversation>> = conversationDao.getAllConversations()

    suspend fun insert(conversation: Conversation) {
        conversationDao.insert(conversation)
    }
}