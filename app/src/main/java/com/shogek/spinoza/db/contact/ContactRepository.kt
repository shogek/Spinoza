package com.shogek.spinoza.db.contact

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import com.shogek.spinoza.db.ApplicationRoomDatabase

class ContactRepository(
    context: Context,
    scope: CoroutineScope
) {

    private val contactDao = ApplicationRoomDatabase.getDatabase(context, scope).contactDao()


    suspend fun getAll(): List<Contact> {
        return contactDao.getAll()
    }

    fun getAllObservable(): LiveData<List<Contact>> {
        return contactDao.getAllObservable()
    }
}