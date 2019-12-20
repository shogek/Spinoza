package com.shogek.spinoza.activities

import android.app.Activity
import android.app.PendingIntent
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.net.Uri
import android.view.View
import android.widget.EditText
import android.widget.Toast
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
import com.shogek.spinoza.services.MessageService
import kotlinx.android.synthetic.main.activity_message_list.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent

class MessageListActivity : AppCompatActivity() {

    companion object {
        const val NO_CONVERSATION_ID = -1
    }

    private var contact: Contact? = null
    private var conversation: Conversation? = null

    private var sentPI: PendingIntent? = null
    private lateinit var messages: MutableList<Message>
    private lateinit var sendMessageText: EditText

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
        val adapter = MessageListRecyclerAdapter(this, core, messages, contact?.photoUri)
        rv_messageList.adapter = adapter
        rv_messageList.layoutManager = LinearLayoutManager(this)
        rv_messageList.scrollToPosition(messages.size - 1)

        this.initScrollDownWhenKeyboardAppears(messages.size)
        this.initButtonReturn()
        this.initButtonSendMessage(contactPhone, adapter)
        this.initRowButtonActions(core)
        this.setToolbarInformation(contactName, contact?.photoUri)
        this.sendMessageText = findViewById(R.id.et_sendMessageText)
        // TODO: [Style] Add elevation to message box when not at bottom.
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
        adapter: MessageListRecyclerAdapter
    ) {
        // Send the typed message
        iv_sendMessageButton.setOnClickListener {
            val message = this.sendMessageText.text.toString()
            if (message.isBlank()) {
                return@setOnClickListener
            }

            this.sendMessageText.text.clear()
            MessageService.send(recipientNumber, message, this.getSmsIntent(adapter), null)
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

    private fun getSmsIntent(adapter: MessageListRecyclerAdapter): PendingIntent {
        val smsIntent = this.sentPI
        if (smsIntent != null)
            return smsIntent

        // STEP 1 - create intent
        val sentPI = PendingIntent.getBroadcast(this, 0, Intent(SMS_SENT_PENDING_INTENT), 0)

        // STEP 2 - create BroadcastReceiver
        val sendSMS: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(arg0: Context?, arg1: Intent?) {
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(baseContext, "Failed to send message", Toast.LENGTH_SHORT).show()
                    return
                }

                // TODO: [Style] If we're here, it means message was sent - make speech bubble darker
                val parent = arg0 as MessageListActivity
                adapter.notifyDataSetChanged()
                rv_messageList.scrollToPosition(parent.messages.size)

                // TODO: [Bug] Conversation list isn't updated with newest messages if any were sent
                // TODO: [Bug] Message list isn't updated with new messages
            }
        }

        // STEP 3 - Associate intent with broadcast receiver
        registerReceiver(sendSMS, IntentFilter(SMS_SENT_PENDING_INTENT))

        this.sentPI = sentPI
        return sentPI
    }
}
