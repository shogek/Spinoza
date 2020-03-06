package com.shogek.spinoza.db.contact

import androidx.lifecycle.LiveData

class ContactRepository(private val contactDao: ContactDao) {

    fun getAll(): LiveData<List<Contact>> {
        return contactDao.getAll()
    }

    suspend fun update(contact: Contact) {
        contactDao.update(contact)
    }

    suspend fun insert(contact: Contact): Long {
        return contactDao.insert(contact)
    }

    suspend fun insertAll(contacts: List<Contact>): List<Long> {
        return contactDao.insertAll(contacts)
    }

    suspend fun deleteAll() {
        contactDao.deleteAll()
    }

    suspend fun delete(contact: Contact) {
        contactDao.delete(contact)
    }
}