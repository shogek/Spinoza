package com.shogek.spinoza

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.shogek.spinoza.repositories.SmsRepository
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
            RecyclerView
            - Presents a list of data

            LayoutManager
            - Handles positioning of items

            Adapter
            - Creates item views
            - Associates data with item views
         */

        // Display the items in a vertical list.
        // Alternatives are: custom, GridLayout and StaggeredGridLayout.
        my_recycler_view.layoutManager = LinearLayoutManager(this)

        val date1 = LocalDateTime.of(2019, 10, 31, 12, 54)
        val date2 = LocalDateTime.of(2019, 10, 30, 10, 12)
        val date3 = LocalDateTime.of(2019, 5, 25, 18, 5)
        val date4 = LocalDateTime.of(2018, 7, 4, 22, 39)
        val date5 = LocalDateTime.of(2018, 6, 25, 22, 39)
        val date6 = LocalDateTime.of(2018, 4, 13, 22, 39)
        val date7 = LocalDateTime.of(2018, 2, 13, 22, 39)
        val date8 = LocalDateTime.of(2018, 1, 19, 22, 39)
        val date9 = LocalDateTime.of(2017, 11, 7, 22, 39)

        val dummyConversations = arrayListOf(
            DummyConversation("John Doe",        "I'll meet you there.",        false, false,  date1, null),
            DummyConversation("Jane Doe",        "I guess? Call him later.",    false, false,  date2, null),
            DummyConversation("Vicky Johnson",   "2 pizzas ar 5 burgers.",      true,  true,   date3, null),
            DummyConversation("Aaron Jackson",   "Does it look like I care?",   false, false,  date4, null),
            DummyConversation("Steve Lind",      "Thursday is perfect!",        false, true,   date5, null),
            DummyConversation("Clark Stevenson", "Old Town Road, I think.",     true,  true,   date6, null),
            DummyConversation("James Iverson",   "Three-zero-zero-one-one",     false, true,   date7, null),
            DummyConversation("Ivan Karishnov",  "He will bother you no more.", false, true,   date8, null),
            DummyConversation("Monica Lewinsky", "I hate politics",             false, true,   date9, null)
        )
        my_recycler_view.adapter = ConversationsRecyclerAdapter(this, dummyConversations)

        if (!this.getPermissions()) return
        val messages = SmsRepository.getAllSms(this.contentResolver)
        val contacts = SmsRepository.getAllContacts(this.contentResolver)
    }

    override fun onResume() {
        super.onResume()
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
