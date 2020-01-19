package com.shogek.spinoza.viewModels

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.*
import com.shogek.spinoza.models.Contact
import com.shogek.spinoza.models.Conversation
import com.shogek.spinoza.repositories.ContactRepository
import com.shogek.spinoza.repositories.ConversationRepository

class ConversationListViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    var conversations: LiveData<List<Conversation>> = MutableLiveData()

    init {
        val allConversations = ConversationRepository(this.context).getAll()
        val allContacts = ContactRepository(this.context).getAll().value!!

        this.conversations = Transformations.map(allConversations) {  mergeConversationsToContacts(it, allContacts) }
    }

    private companion object {
        fun mergeConversationsToContacts(
            conversations: List<Conversation>,
            contacts: List<Contact>
        ): List<Conversation> {
            conversations.forEach { conversation ->
                contacts.forEach { contact ->
                    if (conversation.senderPhoneStripped == contact.strippedPhone) {
                        conversation.contact = contact
                    }
                }
            }
            return conversations
        }
    }

    fun archiveConversation(
        conversationId: Number
    ) {
        // TODO: [Feature] Implement archive conversation functionality
        Toast.makeText(this.context, "TODO [Archive]: $conversationId", Toast.LENGTH_SHORT).show()
    }

    fun deleteConversation(
        conversationId: Number
    ) {
        // TODO: [Feature] Implement delete conversation functionality
        Toast.makeText(this.context, "TODO [Delete]: $conversationId", Toast.LENGTH_SHORT).show()
    }

    fun muteConversation(
        conversationId: Number
    ) {
        // TODO: [Feature] Implement mute conversation functionality
        Toast.makeText(this.context, "TODO [Mute]: $conversationId", Toast.LENGTH_SHORT).show()
    }

    fun markAsUnreadConversation(
        conversationId: Number
    ) {
        // TODO: [Feature] Implement mark conversation as unread functionality
        Toast.makeText(this.context, "TODO [Mark unread]: $conversationId", Toast.LENGTH_SHORT).show()
    }

    fun ignoreConversation(
        conversationId: Number
    ) {
        // TODO: [Feature] Implement ignore conversation functionality
        Toast.makeText(this.context, "TODO [Ignore]: $conversationId", Toast.LENGTH_SHORT).show()
    }

    fun blockConversation(
        conversationId: Number
    ) {
        // TODO: [Feature] Implement block conversation functionality
        Toast.makeText(this.context, "TODO [Block]: $conversationId", Toast.LENGTH_SHORT).show()
    }
}
