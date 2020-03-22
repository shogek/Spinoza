package com.shogek.spinoza.ui.contacts.forward

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.shogek.spinoza.R
import com.shogek.spinoza.db.contact.Contact
import kotlinx.android.synthetic.main.activity_contact_list.*

class ContactListForwardActivity : AppCompatActivity() {

    private lateinit var viewModel: ContactListForwardViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list_forward)

        this.viewModel = ViewModelProvider(this)
            .get(ContactListForwardViewModel::class.java)
            .init(intent)
        val adapter = ContactListForwardAdapter(this, ::onClickForward)

        this.viewModel.contacts.observe(this, Observer { contacts ->
            val sorted = contacts.sortedBy { it.getDisplayTitle().toLowerCase() }
            adapter.setContacts(sorted)
        })

        rv_contactList.adapter = adapter
        rv_contactList.layoutManager = LinearLayoutManager(this)

        this.initButtonReturn()
        this.initContactFilter(adapter)
    }

    private fun onClickForward(contact: Contact) {
        this.viewModel.forwardMessage(contact)
    }

    private fun initContactFilter(recyclerAdapter: ContactListForwardAdapter) {
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
