package com.shogek.spinoza.activities

import android.app.Activity
import android.app.PendingIntent
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.net.Uri
import android.telephony.SmsManager
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.shogek.spinoza.*
import com.shogek.spinoza.adapters.MessageListRecyclerAdapter
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.models.Message
import com.shogek.spinoza.events.conversations.ConversationOpenedEvent
import com.shogek.spinoza.events.messages.MessageReceivedEvent
import com.shogek.spinoza.events.messages.*
import com.shogek.spinoza.models.Contact
import com.shogek.spinoza.repositories.ContactRepository
import com.shogek.spinoza.repositories.ConversationRepository
import com.shogek.spinoza.repositories.MessageRepository
import com.shogek.spinoza.services.MessageService
import com.shogek.spinoza.ui.messages.list.MessageListViewModel
import kotlinx.android.synthetic.main.activity_message_list.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MessageListActivity : AppCompatActivity() {

//    private lateinit var viewModel: MessageListViewModel
    private lateinit var vm: MessageListViewModel

    companion object {
        const val NO_CONVERSATION_ID = -1L
        const val PENDING_MESSAGE_INTENT = "PENDING_MESSAGE_INTENT"
        const val PENDING_MESSAGE_THREAD = "PENDING_MESSAGE_THREAD"
        const val PENDING_MESSAGE_BODY   = "PENDING_MESSAGE_BODY"
    }

    private var messageIndex = 0
    private var contact: Contact? = null
    private var conversation: Conversation? = null
    private lateinit var messageActionButtons: ConstraintLayout
    private var messages: MutableList<Message> = mutableListOf()
    private lateinit var adapter: MessageListRecyclerAdapter

    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context?, arg1: Intent?) {
            if (resultCode == Activity.RESULT_OK) {
                val messageText = arg1!!.extras!!.getString(PENDING_MESSAGE_BODY)!!
                val conversationId = arg1.extras!!.getInt(PENDING_MESSAGE_THREAD)
                onMessageSentSuccess(conversationId, messageText)
            } else {
                // TODO: [Bug] Handle failed to send message scenario
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)

        val conversationId = intent.getLongExtra(Extra.ConversationList.MessageList.OpenConversation.CONVERSATION_ID, NO_CONVERSATION_ID)
        this.vm = ViewModelProvider(this)
            .get(MessageListViewModel::class.java)
            .init(conversationId)

        when (intent.getStringExtra(Extra.GOAL)) {
            Extra.ConversationList.MessageList.NewMessage.GOAL          -> this.cameFromWriteNewMessage()
//            Extra.ConversationList.MessageList.OpenConversation.GOAL    -> this.cameFromOpenConversation()
            Extra.MessageNotification.MessageList.MessageReceived.GOAL  -> this.cameFromReceivedMessage()
        }

//        EventBus.getDefault().postSticky(ConversationOpenedEvent(this.conversation?.threadId))

        val buttonCopyMessage     = findViewById<ConstraintLayout>(R.id.cl_copyMessageColumn)
        val buttonRemoveMessage   = findViewById<ConstraintLayout>(R.id.cl_removeMessageColumn)
        val buttonForwardMessage  = findViewById<ConstraintLayout>(R.id.cl_forwardMessageColumn)
        this.messageActionButtons = findViewById(R.id.cl_messageActionsRow)

        this.adapter = MessageListRecyclerAdapter(this, buttonCopyMessage, buttonRemoveMessage, buttonForwardMessage, contact?.photoUri)

        this.vm.messages.observe(this, Observer { messages ->
            adapter.setMessages(messages)
            rv_messageList.scrollToPosition(messages.size - 1)
            this.initScrollDownWhenKeyboardAppears(messages.size)
        })

        this.vm.conversation.observe(this, Observer {
            // TODO: [Bug] A conversation is not yet created when sending the first message to a new contact
            this.initButtonSendMessage(it.phone, it.id)
            // TODO: [Style] Add elevation to message box when not at bottom.
            this.setToolbarInformation(it.phone, contact?.photoUri)
        })

        rv_messageList.adapter = this.adapter
        rv_messageList.layoutManager = LinearLayoutManager(this)

        this.initButtonReturn()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun onMessageSentSuccess(
        threadId: Number,
        messageText: String
    ) {
        val sentMessage = MessageRepository(this).messageSent(threadId, messageText)
        ConversationRepository(this).messageSent(threadId, sentMessage)
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
        val conversationId = intent.getLongExtra(Extra.ConversationList.MessageList.OpenConversation.CONVERSATION_ID, NO_CONVERSATION_ID)
        val repository = ConversationRepository(this)
        val conversation = repository.get(conversationId)!!
//        this.conversation = conversation

        if (!conversation.latestMessageWasRead) {
//            this.viewModel.markConversationAsRead()
        }

        this.contact = ContactRepository(this)
            .getAll().value!!
            .find { c -> c.strippedPhone == conversation.senderPhoneStripped }
    }

    private fun cameFromWriteNewMessage() {
        val conversationId = intent.getLongExtra(Extra.ConversationList.MessageList.NewMessage.CONVERSATION_ID, NO_CONVERSATION_ID)
        val contactId = intent.getStringExtra(Extra.ConversationList.MessageList.NewMessage.CONTACT_ID)!!

        this.contact = ContactRepository(this).get(contactId)

        if (conversationId != NO_CONVERSATION_ID) {
//            this.conversation = ConversationRepository(this).get(conversationId)
//            this.messages = MessageCache
//                .getAll(contentResolver, conversationId)
//                .toMutableList()
        } else {
            // Never exchanged messages with this contact before
            this.messages = mutableListOf()
        }
    }

    private fun cameFromReceivedMessage() {
//        val conversationId = intent.getLongExtra(Extra.MessageNotification.MessageList.MessageReceived.CONVERSATION_ID, NO_CONVERSATION_ID)
//        this.conversation = ConversationRepository(this).get(conversationId)
//        this.contact = ContactRepository(this)
//            .getAll().value!!
//            .find { c -> c.strippedPhone == this.conversation!!.senderPhoneStripped }
    }

    private fun initScrollDownWhenKeyboardAppears(messageCount: Int) {
        KeyboardVisibilityEvent.setEventListener(this) { isVisible ->
            // TODO: test if it's not called multiple times
            if (isVisible) {
                rv_messageList.scrollToPosition(messageCount - 1)
            }
        }
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

        Glide.with(this)
             .load(Uri.parse(contactPhotoUri ?: ""))
             .apply(RequestOptions().placeholder(R.drawable.unknown_contact))
             .into(message_list_toolbar_sender_photo_civ)
    }

    /** Simply to differentiate between broadcast's pending intents - otherwise it returns the same one */
    private fun getMessageCode() : Number {
        val number = this.messageIndex
        this.messageIndex++
        return number
    }

    private fun showMessageActionButtons() {
        this.messageActionButtons.visibility = View.VISIBLE
    }

    private fun hideMessageActionButtons() {
        this.messageActionButtons.visibility = View.GONE
    }

    @Subscribe
    fun onMessageReceivedEvent(event: MessageReceivedEvent) {
//        if (event.conversationId == this.conversation?.threadId) {
//            this.messages.add(event.message)
//            this.adapter.notifyDataSetChanged()
//            rv_messageList.scrollToPosition(messages.size - 1)
//        }
    }

    @Subscribe
    fun onMessageLongClicked(event: MessageLongClickedEvent) {
        this.showMessageActionButtons()
    }

    @Subscribe
    fun onMessageClicked(event: MessageClickedEvent) {
        this.hideMessageActionButtons()
    }

    @Subscribe
    fun onMessageForwarded(event: MessageForwardedEvent) {
        this.hideMessageActionButtons()
        val intent = Intent(this, ContactListForwardActivity::class.java)
        intent.putExtra(Extra.MessageList.ContactListForward.ForwardMessage.MESSAGE, event.text)
        this.startActivity(intent)
    }

    @Subscribe
    fun onMessageCopied(event: MessageCopiedEvent) {
        this.hideMessageActionButtons()
        // TODO: [Task] Create a new toast background
        val clipData = ClipData.newPlainText("", event.text) // 'label' is for developers only
        val clipManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipManager.setPrimaryClip(clipData)
        Toast.makeText(this, "Copied", Toast.LENGTH_LONG).show()
    }

    @Subscribe
    fun onMessageDeleted(event: MessageDeletedEvent) {
        this.hideMessageActionButtons()
        MessageService.delete(this.contentResolver, event.messageId)
    }
}
