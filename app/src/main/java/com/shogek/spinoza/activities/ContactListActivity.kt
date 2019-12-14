package com.shogek.spinoza.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import com.shogek.spinoza.R
import com.shogek.spinoza.adapters.ContactListRecyclerAdapter
import com.shogek.spinoza.caches.ContactCache
import kotlinx.android.synthetic.main.activity_contact_list.*

class ContactListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)

        val sortedContacts = ContactCache
            .getAll(contentResolver, true)
            .sortedBy { c -> c.displayName }
            .toTypedArray()
        val adapter = ContactListRecyclerAdapter(this, sortedContacts)
        rv_contactList.adapter = adapter
        rv_contactList.layoutManager = LinearLayoutManager(this)
        rv_contactList.adapter

        this.enableReturnButton()
        this.enableContactFiltering(adapter)
        this.focusOnSearchBox()
    }

    private fun focusOnSearchBox() {
        et_filterContacts.isFocusableInTouchMode = true
        et_filterContacts.requestFocus()
    }

    private fun enableContactFiltering(adapter: ContactListRecyclerAdapter) {
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
