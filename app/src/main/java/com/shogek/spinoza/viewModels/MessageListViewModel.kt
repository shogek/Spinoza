package com.shogek.spinoza.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shogek.spinoza.models.Message
import com.shogek.spinoza.repositories.ConversationRepository
import com.shogek.spinoza.repositories.MessageRepository

class MessageListViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private var messages: LiveData<List<Message>> = MutableLiveData(listOf())
    private lateinit var threadId: Number

    fun init(threadId: Number): MessageListViewModel {
        this.threadId = threadId
        return this
    }

    fun getMessages(): LiveData<List<Message>> {
        val retrievedMessages = MessageRepository(this.context).getAll(this.threadId)
        this.messages = retrievedMessages
        return this.messages
    }

    fun sendMessage() {

    }

    fun markConversationAsRead() {
        MessageRepository(this.context).markAsRead(this.threadId)
        ConversationRepository(this.context).markAsRead(this.threadId)
    }
}