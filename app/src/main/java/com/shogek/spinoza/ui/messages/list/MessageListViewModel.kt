package com.shogek.spinoza.ui.messages.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationRepository
import com.shogek.spinoza.db.message.Message
import com.shogek.spinoza.db.message.MessageRepository
import com.shogek.spinoza.helpers.LiveDataHelpers.combineWith


class MessageListViewModel(application: Application) : AndroidViewModel(application) {

    private val conversationRepository = ConversationRepository(application, viewModelScope)
    private val messageRepository = MessageRepository(application, viewModelScope)

    lateinit var conversation: LiveData<Conversation?>


    fun init(conversationId: Long): MessageListViewModel {
        val conversationObservable = this.conversationRepository.getObservable(conversationId)
        val messagesObservable = this.messageRepository.getAllObservable(conversationId)

        this.conversation = conversationObservable.combineWith(messagesObservable) {
                conversation, messages -> assign(conversation, messages)
        }
        return this
    }

    private fun assign(
        conversation: Conversation?,
        messages: List<Message>?
    ): Conversation? {
        if (conversation != null) {
            conversation.messages = messages
        }
        return conversation
    }
}