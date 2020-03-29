package com.shogek.spinoza.ui.messages.list

import android.app.Application
import android.content.*
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.*
import com.shogek.spinoza.R
import com.shogek.spinoza.Extra
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationRepository
import com.shogek.spinoza.db.message.Message
import com.shogek.spinoza.db.message.MessageRepository
import com.shogek.spinoza.helpers.MessageSendingService
import com.shogek.spinoza.ui.contacts.forward.ContactListForwardActivity
import com.shogek.spinoza.ui.state.CommonState
import kotlinx.coroutines.launch


class MessageListViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.baseContext
    private val messageRepository = MessageRepository(application, viewModelScope)
    private val conversationRepository = ConversationRepository(application, viewModelScope)
    private val messageSendingService = MessageSendingService(application, viewModelScope)
    /** Stores the currently selected message for a later action (copy/forward/delete). */
    private var selectedMessage: Message? = null
    /** Stores the current conversation record which is extracted from the LiveData version. */
    private var currentConversation: Conversation? = null
    lateinit var conversation: LiveData<Conversation>

    private companion object {
        private val TAG = MessageListViewModel::class.java.simpleName
    }


    private fun cameFromOpenConversation(intent: Intent): LiveData<Conversation> {
        val conversationId = intent.getLongExtra(Extra.ConversationList.MessageList.OpenConversation.CONVERSATION_ID, -1)
        return conversationRepository.getWithContactAndMessagesObservable(conversationId)
    }

    /** User picked a contact with whom we may OR MAY NOT have a conversation with already. */
    private fun cameFromWriteNewMessage(intent: Intent): LiveData<Conversation> {
        val contactId = intent.getLongExtra(Extra.ConversationList.MessageList.NewMessage.CONTACT_ID, -1)
        return conversationRepository.getByContactIdObservable(contactId)
    }

    /** Clicked on a notification when a message was received. */
    private fun cameFromMessageNotification(intent: Intent): LiveData<Conversation> {
        val conversationId = intent.getLongExtra(Extra.MessageNotification.MessageList.MessageReceived.CONVERSATION_ID, -1)
        return conversationRepository.getObservable(conversationId)
    }

    private fun extractActiveConversation(conversation: Conversation): Conversation {
        CommonState.setCurrentOpenConversationId(conversation.id)
        this.currentConversation = conversation
        return conversation
    }

    fun init(intent: Intent): MessageListViewModel {
        val conversationData = when (intent.getStringExtra(Extra.GOAL)) {
            Extra.ConversationList.MessageList.NewMessage.GOAL          -> this.cameFromWriteNewMessage(intent)
            Extra.ConversationList.MessageList.OpenConversation.GOAL    -> this.cameFromOpenConversation(intent)
            Extra.MessageNotification.MessageList.MessageReceived.GOAL  -> this.cameFromMessageNotification(intent)
            else -> throw IllegalArgumentException("Unknown goal type!")
        }
        this.conversation = Transformations.map(conversationData, ::extractActiveConversation)
        return this
    }

    fun markConversationAsRead(conversation: Conversation) {
        if (conversation.snippetWasRead) {
            return
        }

        conversation.snippetWasRead = true
        viewModelScope.launch {
            conversationRepository.update(conversation)
        }
    }

    fun onMessageClick() {
        this.selectedMessage = null
    }

    fun onMessageLongClick(message: Message) {
        this.selectedMessage = message
    }

    fun copyMessage() {
        val message = this.selectedMessage
        if (message == null) {
            Log.e(TAG, "No message to copy!"); return
        }

        val clipData = ClipData.newPlainText("", message.body) // 'label' is for developers only
        val clipManager = getSystemService(this.context, ClipboardManager::class.java)
        if (clipManager == null) {
            Log.e(TAG, "ClipboardManager is null!"); return
        }

        clipManager.setPrimaryClip(clipData)
        Toast.makeText(this.context, "Copied: " + message.body, Toast.LENGTH_LONG).show()
    }

    fun removeMessage() {
        val message = this.selectedMessage
        if (message == null) {
            Log.e(TAG, "No message to remove!"); return
        }

        viewModelScope.launch {
            messageRepository.delete(message)
        }
    }

    fun forwardMessage() {
        val message = this.selectedMessage
        if (message == null) {
            Log.e(TAG, "No message to forward!"); return
        }

        val intent = Intent(this.context, ContactListForwardActivity::class.java)
        intent.putExtra(Extra.MessageList.ContactListForward.ForwardMessage.MESSAGE, message.body)
        this.context.startActivity(intent)
    }

    fun sendMessage(text: String) {
        val conversation = this.currentConversation!!
        this.messageSendingService.sendMessage(conversation, text, ::onMessageSendSuccess, ::onMessageSendFail)
    }

    private fun onMessageSendSuccess(message: Message) { }

    private fun onMessageSendFail() {
        val text = context.getString(R.string.message_list_text_failed_to_send)
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    fun onActivityResume() {
        if (currentConversation != null) {
            CommonState.setCurrentOpenConversationId(currentConversation!!.id)
        }
    }

    fun onActivityPause() {
        CommonState.clearCurrentOpenConversationId()
    }
}