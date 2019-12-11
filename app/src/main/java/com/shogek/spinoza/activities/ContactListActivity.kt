package com.shogek.spinoza.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.shogek.spinoza.R
import com.shogek.spinoza.adapters.ContactListRecyclerAdapter
import com.shogek.spinoza.repositories.ContactRepository
import kotlinx.android.synthetic.main.activity_contact_list.*

class ContactListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)

        val contacts = ContactRepository.getAll(contentResolver, true)
        val adapter = ContactListRecyclerAdapter(this, contacts.toMutableList())
        rv_contactList.adapter = adapter
        rv_contactList.layoutManager = LinearLayoutManager(this)

        // Return to previous activity on arrow click
        contact_list_toolbar_return_iv.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
