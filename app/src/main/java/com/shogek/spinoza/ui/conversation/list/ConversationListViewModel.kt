package com.shogek.spinoza.ui.conversation.list

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.shogek.spinoza.db.contact.Contact
import com.shogek.spinoza.db.contact.ContactRepository
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationRepository
import com.shogek.spinoza.helpers.LiveDataHelpers.combineWith
import kotlinx.coroutines.launch

class ConversationListViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private val contactRepository: ContactRepository = ContactRepository(application, viewModelScope)
    private val conversationRepository: ConversationRepository = ConversationRepository(application, viewModelScope)

    var conversations: LiveData<List<Conversation>>

    init {
        this.conversations = conversationRepository.getAllObservable().combineWith(contactRepository.getAllObservable()) {
                conversations, contacts -> matchByContactId(conversations, contacts)
        }
    }

    private fun matchByContactId(
        conversations: List<Conversation>?,
        contacts: List<Contact>?
    ): List<Conversation> {
        if (conversations.isNullOrEmpty()) {
            return listOf()
        }

        if (contacts.isNullOrEmpty()) {
            return conversations
        }

        val contactTable = contacts.associateBy({it.id}, {it})

        conversations.forEach { conversation ->
            if (contactTable.containsKey(conversation.contactId)) {
                conversation.contact = contactTable[conversation.contactId]
            }
        }

        return conversations
    }

    fun archiveConversation(id: Long) {
        // TODO: [Feature] Implement archive conversation functionality
        Toast.makeText(this.context, "Archive: $id", Toast.LENGTH_SHORT).show()
    }

    fun deleteConversation(id: Long) = viewModelScope.launch {
        conversationRepository.deleteAllByIds(listOf(id))
        Toast.makeText(context, "Conversation deleted", Toast.LENGTH_SHORT).show()
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