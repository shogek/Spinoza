package com.shogek.spinoza.cores

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.shogek.spinoza.Extra
import com.shogek.spinoza.activities.ContactListForwardActivity
import com.shogek.spinoza.models.Message
import com.shogek.spinoza.services.MessageService

class MessageListCore(
    private val context: Context,
    private val contactId: String?, // unknown number sent the message
    private val clipboard: ClipboardManager,
    private val rowMessageActions: ConstraintLayout
) {
    private var selectedMessage: Message? = null

    fun onLongClickMessage(message: Message) {
        this.selectedMessage = message
        this.rowMessageActions.visibility = View.VISIBLE
    }

    fun onClickMessage() = this.hideActions()

    fun onClickCopy() {
        val message = this.selectedMessage ?: return

        // TODO: [Task] Create a new toast background
        val clipData = ClipData.newPlainText("", message.text) // 'label' is for developers only
        this.clipboard.setPrimaryClip(clipData)
        Toast.makeText(this.context, "Copied", Toast.LENGTH_LONG).show()

        this.hideActions()
    }

    fun onClickForwardMessage() {
        val message = this.selectedMessage ?: return

        val intent = Intent(this.context, ContactListForwardActivity::class.java)
        intent.putExtra(Extra.MessageList.ContactListForward.ForwardMessage.MESSAGE, message.text)
        intent.putExtra(Extra.MessageList.ContactListForward.ForwardMessage.CONTACT_ID, this.contactId)
        this.context.startActivity(intent)

        this.hideActions()
    }

    fun onClickRemoveMessage() {
        val message = this.selectedMessage ?: return

        MessageService.delete(context.contentResolver, message)

        this.hideActions()
    }

    private fun hideActions() {
        this.selectedMessage = null
        this.rowMessageActions.visibility = View.GONE
    }
}