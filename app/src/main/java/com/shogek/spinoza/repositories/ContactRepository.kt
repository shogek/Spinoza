package com.shogek.spinoza.repositories

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import androidx.core.database.getStringOrNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shogek.spinoza.models.Contact
import com.shogek.spinoza.utils.PhoneUtils

class ContactRepository(
    private val context: Context
) {

    private companion object {
        private var contacts: MutableLiveData<List<Contact>> = MutableLiveData(listOf())
    }

    fun get(
        contactId: String
    ): Contact? {
        if (contacts.value.isNullOrEmpty()) {
            this.initData()
        }
        return contacts.value!!.find { it.id == contactId }
    }

    fun getAll(): LiveData<List<Contact>> {
        if (contacts.value.isNullOrEmpty()) {
            this.initData()
        }
        return contacts
    }

    private fun initData() {
        contacts.value = this.retrieveAllContacts(context.contentResolver)
    }

    private fun retrieveAllContacts(resolver: ContentResolver): List<Contact> {
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
            ?: return listOf()

        val contacts = mutableListOf<Contact>()

        while (cursor.moveToNext()) {
            val id      = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID))
            val number  = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            /*
                TODO: [Refactor] Figure out when it is set and use it instead of comparing 'Phone.NUMBER'
                To my understanding, the 'Phone.NUMBER' is separated to distinct parts,         ex.: "+372 512 4788"
                while 'Phone.NORMALIZED_NUMBER' matches what we get from 'Conversations',       ex.: "+3725124788"
             */
            val e164    = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER))
            val name    = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val photo   = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI))
            val strippedPhone = PhoneUtils.getStrippedPhone(number)

            contacts.add(Contact(id.toString(), name, strippedPhone, number, e164, photo))
        }

        cursor.close()
        return contacts
    }
}