package com.shogek.spinoza.db.contact

import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log

object ContactDatabaseHelper {

    private val LOG: String = ContactDatabaseHelper::class.java.simpleName

    /** Retrieve contacts saved in phone. */
    fun retrieveAllPhoneContacts(resolver: ContentResolver): List<Contact> {
        val projection = arrayOf(
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
            val phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val photo = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI))

            val contact = Contact(name, phone, photo)
            contacts.add(contact)
        }

        cursor.close()
        return contacts
    }
}