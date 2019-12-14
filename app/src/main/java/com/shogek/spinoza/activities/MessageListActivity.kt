package com.shogek.spinoza.activities

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
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
import com.shogek.spinoza.models.Contact
import kotlinx.android.synthetic.main.activity_message_list.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent

class MessageListActivity : AppCompatActivity() {

    private var sentPI: PendingIntent? = null
    /** Last sent SMS message's text is stored here to be used in intent */
    private lateinit var messages: MutableList<Message>
    private var conversation: Conversation? = null
    private var contact: Contact? = null
    private lateinit var adapter: MessageListRecyclerAdapter
    private lateinit var sendMessageText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)

        this.initActivity()

        val conversationId = intent.getIntExtra(CONVERSATION_ID, NO_CONVERSATION_ID)

        // TODO: [Bug] Contact exists without a conversation (we're writing the first message)
        if (conversationId == NO_CONVERSATION_ID) {
            val contactId = intent.getStringExtra(CONTACT_ID)
            this.contact = ContactCache.get(contentResolver, contactId!!)
            this.messages = mutableListOf()
        } else {
            this.conversation = ConversationCache.get(conversationId)
            this.messages = MessageCache
                .getAll(contentResolver, conversationId)
                .toMutableList()
        }

        val contactName = this.conversation?.getDisplayName() ?: this.contact!!.displayName
        val contactPhotoUri = this.conversation?.contact?.photoUri ?: this.contact?.photoUri

        // TODO: [Bug] Opening an unread conversation should mark it as read
        this.adapter = MessageListRecyclerAdapter(this, messages, contactPhotoUri)
        rv_messageList.adapter = this.adapter
        rv_messageList.layoutManager = LinearLayoutManager(this)
        rv_messageList.scrollToPosition(messages.size - 1)

        this.initScrollDownWhenKeyboardAppears(messages.size)
        this.initButtonReturn()
        this.initButtonSendMessage()
        this.setToolbarInformation(contactName, contactPhotoUri)
        // TODO: [Style] Add elevation to message box when not at bottom.
    }

    private fun initScrollDownWhenKeyboardAppears(messageCount: Int) {
        KeyboardVisibilityEvent.setEventListener(this) { isVisible ->
            if (isVisible) {
                rv_messageList.scrollToPosition(messageCount - 1)
            }
        }
    }

    private fun initButtonSendMessage() {
        // Send the typed message
        iv_sendMessageButton.setOnClickListener {
            val message = this.sendMessageText.text.toString()
            if (message.isBlank()) {
                return@setOnClickListener
            }

            this.sendMessageText.text.clear()

            val recipient = this.conversation?.senderPhoneStripped ?: this.contact!!.strippedPhone

            SmsManager
                .getDefault()
                .sendTextMessage(recipient, null, message, this.getSmsIntent(), null)
        }
    }

    private fun initButtonReturn() {
        message_list_toolbar_return_iv.setOnClickListener { finish() }
    }

    private fun initActivity() {
        this.sendMessageText = findViewById(R.id.et_sendMessageText)
    }

    private fun setToolbarInformation(title: String,
                                      contactPhotoUri: String?
    ) {
        message_list_toolbar_title_tv.text = title

        // Show the contact photo or hide the bubble completely
        if (contactPhotoUri != null)
            message_list_toolbar_sender_photo_civ.setImageURI(Uri.parse(contactPhotoUri))
        else
            message_list_toolbar_sender_photo_civ.visibility = View.GONE
    }

    private fun getSmsIntent(): PendingIntent {
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
                parent.adapter.notifyDataSetChanged()
                rv_messageList.scrollToPosition(parent.messages.size)

                // TODO: [Bug] Conversation list isn't updated with newest messages if any were sent
            }
        }

        // STEP 3 - Associate intent with broadcast receiver
        registerReceiver(sendSMS, IntentFilter(SMS_SENT_PENDING_INTENT))

        this.sentPI = sentPI
        return sentPI
    }
}
