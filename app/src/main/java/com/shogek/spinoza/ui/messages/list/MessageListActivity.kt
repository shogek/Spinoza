package com.shogek.spinoza.ui.messages.list

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.net.Uri
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.shogek.spinoza.*
import com.shogek.spinoza.db.message.Message
import com.shogek.spinoza.ui.state.CommonState
import kotlinx.android.synthetic.main.activity_message_list.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent

class MessageListActivity : AppCompatActivity() {

    private lateinit var viewModel: MessageListViewModel
    private lateinit var messageActionButtons: ConstraintLayout


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

        val adapter = MessageListAdapter(this, ::onMessageClick, ::onMessageLongClick)

        this.viewModel.conversation.observe(this, Observer { conversation ->
            CommonState.setCurrentOpenConversationId(conversation.id)

            this.initButtonSendMessage()
            // TODO: [Style] Add elevation to message box when not at bottom.
            val title = conversation.contact?.getDisplayTitle() ?: conversation.phone
            this.setToolbarInformation(title, conversation.contact?.photoUri)

            this.viewModel.markConversationAsRead(conversation)

            if (conversation.contact != null) {
                val contact = conversation.contact!!
                adapter.setContactImage(contact.photoUri)
            }

            val messages = conversation.messages!!
            adapter.setMessages(messages)
            rv_messageList.scrollToPosition(messages.size - 1)
            this.initScrollDownWhenKeyboardAppears(messages.size)
        })

        rv_messageList.adapter = adapter
        rv_messageList.layoutManager = LinearLayoutManager(this)

        this.initButtonReturn()
    }

    override fun onResume() {
        super.onResume()
        this.viewModel.onActivityResume()
    }

    override fun onPause() {
        super.onPause()
        this.viewModel.onActivityPause()
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

    private fun initScrollDownWhenKeyboardAppears(messageCount: Int) {
        KeyboardVisibilityEvent.setEventListener(this) { isVisible ->
            if (isVisible) {
                rv_messageList.scrollToPosition(messageCount - 1)
            }
        }
    }

    private fun initButtonSendMessage() {
        iv_sendMessageButton.setOnClickListener {
            val message = et_sendMessageText.text.toString()
            if (message.isBlank()) {
                return@setOnClickListener
            }

            et_sendMessageText.text.clear()
            viewModel.sendMessage(message)
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
}
