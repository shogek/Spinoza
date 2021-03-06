package com.shogek.spinoza.ui.conversation.list

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.shogek.spinoza.Extra
import com.shogek.spinoza.R
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.ui.contacts.list.ContactListActivity
import com.shogek.spinoza.ui.messages.list.MessageListActivity
import com.shogek.spinoza.utils.UnitUtils
import kotlinx.android.synthetic.main.activity_conversation_list.*
import java.security.InvalidParameterException


class ConversationListActivity : AppCompatActivity() {

    private lateinit var viewModel: ConversationListViewModel
    private lateinit var adapter: ConversationListAdapter

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_list)
        // TODO: [Refactor] Move permissions asking to a separate activity
//        this.ensurePermissionsGranted(PERMISSIONS_REQUIRED)
//        return
        this.viewModel = ViewModelProvider(this).get(ConversationListViewModel::class.java)
        this.ensurePermissionsGranted(PERMISSIONS_REQUIRED)
        this.initApp()
    }

    private fun ensurePermissionsGranted(permissions: Array<String>) {
        if (!hasPermissions(this, permissions))
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_ALL)
//        else
//            this.initApp()
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
////        val permissionsMissing = grantResults.any { gr -> gr == PackageManager.PERMISSION_DENIED }
////        if (permissionsMissing)
////            this.requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_ALL)
////        else
//            this.initApp()
    }

    private fun initApp() {
        this.adapter = ConversationListAdapter(this, ::onClickConversation, ::onLongClickConversation)

        this.viewModel.conversations.observe(this, Observer { conversations ->
            val sorted = conversations.sortedByDescending { it.snippetTimestamp }
            adapter.setConversations(sorted)
        })

        rv_conversationList.layoutManager = LinearLayoutManager(this)
        rv_conversationList.adapter = adapter
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

    private fun initButtonNewMessage() {
        createNewMessage.setOnClickListener {
            val intent = Intent(this, ContactListActivity::class.java)
            startActivityForResult(intent, REQUEST_PICK_CONTACT)
        }
    }

    private fun onClickConversation(conversation: Conversation) {
        this.viewModel.onConversationClick(conversation)
        this.adapter.clearSearchBox()
    }

    private fun onLongClickConversation(conversation: Conversation) {
        this.openConversationActionsModal(conversation)
    }

    private fun openConversationActionsModal(conversation: Conversation) {
        MaterialDialog(this).show {
            title(R.string.conversation_list_item_options_title)

            val archive = context.getString(R.string.conversation_list_item_option_archive)
            val delete  = context.getString(R.string.conversation_list_item_option_delete)
            val mute    = context.getString(R.string.conversation_list_item_option_mute)
            val unread  = context.getString(R.string.conversation_list_item_option_unread)
            val ignore  = context.getString(R.string.conversation_list_item_option_ignore)
            val block   = context.getString(R.string.conversation_list_item_option_block)

            listItems(items = listOf(archive, delete, mute, unread, ignore, block)) { _, _, text ->
                when (text) {
                    archive -> viewModel.archiveConversation(conversation)
                    delete  -> viewModel.deleteConversation(conversation)
                    mute    -> viewModel.muteConversation(conversation)
                    unread  -> viewModel.markAsUnreadConversation(conversation)
                    ignore  -> viewModel.ignoreConversation(conversation)
                    block   -> viewModel.blockConversation(conversation)
                    else -> throw InvalidParameterException("Unknown conversation action!")
                }
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_PICK_CONTACT || resultCode != Activity.RESULT_OK) {
            return
        }

        // User picked a contact - open the corresponding conversation
        val intent = Intent(this, MessageListActivity::class.java)
        val contactId = data!!.extras!![Extra.ContactList.ConversationList.PickContact.CONTACT_ID] as Long

        intent.putExtra(Extra.GOAL, Extra.ConversationList.MessageList.NewMessage.GOAL)
        intent.putExtra(Extra.ConversationList.MessageList.NewMessage.CONTACT_ID, contactId)
        startActivity(intent)
    }
}
