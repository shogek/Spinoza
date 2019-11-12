package com.shogek.spinoza.repositories

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.shogek.spinoza.models.Contact

object ContactRepository {
    fun getAllContacts(resolver: ContentResolver): Array<Contact> {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val selection = null
        val selectionArgs = null
        val sortOrder = null

        val cursor = resolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
            ?: return emptyArray()

        var log = "\n "
        var contact: Contact
        var contacts = arrayOf<Contact>()

        while (cursor.moveToNext()) {
            log += "\n"
            contact = Contact()
            for (column in cursor.columnNames) {
                val index = cursor.getColumnIndex(column)
                val value = cursor.getString(index)

                mapContactField(contact, column, value ?: "")

                log += "\n"
                log += "INDEX: $index \t"
                log += "NAME:  $column  \t"
                log += "VALUE: $value"
            }
            contacts += contact
        }
        Log.w("2", log)
        cursor.close()
        return contacts
    }

    private fun mapContactField(contact: Contact, column: String, value: String) {
        when (column) {
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID ->            contact.id      = value
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME ->          contact.name    = value
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI ->   contact.photo   = if (value != "") Uri.parse(value) else null
            ContactsContract.CommonDataKinds.Phone.NUMBER ->                contact.phone   = value
        }
    }
}