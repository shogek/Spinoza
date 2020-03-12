package com.shogek.spinoza.db.contact

import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log


object ContactDatabaseHelper {

    private val TAG: String = ContactDatabaseHelper::class.java.simpleName

    /** Return passed in contacts that were not found in the phone. */
    fun retrieveDeletedPhoneContacts(
        resolver: ContentResolver,
        contacts: List<Contact>
    ): List<Contact> {
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone._ID)
        val contactIds = contacts.map { it.androidId }

        // Get all contacts by ID. If an ID was not returned - it was removed
        val selection = ContactsContract.CommonDataKinds.Phone._ID + " IN " + "(" + contactIds.joinToString(",") + ")"

        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            null,
            null
        )

        if (cursor == null) {
            Log.e(TAG, "Cursor is null")
            return listOf()
        }

        val foundIds = mutableListOf<Long>()

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID))
            foundIds.add(id)
        }
        cursor.close()

        return contacts.filter { !foundIds.contains(it.androidId) }
    }

    /** Retrieves all phone's contacts that were created or updated after the specified date. */
    fun retrieveUpsertedPhoneContacts(
        resolver: ContentResolver,
        dateAfterTimestamp: Long
    ): List<Contact> {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
        )

        val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP} > $dateAfterTimestamp"

        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            null,
            null
        )
            ?: return listOf()

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
            Log.e(TAG, "Cursor is null")
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
}