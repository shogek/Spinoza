package com.shogek.spinoza.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.shogek.spinoza.CONVERSATION_ID
import com.shogek.spinoza.NO_CONVERSATION_ID
import com.shogek.spinoza.R
import com.shogek.spinoza.adapters.MessageListRecyclerAdapter
import com.shogek.spinoza.models.Conversation
import com.shogek.spinoza.models.Message
import com.shogek.spinoza.repositories.ConversationRepository
import com.shogek.spinoza.repositories.MessageRepository
import kotlinx.android.synthetic.main.activity_message_list.*

class MessageListActivity : AppCompatActivity() {
    private lateinit var messages: MutableList<Message>
    private lateinit var adapter: MessageListRecyclerAdapter
    private lateinit var conversation: Conversation
    private lateinit var sendMessageText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)

        this.sendMessageText = findViewById(R.id.et_sendMessageText)

        val conversationId = intent.getIntExtra(CONVERSATION_ID, NO_CONVERSATION_ID)
        val conversation = ConversationRepository.get(conversationId) ?: return
        this.conversation = conversation
        if (conversation.messages == null) {
            conversation.messages = MessageRepository.get(contentResolver, conversationId)
        }

        val messages = conversation.messages ?: return
        this.messages = messages

        title = conversation.getDisplayName()

        // TODO: [Bug] Opening an unread conversation should mark it as read
        // TODO: [Style] Change message list action bar
        // TODO: [Style] Keyboard appears over the conversation thus hiding the bottom portion of the conversation
        val adapter = MessageListRecyclerAdapter(this, messages, conversation.contact?.photoUri)
        this.adapter = adapter
        rv_messageList.adapter = adapter
        rv_messageList.layoutManager = LinearLayoutManager(this)
        rv_messageList.scrollToPosition(messages.size - 1)

        // Send the typed message
        iv_sendMessageButton.setOnClickListener {
            val textToSend = this.sendMessageText.text.toString()
            if (textToSend.isBlank()) {
                return@setOnClickListener
            }
            this.sendMessageText.text.clear()

            // Send the message
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(this.conversation.senderPhone, null, textToSend, null, null)
            // Check if sent successfully
            val lastMessage = MessageRepository.checkIfMessageSent(contentResolver, this.conversation.threadId, textToSend)
            if (lastMessage != null) {
                // Update message list
                this.adapter.notifyDataSetChanged()
                // Update info in conversation to reflect latest changes
                // TODO: Bad practice, use something not mutable + we're concerning ourselves about logic in another activity
                this.conversation.latestMessageTimestamp = lastMessage.dateTimestamp
                this.conversation.latestMessageText = lastMessage.text
            }
        }
    }
}
