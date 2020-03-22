package com.shogek.spinoza.ui.contacts.list

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shogek.spinoza.Extra
import com.shogek.spinoza.db.contact.Contact
import com.shogek.spinoza.db.contact.ContactRepository


class ContactListViewModel(application: Application) : AndroidViewModel(application) {

    private val contactRepository = ContactRepository(application.baseContext, viewModelScope)
    val contacts = contactRepository.getAllObservable()


    fun onContactClick(
        context: AppCompatActivity,
        contact: Contact
    ) {
        this.returnPickedContact(context, contact)
    }

    /** Return chosen contact's ID to the calling activity. */
    private fun returnPickedContact(
        context: AppCompatActivity,
        contact: Contact
    ) {
        val returnIntent = Intent()
        returnIntent.putExtra(Extra.ContactList.ConversationList.PickContact.CONTACT_ID, contact.id)
        context.setResult(Activity.RESULT_OK, returnIntent)
        context.finish()
    }
}