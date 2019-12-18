package com.shogek.spinoza.activities

import android.app.Activity
import android.app.PendingIntent
import android.content.*
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
import com.shogek.spinoza.cores.MessageListCore
import com.shogek.spinoza.models.Contact
import com.shogek.spinoza.services.MessageService
import kotlinx.android.synthetic.main.activity_message_list.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent

class MessageListActivity : AppCompatActivity() {

    private var sentPI: PendingIntent? = null
    private lateinit var messages: MutableList<Message>
    private lateinit var sendMessageText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)

        val core = MessageListCore(
            this,
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager,
            findViewById(R.id.cl_messageActionsRow)
        )

        val conversationId = intent.getIntExtra(CONVERSATION_ID, NO_CONVERSATION_ID)

        val contact: Contact
        val conversation: Conversation
        val adapter: MessageListRecyclerAdapter

        // Contact exists without a conversation (we're writing the first message)
        if (conversationId == NO_CONVERSATION_ID) {
            val contactId = intent.getStringExtra(CONTACT_ID)
            contact = ContactCache.get(contentResolver, contactId!!)
            this.messages = mutableListOf()
        } else {
            conversation = ConversationCache.get(conversationId)!!
            contact = ContactCache
                .getAll(contentResolver)
                .find { c -> c.strippedPhone == conversation.senderPhoneStripped }!!
            this.messages = MessageCache
                .getAll(contentResolver, conversationId)
                .toMutableList()
        }

        // TODO: [Bug] Opening an unread conversation should mark it as read
        adapter = MessageListRecyclerAdapter(this, core, messages, contact.photoUri)
        rv_messageList.adapter = adapter
        rv_messageList.layoutManager = LinearLayoutManager(this)
        rv_messageList.scrollToPosition(messages.size - 1)

        this.initScrollDownWhenKeyboardAppears(messages.size)
        this.initButtonReturn()
        this.initButtonSendMessage(contact.strippedPhone, adapter)
        this.initRowButtonActions(core)
        this.setToolbarInformation(contact.displayName, contact.photoUri)
        this.sendMessageText = findViewById(R.id.et_sendMessageText)
        // TODO: [Style] Add elevation to message box when not at bottom.
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
