package com.shogek.spinoza.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shogek.spinoza.models.Contact
import com.shogek.spinoza.repositories.ContactRepository
import com.shogek.spinoza.services.MessageService

class ContactListForwardViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private lateinit var messageBodyToForward: String
    var contacts: LiveData<List<Contact>> = MutableLiveData()

    init {
        this.contacts = ContactRepository(this.context).getAll()
    }

    fun setTextToForward(
        messageBody: String
    ) {
        this.messageBodyToForward = messageBody
    }

    fun forwardMessage(
        contactId: String
    ) {
        val contact = this.contacts.value!!.find { it.id == contactId }!!
        MessageService.send(contact.strippedPhone, this.messageBodyToForward)
        // TODO: [Bug] Not notifying about the message sent
    }
}