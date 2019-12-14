package com.shogek.spinoza.caches

import android.content.ContentResolver
import com.shogek.spinoza.models.Contact
import com.shogek.spinoza.repositories.ContactRepository

object ContactCache {
    /** ContactsContract.CommonDataKinds.Phone.NUMBER (stripped phone) returns Contact */
    private val cache: HashMap<String, Contact> = HashMap()

    fun get(resolver: ContentResolver,
            contactId: String
    ) : Contact {
        if (this.cache.isNotEmpty()) {
            val target = this.cache.values.find { c -> c.id == contactId }
            if (target != null) {
                return target
            }
        }

        val contact = ContactRepository.get(resolver, contactId)
        this.cache[contact.strippedPhone] = contact
        return contact
    }

    fun getAll(resolver: ContentResolver,
               clearCache: Boolean = false
    ) : Array<Contact> {
        // Return cached result if possible
        if (this.cache.isNotEmpty() && !clearCache) {
            return this.cache.values.toTypedArray()
        }

        val contacts = ContactRepository.getAll(resolver)

        // Cache the result
        contacts.forEach { c -> this.cache[c.strippedPhone] = c }

        return contacts
    }
}