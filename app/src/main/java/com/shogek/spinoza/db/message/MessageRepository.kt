package com.shogek.spinoza.db.message

import androidx.lifecycle.LiveData

class MessageRepository(private val messageDao: MessageDao) {

    fun getAll(conversationId: Int): LiveData<List<Message>> {
        return messageDao.getAllMessages(conversationId)
    }

    suspend fun insert(message: Message) {
        messageDao.insert(message)
    }
}