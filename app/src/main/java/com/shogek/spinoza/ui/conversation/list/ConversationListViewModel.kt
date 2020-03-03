package com.shogek.spinoza.ui.conversation.list

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationRepository
import com.shogek.spinoza.db.conversation.ConversationRoomDatabase
import kotlinx.coroutines.launch

class ConversationListViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
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

    fun archiveConversation(id: Long) {
        // TODO: [Feature] Implement archive conversation functionality
        Toast.makeText(this.context, "Archive: $id", Toast.LENGTH_SHORT).show()
    }

    fun deleteConversation(id: Long) {
        // TODO: [Feature] Implement delete conversation functionality
        Toast.makeText(this.context, "Delete: $id", Toast.LENGTH_SHORT).show()
    }

    fun muteConversation(id: Long) {
        // TODO: [Feature] Implement mute conversation functionality
        Toast.makeText(this.context, "Mute: $id", Toast.LENGTH_SHORT).show()
    }

    fun markAsUnreadConversation(id: Long) {
        // TODO: [Feature] Implement mark conversation as unread functionality
        Toast.makeText(this.context, "Mark unread: $id", Toast.LENGTH_SHORT).show()
    }

    fun ignoreConversation(id: Long) {
        // TODO: [Feature] Implement ignore conversation functionality
        Toast.makeText(this.context, "Ignore: $id", Toast.LENGTH_SHORT).show()
    }

    fun blockConversation(id: Long) {
        // TODO: [Feature] Implement block conversation functionality
        Toast.makeText(this.context, "Block: $id", Toast.LENGTH_SHORT).show()
    }
}