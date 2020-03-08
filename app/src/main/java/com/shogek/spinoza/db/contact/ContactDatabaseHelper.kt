package com.shogek.spinoza.db.contact

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object ContactDatabaseHelper {

    private val LOG: String = ContactDatabaseHelper::class.java.simpleName

    fun importContactsFromPhone(
        context: Context,
        scope: CoroutineScope,
        contactDao: ContactDao
    ) {
        val contacts = this.retrieveAllPhoneContacts(context.contentResolver)
        if (contacts.isEmpty()) {
            return
        }

        scope.launch {
            contactDao.insertAll(contacts)
        }
    }

    /** Retrieve contacts saved in phone. */
    fun retrieveAllPhoneContacts(resolver: ContentResolver): List<Contact> {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
        )

        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            null
        )
        if (cursor == null) {
            Log.e(LOG, "Cursor is null")
            return listOf()
        }

        val contacts = mutableListOf<Contact>()

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID))
            val phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val photo = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI))

            val contact = Contact(id, name, phone, photo)
            contacts.add(contact)
        }

        cursor.close()
        return contacts
    }

    /** Update our contact records if their representations in the phone have changed. */
    fun synchronizeOurContactsWithPhoneContacts(
        context: Context,
        scope: CoroutineScope,
        contactDao: ContactDao
    ) {
        val contactData = contactDao.getAll()
        contactData.observeForever(object : Observer<List<Contact>> { override fun onChanged(ourContacts: List<Contact>?) {
            contactData.removeObserver(this)

            if (ourContacts == null || ourContacts.isEmpty()) {
                return
            }

            val phoneContacts = retrieveAllPhoneContacts(context.contentResolver)
            if (phoneContacts.isEmpty()) {
                return
            }

            val ourContactTable = ourContacts.associateBy({it.id}, {it})
            for (phoneContact in phoneContacts) {
                val ourContact = ourContactTable[phoneContact.id]
                if (ourContact == null) {
                    // New contact found in phone
                    scope.launch { contactDao.insert(phoneContact) }
                    continue
                }

                if (ourContact.name != phoneContact.name) { ourContact.name = phoneContact.name }
                if (ourContact.phone != phoneContact.phone) { ourContact.phone = phoneContact.phone }
                if (ourContact.photoUri != phoneContact.photoUri) { ourContact.photoUri = phoneContact.photoUri }
                scope.launch { contactDao.update(ourContact) }
            }
        }})
    }
}