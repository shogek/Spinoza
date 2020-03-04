package com.shogek.spinoza.db.contact

import androidx.lifecycle.LiveData

class ContactRepository(private val contactDao: ContactDao) {

    fun getAll(): LiveData<List<Contact>> {
        return contactDao.getAll()
    }

    suspend fun deleteAll() {
        contactDao.deleteAll()
    }

    suspend fun insert(contact: Contact): Long {
        return contactDao.insert(contact)
    }
}