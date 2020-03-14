package com.shogek.spinoza.db.contact

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import com.shogek.spinoza.db.ApplicationRoomDatabase
import com.shogek.spinoza.db.ModelHelpers

class ContactRepository(
    context: Context,
    scope: CoroutineScope
) {

    private val contactDao = ApplicationRoomDatabase.getDatabase(context, scope).contactDao()
    private val conversationDao = ApplicationRoomDatabase.getDatabase(context, scope).conversationDao()


    fun getAllObservable(): LiveData<List<Contact>> {
        return contactDao.getAllObservable()
    }

    suspend fun getAll(): List<Contact> {
        return contactDao.getAll()
    }

    suspend fun getAll(contactIds: List<Long>): List<Contact> {
        if (contactIds.isEmpty()) {
            return listOf()
        }
        return contactDao.getAll(contactIds)
    }

    suspend fun update(contact: Contact) {
        contactDao.update(contact)
    }

    suspend fun updateAll(contacts: List<Contact>) {
        if (contacts.isNotEmpty()) {
            contactDao.updateAll(contacts)
        }
    }

    suspend fun insert(contact: Contact): Long {
        return contactDao.insert(contact)
    }

    /** SIDE EFFECT - updates foreign key for associated conversations */
    suspend fun insertAll(contacts: List<Contact>): List<Long> {
        if (contacts.isEmpty()) {
            return listOf()
        }

        val conversations = conversationDao.getContactless()
        if (conversations.isEmpty()) {
            return contactDao.insertAll(contacts)
        }

        // Assign contacts to conversations if the phones match
        val insertedIds = contactDao.insertAll(contacts)
        val insertedContacts = contactDao.getAll(insertedIds)
        val matched = ModelHelpers.matchByPhone(conversations, insertedContacts, onlyMatches = true)
        if (matched.isNotEmpty()) {
            conversationDao.updateAll(matched)
        }

        return insertedIds
    }

    /** SIDE EFFECT - nulls foreign contact key for associated conversations */
    suspend fun deleteAll(contacts: List<Contact>) {
        if (contacts.isEmpty()) {
            return
        }

        // Remove contacts from conversations
        val contactIds = contacts.map { it.id }
        val conversations = conversationDao.getByContactIds(contactIds)
        if (conversations.isNotEmpty()) {
            conversations.forEach { it.contactId = null }
            conversationDao.updateAll(conversations)
        }
        contactDao.deleteAll(contacts)
    }
}