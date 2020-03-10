package com.shogek.spinoza.db.contact

import android.content.Context
import androidx.lifecycle.LiveData
import com.shogek.spinoza.db.ApplicationRoomDatabase
import kotlinx.coroutines.CoroutineScope

class ContactRepository(
    context: Context,
    scope: CoroutineScope
) {

    private val dao = ApplicationRoomDatabase.getDatabase(context, scope).contactDao()

    fun getAllObservable(): LiveData<List<Contact>> {
        return dao.getAllObservable()
    }

    suspend fun getAll(): List<Contact> {
        return dao.getAll()
    }

    suspend fun update(contact: Contact) {
        dao.update(contact)
    }

    suspend fun updateAll(contacts: List<Contact>) {
        dao.updateAll(contacts)
    }

    suspend fun insert(contact: Contact): Long {
        return dao.insert(contact)
    }

    suspend fun insertAll(contacts: List<Contact>): List<Long> {
        return dao.insertAll(contacts)
    }

    suspend fun deleteAll(contacts: List<Contact>) {
        dao.deleteAll(contacts)
    }

    suspend fun delete(contact: Contact) {
        dao.delete(contact)
    }
}