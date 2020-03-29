package com.shogek.spinoza.ui.contacts.forward

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shogek.spinoza.Extra
import com.shogek.spinoza.db.contact.Contact
import com.shogek.spinoza.db.contact.ContactRepository
import com.shogek.spinoza.db.conversation.ConversationRepository
import com.shogek.spinoza.db.message.Message
import com.shogek.spinoza.helpers.MessageSendingService
import kotlinx.coroutines.launch

class ContactListForwardViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.baseContext
    private val conversationRepository = ConversationRepository(context, viewModelScope)
    private val contactRepository = ContactRepository(context, viewModelScope)
    private val messageSendingService = MessageSendingService(context, viewModelScope)

    val contacts = contactRepository.getAllObservable()
    /** A message that the user wants to forward to other contacts. */
    private var textToForward: String? = null


    fun init(intent: Intent): ContactListForwardViewModel {
        val text = intent.getStringExtra(Extra.MessageList.ContactListForward.ForwardMessage.MESSAGE)
            ?: throw ExceptionInInitializerError("Intent did not contain a message to forward to other contacts!")

        this.textToForward = text
        return this
    }

    fun forwardMessage(contact: Contact) = viewModelScope.launch {
        // TODO: [Refactor] Only update database when we get a confirmation that message was sent.
        // TODO: [Feature] Disable send button to indicate that then message is being sent.
        val conversation = conversationRepository.getByContactId(contact.id)
        messageSendingService.sendMessage(conversation, textToForward!!, ::onMessageSendSuccess, ::onMessageSendError)
    }

    private fun onMessageSendSuccess(message: Message) {
        // TODO: Implement
    }

    private fun onMessageSendError() {
        // TODO: Implement
    }
}