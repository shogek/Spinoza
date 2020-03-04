package com.shogek.spinoza.ui.messages.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationRepository
import com.shogek.spinoza.db.conversation.ConversationRoomDatabase
import com.shogek.spinoza.db.message.Message
import com.shogek.spinoza.db.message.MessageRepository
import com.shogek.spinoza.db.message.MessageRoomDatabase

class MessageListViewModel(application: Application) : AndroidViewModel(application) {

    private val conversationRepository: ConversationRepository
    private val messageRepository: MessageRepository
    lateinit var conversation: LiveData<Conversation>
    lateinit var messages: LiveData<List<Message>>

    init {
        val conversationDao = ConversationRoomDatabase.getDatabase(application, viewModelScope).conversationDao()
        val messageDao = MessageRoomDatabase.getDatabase(application).messageDao()
        this.conversationRepository = ConversationRepository(conversationDao)
        this.messageRepository = MessageRepository(messageDao)
    }

    fun init(conversationId: Long): MessageListViewModel {
        this.messages = this.messageRepository.getAll(conversationId)
        this.conversation = this.conversationRepository.get(conversationId)
        return this
    }
}