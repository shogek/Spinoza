package com.shogek.spinoza.ui.contacts.forward

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.shogek.spinoza.Extra
import com.shogek.spinoza.R
import kotlinx.android.synthetic.main.activity_contact_list.*

class ContactListForwardActivity : AppCompatActivity() {

//    private lateinit var viewModel: ContactListForwardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list_forward)

        // TODO: [Refactor]
//        this.viewModel = ViewModelProviders.of(this).get(ContactListForwardViewModel::class.java)

//        val textToForward = intent.getStringExtra(Extra.MessageList.ContactListForward.ForwardMessage.MESSAGE)!!
//        this.viewModel.setTextToForward(textToForward)

//        val adapter =
//            ContactListForwardAdapter(
//                this,
//                this.viewModel
//            )
//        this.viewModel.contacts.observe(this, Observer { contacts ->
//            val sorted = contacts.sortedBy { it.displayName }
//            adapter.setContacts(sorted)
//        })

//        rv_contactList.adapter = adapter
//        rv_contactList.layoutManager = LinearLayoutManager(this)
//
//        this.initButtonReturn()
//        this.initContactFilter(adapter)
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
