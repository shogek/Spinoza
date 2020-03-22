package com.shogek.spinoza.ui.messages.list

import android.app.Activity
import android.app.Application
import android.app.PendingIntent
import android.content.*
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.*
import com.shogek.spinoza.Extra
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationRepository
import com.shogek.spinoza.db.message.Message
import com.shogek.spinoza.db.message.MessageRepository
import com.shogek.spinoza.ui.contacts.forward.ContactListForwardActivity
import com.shogek.spinoza.ui.state.CommonState
import kotlinx.coroutines.launch


class MessageListViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.baseContext
    private val messageRepository = MessageRepository(application, viewModelScope)
    private val conversationRepository = ConversationRepository(application, viewModelScope)
    /** Stores the currently selected message for a later action (copy/forward/delete). */
    private var selectedMessage: Message? = null
    private var messageIndex = 0
    private var currentConversation: Conversation? = null
    lateinit var conversation: LiveData<Conversation>

    private companion object {
        private val TAG = MessageListViewModel::class.java.simpleName

        const val PENDING_MESSAGE_INTENT = "PENDING_MESSAGE_INTENT"
        const val PENDING_MESSAGE_CONVERSATION_ID = "PENDING_MESSAGE_THREAD"
        const val PENDING_MESSAGE_TIMESTAMP = "PENDING_MESSAGE_TIMESTAMP"
        const val PENDING_MESSAGE_BODY = "PENDING_MESSAGE_BODY"
    }


    private fun cameFromOpenConversation(intent: Intent): MessageListViewModel {
        val conversationId = intent.getLongExtra(Extra.ConversationList.MessageList.OpenConversation.CONVERSATION_ID, -1)
        val conversationData = conversationRepository.getWithContactAndMessagesObservable(conversationId)
        this.conversation = Transformations.map(conversationData, ::extractActiveConversation)
        return this
    }

    /** User picked a contact with whom we may OR MAY NOT have a conversation with already. */
    private fun cameFromWriteNewMessage(intent: Intent): MessageListViewModel {
        val contactId = intent.getLongExtra(Extra.ConversationList.MessageList.NewMessage.CONTACT_ID, -1)
        this.conversation = conversationRepository.getByContactIdObservable(contactId)
        return this
    }

    /** Clicked on a notification when a message was received. */
    private fun cameFromReceivedMessage(intent: Intent): MessageListViewModel {
        // TODO: [Feature] Implement this
        return this
    }

    private fun extractActiveConversation(conversation: Conversation): Conversation {
        CommonState.setCurrentOpenConversationId(conversation.id)
        this.currentConversation = conversation
        return conversation
    }

    fun init(intent: Intent): MessageListViewModel {
        return when (intent.getStringExtra(Extra.GOAL)) {
            Extra.ConversationList.MessageList.NewMessage.GOAL          -> this.cameFromWriteNewMessage(intent)
            Extra.ConversationList.MessageList.OpenConversation.GOAL    -> this.cameFromOpenConversation(intent)
            Extra.MessageNotification.MessageList.MessageReceived.GOAL  -> this.cameFromReceivedMessage(intent)
            else -> throw IllegalArgumentException("Unknown goal type!")
        }
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

        val intent = Intent(PENDING_MESSAGE_INTENT)
        intent.putExtra(PENDING_MESSAGE_CONVERSATION_ID, conversation.id)
        intent.putExtra(PENDING_MESSAGE_TIMESTAMP, System.currentTimeMillis())
        intent.putExtra(PENDING_MESSAGE_BODY, text)

        val pendingIntent = PendingIntent.getBroadcast(context, this.getMessageCode(), intent, 0)
        SmsManager
            .getDefault()
            .sendTextMessage(conversation.phone, null, text, pendingIntent, null)
    }

    // TODO: [Refactor] Move to common - message forwarding should use the same thing
    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context?, arg1: Intent?) {
            val extras = arg1!!.extras!!
            val conversationId = extras.getLong(PENDING_MESSAGE_CONVERSATION_ID)
            val timestamp = extras.getLong(PENDING_MESSAGE_TIMESTAMP)
            val text = extras.getString(PENDING_MESSAGE_BODY)!!

            if (resultCode == Activity.RESULT_OK) {
                onMessageSendSuccess(conversationId, text, timestamp)
            } else {
                onMessageSendFail()
            }
        }
    }

    private fun onMessageSendSuccess(
        conversationId: Long,
        messageText: String,
        timestamp: Long
    ) = viewModelScope.launch {
        val conversation = currentConversation!!
        conversation.snippet = messageText
        conversation.snippetTimestamp = timestamp
        conversation.snippetWasRead = true
        conversation.snippetIsOurs = true
        conversationRepository.update(conversation)

        val message = Message(null, conversationId, messageText, timestamp, isOurs = true)
        messageRepository.insert(message)
    }

    private fun onMessageSendFail() {
        // TODO: [Bug] Handle failed to send message scenario
    }

    /** Simply to differentiate between broadcast's pending intents - otherwise it returns the same one. */
    private fun getMessageCode(): Int {
        val number = this.messageIndex
        this.messageIndex++
        return number
    }

    fun onActivityResume() {
        if (currentConversation != null) {
            CommonState.setCurrentOpenConversationId(currentConversation!!.id)
        }
        context.registerReceiver(this.messageReceiver, IntentFilter(PENDING_MESSAGE_INTENT))
    }

    fun onActivityPause() {
        CommonState.clearCurrentOpenConversationId()
        context.unregisterReceiver(this.messageReceiver)
    }
}