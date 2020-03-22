package com.shogek.spinoza.ui.contacts.forward

import android.app.Application
import android.content.Intent
import android.telephony.SmsManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shogek.spinoza.Extra
import com.shogek.spinoza.db.contact.Contact
import com.shogek.spinoza.db.contact.ContactRepository
import com.shogek.spinoza.db.conversation.ConversationRepository
import com.shogek.spinoza.db.message.Message
import com.shogek.spinoza.db.message.MessageRepository
import kotlinx.coroutines.launch

class ContactListForwardViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.baseContext
    private val conversationRepository = ConversationRepository(context, viewModelScope)
    private val contactRepository = ContactRepository(context, viewModelScope)
    private val messageRepository = MessageRepository(context, viewModelScope)

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
        val timestamp = System.currentTimeMillis()

        // TODO: [Refactor] Only update database when we get a confirmation that message was sent.
        // TODO: [Feature] Disable send button to indicate that then message is being sent.
        val conversation = conversationRepository.getByContactId(contact.id)
        conversation.snippet = textToForward!!
        conversation.snippetTimestamp = timestamp
        conversation.snippetWasRead = true
        conversation.snippetIsOurs = true
        conversationRepository.update(conversation)

        val message = Message(null, conversation.id, textToForward!!, timestamp, isOurs = true)
        messageRepository.insert(message)

        SmsManager.getDefault().sendTextMessage(conversation.phone, null, textToForward, null, null)
    }
}