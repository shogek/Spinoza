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
import com.shogek.spinoza.CONVERSATION_ID
import com.shogek.spinoza.NO_CONVERSATION_ID
import com.shogek.spinoza.R
import com.shogek.spinoza.SMS_SENT_PENDING_INTENT
import com.shogek.spinoza.adapters.MessageListRecyclerAdapter
import com.shogek.spinoza.models.Conversation
import com.shogek.spinoza.models.Message
import com.shogek.spinoza.repositories.ConversationRepository
import com.shogek.spinoza.repositories.MessageRepository
import kotlinx.android.synthetic.main.activity_message_list.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent

class MessageListActivity : AppCompatActivity() {
    private var sentPI: PendingIntent? = null
    /** Last sent SMS message's text is stored here to be used in intent */
    private lateinit var textSent: String
    private lateinit var messages: MutableList<Message>
    private lateinit var adapter: MessageListRecyclerAdapter
    private lateinit var conversation: Conversation
    private lateinit var sendMessageText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)

        this.initActivity()

        val conversationId = intent.getIntExtra(CONVERSATION_ID, NO_CONVERSATION_ID)
        val conversation = ConversationRepository.get(conversationId) ?: return
        this.conversation = conversation
        if (conversation.messages == null)
            conversation.messages = MessageRepository.get(contentResolver, conversationId)

        val messages = conversation.messages ?: return
        this.messages = messages

        this.initCustomActionBar(conversation.getDisplayName(), conversation.contact?.photoUri)

        // TODO: [Bug] Opening an unread conversation should mark it as read
        val adapter = MessageListRecyclerAdapter(this, messages, conversation.contact?.photoUri)
        this.adapter = adapter
        rv_messageList.adapter = adapter
        rv_messageList.layoutManager = LinearLayoutManager(this)
        rv_messageList.scrollToPosition(messages.size - 1)

        // Scroll to last message on keyboard appear
        KeyboardVisibilityEvent.setEventListener(this) { isVisible ->
            if (isVisible)
                rv_messageList.scrollToPosition(messages.size - 1)
        }

        // Send the typed message
        iv_sendMessageButton.setOnClickListener {
            val textToSend = this.sendMessageText.text.toString()
            if (textToSend.isBlank()) {
                return@setOnClickListener
            }
            this.sendMessageText.text.clear()
            this.textSent = textToSend

            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(this.conversation.senderPhone, null, textToSend, this.getSmsIntent(), null)
        }

        // Return to previous activity on arrow click
        message_list_toolbar_return_iv.setOnClickListener { finish() }
    }

    private fun initActivity() {
        this.sendMessageText = findViewById(R.id.et_sendMessageText)
    }

    private fun initCustomActionBar(title: String, contactPhotoUri: String?) {
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

                // TODO: Bad practice, use something not mutable + we're concerning ourselves about logic in another activity
                val parent = arg0 as MessageListActivity
                val lastMessage = MessageRepository.checkIfMessageSent(contentResolver, parent.conversation.threadId, parent.textSent)
                if (lastMessage == null) {
                    Toast.makeText(baseContext, "Failed to send message", Toast.LENGTH_SHORT).show()
                    return
                }
                parent.adapter.notifyDataSetChanged()
                rv_messageList.scrollToPosition(parent.messages.size - 1)
                parent.conversation.latestMessageTimestamp = lastMessage.dateTimestamp
                parent.conversation.latestMessageText = lastMessage.text
            }
        }

        // STEP 3 - Associate intent with broadcast receiver
        registerReceiver(sendSMS, IntentFilter(SMS_SENT_PENDING_INTENT))

        this.sentPI = sentPI
        return sentPI
    }
}
