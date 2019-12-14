package com.shogek.spinoza.repositories

import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.core.database.getStringOrNull
import com.shogek.spinoza.models.Contact
import com.shogek.spinoza.utils.PhoneUtils

object ContactRepository {

    fun get(resolver: ContentResolver,
            contactId: String
    ) : Contact {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
        )

        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            "${ContactsContract.CommonDataKinds.Phone._ID} = ?",
            arrayOf(contactId),
            null
        )

        cursor!!.moveToFirst()
        val id      = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID))
        val number  = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
        /*
            TODO: [Refactor] Figure out when it is set and use it instead of comparing 'Phone.NUMBER'
            To my understanding, the 'Phone.NUMBER' is separated to distinct parts,         ex.: "+372 512 4788"
            while 'Phone.NORMALIZED_NUMBER' matches what we get from 'Conversations',       ex.: "+3725124788"
         */
        val e164    = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER))
        val name    = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
        val photo   = cursor.getStringOrNull(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI))
        val strippedPhone = PhoneUtils.getStrippedPhone(number)

        val contact = Contact(id, name, strippedPhone, number, e164, photo)

        cursor.close()
        return contact
    }

    fun getAll(resolver: ContentResolver): Array<Contact> {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
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
            ?: return arrayOf()

        val contacts = mutableListOf<Contact>()

        while (cursor.moveToNext()) {
            val id      = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID))
            val number  = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            /*
                TODO: [Refactor] Figure out when it is set and use it instead of comparing 'Phone.NUMBER'
                To my understanding, the 'Phone.NUMBER' is separated to distinct parts,         ex.: "+372 512 4788"
                while 'Phone.NORMALIZED_NUMBER' matches what we get from 'Conversations',       ex.: "+3725124788"
             */
            val e164    = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER))
            val name    = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val photo   = cursor.getStringOrNull(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI))
            val strippedPhone = PhoneUtils.getStrippedPhone(number)

            contacts.add(Contact(id, name, strippedPhone, number, e164, photo))
        }

        cursor.close()
        return contacts.toTypedArray()
    }
}