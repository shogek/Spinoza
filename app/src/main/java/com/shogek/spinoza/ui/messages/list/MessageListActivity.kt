package com.shogek.spinoza.ui.messages.list

import android.app.Activity
import android.app.PendingIntent
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.net.Uri
import android.telephony.SmsManager
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.shogek.spinoza.*
import com.shogek.spinoza.db.contact.Contact
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.message.Message
import com.shogek.spinoza.ui.state.CommonState
import kotlinx.android.synthetic.main.activity_message_list.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent

class MessageListActivity : AppCompatActivity() {

    private lateinit var viewModel: MessageListViewModel

    companion object {
        const val PENDING_MESSAGE_INTENT = "PENDING_MESSAGE_INTENT"
        const val PENDING_MESSAGE_THREAD = "PENDING_MESSAGE_THREAD"
        const val PENDING_MESSAGE_BODY   = "PENDING_MESSAGE_BODY"
    }

    private var messageIndex = 0
    private var contact: Contact? = null
    private var conversation: Conversation? = null
    private lateinit var messageActionButtons: ConstraintLayout
    private var messages: MutableList<Message> = mutableListOf()
    private lateinit var adapter: MessageListAdapter

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

        this.viewModel = ViewModelProvider(this)
            .get(MessageListViewModel::class.java)
            .init(intent)

        findViewById<ConstraintLayout>(R.id.cl_copyMessageColumn).setOnClickListener { this.onClickCopyMessage() }
        findViewById<ConstraintLayout>(R.id.cl_removeMessageColumn).setOnClickListener { this.onClickRemoveMessage() }
        findViewById<ConstraintLayout>(R.id.cl_forwardMessageColumn).setOnClickListener { this.onClickForwardMessage() }
        this.messageActionButtons = findViewById(R.id.cl_messageActionsRow)

        this.adapter = MessageListAdapter(this, ::onMessageClick, ::onMessageLongClick)

        this.viewModel.conversation.observe(this, Observer { conversation ->
            CommonState.setCurrentOpenConversationId(conversation.id)

            this.initButtonSendMessage(conversation.phone, conversation.id)
            // TODO: [Style] Add elevation to message box when not at bottom.
            val title = conversation.contact?.getDisplayTitle() ?: conversation.phone
            this.setToolbarInformation(title, conversation.contact?.photoUri)

            this.viewModel.markConversationAsRead(conversation)

            if (conversation.contact != null) {
                val contact = conversation.contact!!
                adapter.setContactImage(contact.photoUri)
            }

            if (conversation.messages != null) {
                val messages = conversation.messages!!
                adapter.setMessages(messages)
                rv_messageList.scrollToPosition(messages.size - 1)
                this.initScrollDownWhenKeyboardAppears(messages.size)
            }
        })

        rv_messageList.adapter = this.adapter
        rv_messageList.layoutManager = LinearLayoutManager(this)

        this.initButtonReturn()
    }

    private fun onMessageClick() {
        this.viewModel.onMessageClick()
        this.hideMessageActionButtons()
    }

    private fun onMessageLongClick(message: Message) {
        this.viewModel.onMessageLongClick(message)
        this.showMessageActionButtons()
    }

    private fun onClickCopyMessage() {
        this.hideMessageActionButtons()
        this.viewModel.copyMessage()
    }

    private fun onClickRemoveMessage() {
        this.hideMessageActionButtons()
        this.viewModel.removeMessage()
    }

    private fun onClickForwardMessage() {
        this.hideMessageActionButtons()
        this.viewModel.forwardMessage()
    }

    private fun showMessageActionButtons() {
        this.messageActionButtons.visibility = View.VISIBLE
    }

    private fun hideMessageActionButtons() {
        this.messageActionButtons.visibility = View.GONE
    }

    private fun onMessageSentSuccess(
        threadId: Number,
        messageText: String
    ) {
//        val sentMessage = MessageRepository(this).messageSent(threadId, messageText)
//        ConversationRepository(this).messageSent(threadId, sentMessage)
    }

    override fun onResume() {
        super.onResume()
        this.viewModel.conversation.observe(this, Observer { conversation ->
            if (conversation != null) {
                CommonState.setCurrentOpenConversationId(conversation.id)
            }
        })
        registerReceiver(this.messageReceiver, IntentFilter(PENDING_MESSAGE_INTENT))
    }

    override fun onPause() {
        super.onPause()
        CommonState.clearCurrentOpenConversationId()
        unregisterReceiver(this.messageReceiver)
    }

    private fun initScrollDownWhenKeyboardAppears(messageCount: Int) {
        KeyboardVisibilityEvent.setEventListener(this) { isVisible ->
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
             .placeholder(R.drawable.unknown_contact)
             .into(message_list_toolbar_sender_photo_civ)
    }

    /** Simply to differentiate between broadcast's pending intents - otherwise it returns the same one */
    private fun getMessageCode() : Number {
        val number = this.messageIndex
        this.messageIndex++
        return number
    }
}
