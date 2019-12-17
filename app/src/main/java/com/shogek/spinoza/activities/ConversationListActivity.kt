package com.shogek.spinoza.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shogek.spinoza.*
import com.shogek.spinoza.adapters.ConversationListRecyclerAdapter
import com.shogek.spinoza.caches.ContactCache
import com.shogek.spinoza.helpers.ConversationHelper
import com.shogek.spinoza.models.Conversation
import com.shogek.spinoza.caches.ConversationCache
import com.shogek.spinoza.utils.UnitUtils
import kotlinx.android.synthetic.main.activity_conversation_list.*

class ConversationListActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_PICK_CONTACT = 0
        const val DIRECTION_UP = -1
        const val TOOLBAR_ELEVATION_DIP: Float = 4f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_list)

        // TODO: [Task] Create separate Views to ask for permissions
        if (!this.getPermissions()) return

        rv_conversationList.layoutManager = LinearLayoutManager(this)
        rv_conversationList.adapter = ConversationListRecyclerAdapter(this, this.getConversations())

        this.initToolbarElevation()
        this.initButtonNewMessage()
    }

    private fun initToolbarElevation() {
        rv_conversationList.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                val canScrollUp = recyclerView.canScrollVertically(DIRECTION_UP)
                conversation_list_toolbar.elevation =
                    if (canScrollUp)
                        UnitUtils.dpsAsPixels(TOOLBAR_ELEVATION_DIP, applicationContext.resources)
                    else
                        0f
            }
        })
    }

    private fun getConversations() : Array<Conversation> {
        val contacts = ContactCache.getAll(contentResolver)
        val conversations = ConversationCache
            .getAll(contentResolver, false)
            .sortedByDescending { c -> c.latestMessageTimestamp }
            .toTypedArray()

        ConversationHelper.matchContactsWithConversations(conversations, contacts)
        return conversations
    }

    private fun initButtonNewMessage() {
        createNewMessage.setOnClickListener {
            val intent = Intent(this, ContactListActivity::class.java)
            startActivityForResult(intent, REQUEST_PICK_CONTACT)
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != REQUEST_PICK_CONTACT) return

        // Unsuccessful request - the user backed out of the operation
        if (resultCode != Activity.RESULT_OK) return

        // User picked a contact - open the corresponding conversation
        val intent = Intent(this, MessageListActivity::class.java)

        val contactId = data!!.extras!![PARAM_PICK_CONTACT] as String
        val conversationId = ConversationCache
            .getAll(contentResolver)
            .find { c -> c.contact?.id == contactId }
            ?.threadId

        if (conversationId == null) {
            intent.putExtra(CONTACT_ID, contactId)
        } else {
            intent.putExtra(CONVERSATION_ID, conversationId)
        }

        startActivity(intent)
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
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE
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

        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.i("2", "Access to read phone state not granted.")
            return false
        }

        return true
    }
}
