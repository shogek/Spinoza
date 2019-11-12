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
import com.shogek.spinoza.models.Conversation
import com.shogek.spinoza.repositories.ContactRepository
import com.shogek.spinoza.repositories.SmsRepository
import kotlinx.android.synthetic.main.activity_conversation_list.*
import java.time.LocalDateTime

class ConversationListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_list)

        // Change the activity's title
        title = "Chats"

        /*
            RecyclerView
            - Presents a list of data

            LayoutManager
            - Handles positioning of items

            Adapter
            - Creates item views
            - Associates data with item views
         */
        // TODO: Move it to an intent
        if (!this.getPermissions()) return

        // Display the items in a vertical list.
        // Alternatives are: custom, GridLayout and StaggeredGridLayout.
        my_recycler_view.layoutManager = LinearLayoutManager(this)

        val conversations = SmsRepository.getConversations(contentResolver)
        my_recycler_view.adapter = ConversationListRecyclerAdapter(this, conversations)
    }

    override fun onResume() {
        super.onResume()
        // When we return to the conversation list, make sure we show any changes if there are any.
        my_recycler_view.adapter?.notifyDataSetChanged()
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
