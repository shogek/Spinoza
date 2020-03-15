package com.shogek.spinoza.ui.messages.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationRepository


class MessageListViewModel(application: Application) : AndroidViewModel(application) {

    private val conversationRepository = ConversationRepository(application, viewModelScope)
    lateinit var conversation: LiveData<Conversation>


    fun init(conversationId: Long): MessageListViewModel {
        this.conversation = conversationRepository.getWithContactAndMessagesObservable(conversationId)
        return this
    }
}