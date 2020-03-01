package com.shogek.spinoza.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationRepository
import com.shogek.spinoza.db.conversation.ConversationRoomDatabase
import kotlinx.coroutines.launch

class ConversationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ConversationRepository
    val allConversations: LiveData<List<Conversation>>

    init {
        val conversationDao = ConversationRoomDatabase.getDatabase(application).conversationDao()
        this.repository = ConversationRepository(conversationDao)
        this.allConversations = repository.getAll()
    }

    fun insert(conversation: Conversation) = viewModelScope.launch {
        repository.insert(conversation)
    }
}