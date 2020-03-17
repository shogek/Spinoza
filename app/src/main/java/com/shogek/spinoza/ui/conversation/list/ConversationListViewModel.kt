package com.shogek.spinoza.ui.conversation.list

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shogek.spinoza.Extra
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationRepository
import com.shogek.spinoza.ui.messages.list.MessageListActivity
import kotlinx.coroutines.launch

class ConversationListViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private val conversationRepository: ConversationRepository = ConversationRepository(application, viewModelScope)

    var conversations = conversationRepository.getAllWithContactsObservable()


    fun onConversationClick(conversation: Conversation) {
        // Open the corresponding conversation
        val intent = Intent(context, MessageListActivity::class.java)
        intent.putExtra(Extra.GOAL, Extra.ConversationList.MessageList.OpenConversation.GOAL)
        intent.putExtra(Extra.ConversationList.MessageList.OpenConversation.CONVERSATION_ID, conversation.id)
        context.startActivity(intent)
    }

    fun archiveConversation(conversation: Conversation) {
        // TODO: [Feature] Implement archive conversation functionality
        Toast.makeText(this.context, "Archive: ${conversation.id}", Toast.LENGTH_SHORT).show()
    }

    fun deleteConversation(conversation: Conversation) = viewModelScope.launch {
        conversationRepository.deleteAll(listOf(conversation))
        Toast.makeText(context, "Conversation deleted", Toast.LENGTH_SHORT).show()
    }

    fun muteConversation(conversation: Conversation) {
        // TODO: [Feature] Implement mute conversation functionality
        Toast.makeText(this.context, "Mute: ${conversation.id}", Toast.LENGTH_SHORT).show()
    }

    fun markAsUnreadConversation(conversation: Conversation) {
        // TODO: [Feature] Implement mark conversation as unread functionality
        Toast.makeText(this.context, "Mark unread: ${conversation.id}", Toast.LENGTH_SHORT).show()
    }

    fun ignoreConversation(conversation: Conversation) {
        // TODO: [Feature] Implement ignore conversation functionality
        Toast.makeText(this.context, "Ignore: ${conversation.id}", Toast.LENGTH_SHORT).show()
    }

    fun blockConversation(conversation: Conversation) {
        // TODO: [Feature] Implement block conversation functionality
        Toast.makeText(this.context, "Block: ${conversation.id}", Toast.LENGTH_SHORT).show()
    }
}