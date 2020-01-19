package com.shogek.spinoza.cores

import android.content.Context
import com.shogek.spinoza.repositories.ContactRepository
import com.shogek.spinoza.services.MessageService

class ContactListForwardCore(
    private var context: Context,
    private val messageToForward: String
) {

    // TODO: [Refactor] Remove this class and delete the whole folder
    fun onClickForwardMessage(contactId: String) {
        val contact = ContactRepository(context)
            .getAll().value!!
            .find { it.id == contactId }!!
        MessageService.send(contact.strippedPhone, messageToForward)
    }
}