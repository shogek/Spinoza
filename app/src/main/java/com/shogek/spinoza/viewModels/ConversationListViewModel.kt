package com.shogek.spinoza.viewModels

import android.app.Application
import androidx.lifecycle.*
import com.shogek.spinoza.models.Contact
import com.shogek.spinoza.models.Conversation
import com.shogek.spinoza.repositories.ContactRepository
import com.shogek.spinoza.repositories.ConversationRepository

class ConversationListViewModel(
    application: Application
) : AndroidViewModel(application) {

    var conversations: LiveData<List<Conversation>> = MutableLiveData()

    init {
        val context = application.applicationContext
        val allConversations = ConversationRepository(context).getAll()
        val allContacts = ContactRepository.getAll(context.contentResolver)

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
}
