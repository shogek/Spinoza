package com.shogek.spinoza.cores

import android.content.ContentResolver
import com.shogek.spinoza.caches.ContactCache
import com.shogek.spinoza.services.MessageService

class ContactListForwardCore(
    private val resolver: ContentResolver,
    private val messageToForward: String
) {

    fun onClickForwardMessage(contactId: String) {
        val contact = ContactCache.get(this.resolver, contactId)
        MessageService.send(contact.strippedPhone, messageToForward)
    }
}