package com.shogek.spinoza.ui.contacts.list

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


class ContactListActivity : AppCompatActivity() {

    private lateinit var viewModel: ContactListViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)

        this.viewModel = ViewModelProvider(this).get(ContactListViewModel::class.java)
        val adapter = ContactListAdapter(this, ::onClickContact)

        this.viewModel.contacts.observe(this, Observer {
            val sortedContacts = it.sortedBy { c -> c.getDisplayTitle().toLowerCase() }
            adapter.setContacts(sortedContacts)
        })

        rv_contactList.adapter = adapter
        rv_contactList.layoutManager = LinearLayoutManager(this)

        this.enableReturnButton()
        this.enableContactFiltering(adapter)
        this.focusOnSearchBox()
    }

    private fun onClickContact(contact: Contact) {
        this.viewModel.onContactClick(this, contact)
    }

    private fun focusOnSearchBox() {
        et_filterContacts.isFocusableInTouchMode = true
        et_filterContacts.requestFocus()
    }

    private fun enableContactFiltering(adapter: ContactListAdapter) {
        et_filterContacts.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = adapter.filter(s.toString())
        })
    }

    private fun enableReturnButton() {
        contact_list_toolbar_return_iv.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
