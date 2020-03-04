package com.shogek.spinoza.ui.contacts.list

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.shogek.spinoza.Extra
import com.shogek.spinoza.db.contact.Contact
import com.shogek.spinoza.db.contact.ContactRepository
import com.shogek.spinoza.db.contact.ContactRoomDatabase

class ContactListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ContactRepository
    val contacts: LiveData<List<Contact>>

    init {
        val contactDao = ContactRoomDatabase.getDatabase(application, viewModelScope).contactDao()
        this.repository = ContactRepository(contactDao)
        this.contacts = contactDao.getAll()
    }

    /** Return chosen contact's ID to the calling activity. */
    fun returnPickedContact(
        context: AppCompatActivity,
        contactId: Long
    ) {
        val returnIntent = Intent()
        returnIntent.putExtra(Extra.ContactList.ConversationList.PickContact.CONTACT_ID, contactId)
        context.setResult(Activity.RESULT_OK, returnIntent)
        context.finish()
    }
}