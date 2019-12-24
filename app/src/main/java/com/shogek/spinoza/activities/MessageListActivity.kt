package com.shogek.spinoza.activities

import android.app.Activity
import android.app.PendingIntent
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.net.Uri
import android.telephony.SmsManager
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.shogek.spinoza.*
import com.shogek.spinoza.adapters.MessageListRecyclerAdapter
import com.shogek.spinoza.caches.ContactCache
import com.shogek.spinoza.models.Conversation
import com.shogek.spinoza.models.Message
import com.shogek.spinoza.caches.ConversationCache
import com.shogek.spinoza.caches.MessageCache
import com.shogek.spinoza.cores.MessageListCore
import com.shogek.spinoza.models.Contact
import kotlinx.android.synthetic.main.activity_message_list.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent

class MessageListActivity : AppCompatActivity() {

    companion object {
        const val NO_CONVERSATION_ID = -1
        const val PENDING_MESSAGE_INTENT = "PENDING_MESSAGE_INTENT"
        const val PENDING_MESSAGE_BODY = "PENDING_MESSAGE_BODY"
        const val PENDING_MESSAGE_THREAD = "PENDING_MESSAGE_THREAD"
    }

    private var messageIndex = 0
    private var contact: Contact? = null
    private var conversation: Conversation? = null
    private lateinit var messages: MutableList<Message>
    private lateinit var adapter: MessageListRecyclerAdapter

    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context?, arg1: Intent?) {
            if (resultCode == Activity.RESULT_OK) {
                val messageText = arg1!!.extras!!.getString(PENDING_MESSAGE_BODY)!!
                val conversationId = arg1.extras!!.getInt(PENDING_MESSAGE_THREAD)
                onMessageSentSuccess(conversationId, messageText)
            } else {
                onMessageSentError()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)

        when (intent.getStringExtra(Extra.GOAL)) {
            Extra.ConversationList.MessageList.NewMessage.GOAL          -> this.cameFromWriteNewMessage()
            Extra.ConversationList.MessageList.OpenConversation.GOAL    -> this.cameFromOpenConversation()
            Extra.MessageNotification.MessageList.MessageReceived.GOAL  -> this.cameFromReceivedMessage()
        }

        val core = MessageListCore(
            this,
            contact?.id,
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager,
            findViewById(R.id.cl_messageActionsRow)
        )

        val contactName = contact?.displayName ?: conversation!!.getDisplayName()
        val contactPhone = contact?.strippedPhone ?: conversation!!.senderPhoneStripped

        // TODO: [Bug] Opening an unread conversation should mark it as read
        this.adapter = MessageListRecyclerAdapter(this, core, messages, contact?.photoUri)
        rv_messageList.adapter = this.adapter
        rv_messageList.layoutManager = LinearLayoutManager(this)
        rv_messageList.scrollToPosition(messages.size - 1)

        this.initScrollDownWhenKeyboardAppears(messages.size)
        this.initButtonReturn()
        // TODO: [Bug] A conversation is not yet created when sending the first message to a new contact
        this.initButtonSendMessage(contactPhone, conversation!!.threadId)
        this.initRowButtonActions(core)
        this.setToolbarInformation(contactName, contact?.photoUri)
        // TODO: [Style] Add elevation to message box when not at bottom.
    }

    private fun onMessageSentSuccess(
        threadId: Number,
        messageBody: String
    ) {
        // TODO: [Style] If we're here, it means message was sent - make speech bubble darker
        // TODO: [Style] Scroll to bottom when message sent
        val sentMessage = MessageCache.notifyMessageSent(contentResolver, threadId, messageBody)
        ConversationCache.notifyMessageSent(threadId, sentMessage)

        this.messages.add(sentMessage)
        this.adapter.notifyDataSetChanged()
        rv_messageList.scrollToPosition(this.messages.size)
    }

    private fun onMessageSentError() {
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(this.messageReceiver, IntentFilter(PENDING_MESSAGE_INTENT))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(this.messageReceiver)
    }

    private fun cameFromOpenConversation() {
        val conversationId = intent.getIntExtra(Extra.ConversationList.MessageList.OpenConversation.CONVERSATION_ID, NO_CONVERSATION_ID)
        val conversation = ConversationCache.get(conversationId)!!

        this.conversation = conversation
        this.messages = MessageCache
            .getAll(contentResolver, conversationId)
            .toMutableList()
        this.contact = ContactCache
            .getAll(contentResolver)
            .find { c -> c.strippedPhone == conversation.senderPhoneStripped }
    }

    private fun cameFromWriteNewMessage() {
        val conversationId = intent.getIntExtra(Extra.ConversationList.MessageList.NewMessage.CONVERSATION_ID, NO_CONVERSATION_ID)
        val contactId = intent.getStringExtra(Extra.ConversationList.MessageList.NewMessage.CONTACT_ID)!!

        this.contact = ContactCache.get(contentResolver, contactId)

        if (conversationId != NO_CONVERSATION_ID) {
            this.conversation = ConversationCache.get(conversationId)
            this.messages = MessageCache
                .getAll(contentResolver, conversationId)
                .toMutableList()
        } else {
            // Never exchanged messages with this contact before
            this.messages = mutableListOf()
        }
    }

    private fun cameFromReceivedMessage() {
        val conversationId = intent.getIntExtra(Extra.MessageNotification.MessageList.MessageReceived.CONVERSATION_ID, NO_CONVERSATION_ID)
        this.conversation = ConversationCache.get(conversationId)
        this.contact = ContactCache
            .getAll(contentResolver)
            .find { c -> c.strippedPhone == conversation!!.senderPhoneStripped }
        this.messages = MessageCache
            .getAll(contentResolver, conversationId)
            .toMutableList()
    }

    private fun initScrollDownWhenKeyboardAppears(messageCount: Int) {
        KeyboardVisibilityEvent.setEventListener(this) { isVisible ->
            if (isVisible) {
                rv_messageList.scrollToPosition(messageCount - 1)
            }
        }
    }

    private fun initRowButtonActions(core: MessageListCore) {
        cl_copyMessageColumn.setOnClickListener { core.onClickCopy() }
        cl_forwardMessageColumn.setOnClickListener { core.onClickForwardMessage() }
        cl_removeMessageColumn.setOnClickListener { core.onClickRemoveMessage() }
    }

    private fun initButtonSendMessage(
        recipientNumber: String,
        conversationId: Number
    ) {
        iv_sendMessageButton.setOnClickListener {
            val message = et_sendMessageText.text.toString()
            if (message.isBlank()) {
                return@setOnClickListener
            }

            et_sendMessageText.text.clear()

            val intent = Intent(PENDING_MESSAGE_INTENT)
            intent.putExtra(PENDING_MESSAGE_BODY, message)
            intent.putExtra(PENDING_MESSAGE_THREAD, conversationId)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                this.getMessageCode() as Int,
                intent,
                0
            )
            SmsManager
                .getDefault()
                .sendTextMessage(recipientNumber, null, message, pendingIntent, null)
        }
    }

    private fun initButtonReturn() {
        message_list_toolbar_return_iv.setOnClickListener { finish() }
    }

    private fun setToolbarInformation(
        title: String,
        contactPhotoUri: String?
    ) {
        message_list_toolbar_title_tv.text = title

        if (contactPhotoUri != null)
            message_list_toolbar_sender_photo_civ.setImageURI(Uri.parse(contactPhotoUri))
        else
            message_list_toolbar_sender_photo_civ.visibility = View.GONE
    }

    /** Simply to differentiate between broadcast's pending intents - otherwise it returns the same one */
    private fun getMessageCode() : Number {
        val number = this.messageIndex
        this.messageIndex++
        return number
    }
}
