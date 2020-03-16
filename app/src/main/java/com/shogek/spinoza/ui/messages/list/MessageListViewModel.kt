package com.shogek.spinoza.ui.messages.list

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.shogek.spinoza.Extra
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationRepository
import com.shogek.spinoza.db.message.Message
import com.shogek.spinoza.db.message.MessageRepository
import com.shogek.spinoza.ui.contacts.forward.ContactListForwardActivity
import kotlinx.coroutines.launch


class MessageListViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.baseContext
    private val messageRepository = MessageRepository(application, viewModelScope)
    private val conversationRepository = ConversationRepository(application, viewModelScope)
    /** Stores the currently selected message for a later action (copy/forward/delete). */
    private var selectedMessage: Message? = null
    lateinit var conversation: LiveData<Conversation>

    companion object {
        private val TAG = MessageListViewModel::class.java.simpleName
    }


    fun init(conversationId: Long): MessageListViewModel {
        this.conversation = conversationRepository.getWithContactAndMessagesObservable(conversationId)
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
}