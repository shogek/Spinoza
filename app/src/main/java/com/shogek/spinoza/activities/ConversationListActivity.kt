package com.shogek.spinoza.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shogek.spinoza.Extra
import com.shogek.spinoza.R
import com.shogek.spinoza.adapters.ConversationListRecyclerAdapter
import com.shogek.spinoza.caches.ContactCache
import com.shogek.spinoza.caches.ConversationCache
import com.shogek.spinoza.events.ConversationActionEvent
import com.shogek.spinoza.events.messages.MessageReceivedEvent
import com.shogek.spinoza.helpers.ConversationHelper
import com.shogek.spinoza.models.Conversation
import com.shogek.spinoza.utils.UnitUtils
import kotlinx.android.synthetic.main.activity_conversation_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class ConversationListActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_PICK_CONTACT = 0
        const val DIRECTION_UP = -1
        const val TOOLBAR_ELEVATION_DIP: Float = 4f

        const val PERMISSIONS_ALL = 1
        val PERMISSIONS_REQUIRED = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE
        )
    }

    private lateinit var conversations: Array<Conversation>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_list)
        this.ensurePermissionsGranted(PERMISSIONS_REQUIRED)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun ensurePermissionsGranted(permissions: Array<String>) {
        if (!hasPermissions(this, permissions))
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_ALL)
        else
            this.initApp()
    }

    private fun hasPermissions(
        context: Context,
        permissions: Array<String>
    ) : Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val permissionsMissing = grantResults.any { gr -> gr == PackageManager.PERMISSION_DENIED }
        if (permissionsMissing)
            this.requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_ALL)
        else
            this.initApp()
    }

    private fun initApp() {
        this.conversations = this.getConversations()
        rv_conversationList.layoutManager = LinearLayoutManager(this)
        rv_conversationList.adapter = ConversationListRecyclerAdapter(this, this.conversations)
        rv_conversationList.setHasFixedSize(true)

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

        val contactId = data!!.extras!![Extra.ContactList.ConversationList.PickContact.CONTACT_ID] as String
        val conversationId = ConversationCache
            .getAll(contentResolver)
            .find { c -> c.contact?.id == contactId }
            ?.threadId

        intent.putExtra(Extra.GOAL, Extra.ConversationList.MessageList.NewMessage.GOAL)
        intent.putExtra(Extra.ConversationList.MessageList.NewMessage.CONTACT_ID, contactId)

        if (conversationId != null) {
            // Check if exchanged messages with the contact before
            intent.putExtra(Extra.ConversationList.MessageList.NewMessage.CONVERSATION_ID, conversationId)
        }

        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // When we return to the conversation list, make sure we show any changes if there are any.
        rv_conversationList.adapter?.notifyDataSetChanged()
    }

    @Subscribe
    fun onMessageReceivedEvent(event: MessageReceivedEvent) {
        // TODO: [Bug] If when in contact1 message list an SMS for contact2 arrives - it will not be seen in the conversation list
        // (because no one is listening to the event)
        // TODO: [Refactor] Have a single source of truth that's always listening
        // TODO: [Bug] When a message is received, the conversation is not sorted to be on top again
        val conversation = this.conversations.find { c -> c.threadId == event.conversationId }!!
        conversation.latestMessageIsOurs = false
        conversation.wasRead = false
        conversation.latestMessageText = event.message.text
        conversation.latestMessageTimestamp = event.message.dateTimestamp
        rv_conversationList.adapter?.notifyDataSetChanged()
    }

    @Subscribe
    fun onConversationActionEvent(event: ConversationActionEvent) {
        Toast.makeText(this, event.action.toString(), Toast.LENGTH_SHORT).show()
    }
}
