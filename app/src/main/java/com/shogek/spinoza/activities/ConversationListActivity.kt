package com.shogek.spinoza.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.shogek.spinoza.adapters.ConversationListRecyclerAdapter
import com.shogek.spinoza.R
import com.shogek.spinoza.models.Contact
import com.shogek.spinoza.models.Conversation
import com.shogek.spinoza.repositories.ContactRepository
import com.shogek.spinoza.repositories.ConversationRepository
import kotlinx.android.synthetic.main.activity_conversation_list.*

class ConversationListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_list)

        // Change the activity's title
        title = "Chats"

        // TODO: Move it to an intent
        if (!this.getPermissions()) return

        val conversations = ConversationRepository.getConversations(contentResolver)
        val contacts = ContactRepository.getAllContacts(contentResolver)
        merge(conversations, contacts)

        rv_conversationList.layoutManager = LinearLayoutManager(this)
        rv_conversationList.adapter = ConversationListRecyclerAdapter(this, conversations)
    }

    /** Merge by comparing phone numbers. */
    private fun merge(conversations: Array<Conversation>, contacts: Array<Contact>) {
        val trim = "\\s".toRegex() // removes all whitespace
        conversations.forEach { conversation ->
            contacts.forEach { contact ->
                if (conversation.senderPhone == contact.number.replace(trim, "")) {
                    conversation.contact = contact
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // When we return to the conversation list, make sure we show any changes if there are any.
        rv_conversationList.adapter?.notifyDataSetChanged()
    }

    private fun getPermissions(): Boolean {
        // Show a modal asking for permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CONTACTS),
            Build.VERSION.SDK_INT
        )

        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.i("2", "Access to read SMS not granted.")
            return false
        }

        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.i("2", "Access to read contacts not granted.")
            return false
        }

        return true
    }
}
