package com.shogek.spinoza.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import com.shogek.spinoza.Extra
import com.shogek.spinoza.R
import com.shogek.spinoza.adapters.ContactListForwardRecyclerAdapter
import com.shogek.spinoza.caches.ContactCache
import com.shogek.spinoza.cores.ContactListForwardCore
import kotlinx.android.synthetic.main.activity_contact_list.*

class ContactListForwardActivity : AppCompatActivity() {

    lateinit var core: ContactListForwardCore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list_forward)
        // TODO: [Bug] You can forward a message to the same person who you copied it from
        val messageToForward = intent.getStringExtra(Extra.MessageList.ContactListForward.MESSAGE)!!

        this.core = ContactListForwardCore(contentResolver, messageToForward)

        val sortedContacts = ContactCache
            .getAll(contentResolver, true)
            .sortedBy { c -> c.displayName }
            .toTypedArray()
        val adapter = ContactListForwardRecyclerAdapter(this, this.core, sortedContacts)
        rv_contactList.adapter = adapter
        rv_contactList.layoutManager = LinearLayoutManager(this)
        rv_contactList.adapter

        this.initButtonReturn()
        this.initContactFilter(adapter)
    }

    private fun initContactFilter(recyclerAdapter: ContactListForwardRecyclerAdapter) {
        et_filterContacts.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = recyclerAdapter.filter(s.toString())
        })
    }

    private fun initButtonReturn() {
        contact_list_toolbar_return_iv.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
