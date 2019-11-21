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
import com.shogek.spinoza.repositories.ContactRepository
import com.shogek.spinoza.repositories.ConversationRepository
import com.shogek.spinoza.helpers.ConversationHelper
import kotlinx.android.synthetic.main.activity_conversation_list.*

class ConversationListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_list)

        // Show custom action bar
        setSupportActionBar(findViewById(R.id.conversation_list_toolbar))
        supportActionBar?.title = "" // otherwise it shows the app's title

        // TODO: [Task] Create separate Views to ask for permissions
        if (!this.getPermissions()) return

        val conversations = ConversationRepository.getAll(contentResolver)
        conversations.sortByDescending { c -> c.latestMessageTimestamp }
        val contacts = ContactRepository.getAll(contentResolver)
        ConversationHelper.matchContactsWithConversations(conversations, contacts.toMutableList())

        rv_conversationList.layoutManager = LinearLayoutManager(this)
        rv_conversationList.adapter = ConversationListRecyclerAdapter(this, conversations)
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
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_CONTACTS
            ),
            Build.VERSION.SDK_INT
        )

        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.i("2", "Access to read SMS not granted.")
            return false
        }

        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.i("2", "Access to send SMS not granted.")
            return false
        }

        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.i("2", "Access to receive SMS not granted.")
            return false
        }

        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.i("2", "Access to read contacts not granted.")
            return false
        }

        return true
    }
}
