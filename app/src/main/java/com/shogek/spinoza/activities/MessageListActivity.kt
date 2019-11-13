package com.shogek.spinoza.activities

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.shogek.spinoza.CONVERSATION_ID
import com.shogek.spinoza.NO_CONVERSATION_ID
import com.shogek.spinoza.R
import com.shogek.spinoza.adapters.MessageListRecyclerAdapter
import com.shogek.spinoza.repositories.ConversationRepository
import com.shogek.spinoza.repositories.MessageRepository
import kotlinx.android.synthetic.main.activity_message_list.*

class MessageListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)

        val conversationId = intent.getIntExtra(CONVERSATION_ID, NO_CONVERSATION_ID)
        val conversation = ConversationRepository.get(conversationId) ?: return
        if (conversation.messages == null) {
            conversation.messages = MessageRepository.getMessages(contentResolver ,conversationId)
        }

        // Change activity title to display sender's name
        title = conversation.getDisplayName()
        // Change activity title's background color
        val color = ContextCompat.getColor(this, R.color.colorWhite)
        actionBar?.setBackgroundDrawable(ColorDrawable(color))

        val messages = conversation.messages ?: return
        rv_messageList.layoutManager = LinearLayoutManager(this)
        rv_messageList.adapter = MessageListRecyclerAdapter(this, messages, conversation.contact?.photoUri)
        rv_messageList.scrollToPosition(messages.size - 1)
    }
}
