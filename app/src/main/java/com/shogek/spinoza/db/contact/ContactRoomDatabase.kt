package com.shogek.spinoza.db.contact

import android.content.Context
import androidx.lifecycle.Observer
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [Contact::class],
    version = 3,
    exportSchema = false
)
abstract class ContactRoomDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao

    private class ContactDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        private var cameFromOnCreate = false

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            this.cameFromOnCreate = true
            INSTANCE?.let { database -> scope.launch {
                val contacts = ContactDatabaseHelper.retrieveAllPhoneContacts(context.contentResolver)
                database.contactDao().insertAll(contacts)
            } }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            if (this.cameFromOnCreate) {
                return
            }

            INSTANCE?.let { database -> scope.launch {
                val phoneContacts = ContactDatabaseHelper.retrieveAllPhoneContacts(context.contentResolver)
                // No contacts found - nothing new to put in to the database
                if (phoneContacts.isEmpty()) {
                    return@launch
                }

                val dao = database.contactDao()
                val ourContactsData = dao.getAll()
                ourContactsData.observeForever(object : Observer<List<Contact>> { override fun onChanged(ourContacts: List<Contact>?) {
                    if (ourContacts == null || ourContacts.isEmpty()) {
                        scope.launch { dao.insertAll(phoneContacts) }
                        ourContactsData.removeObserver(this)
                        return
                    }

                    val contactsToInsert = mutableListOf<Contact>()
                    val ourPhoneNumbers = ourContacts.associateBy({it.phone}, {it})
                    phoneContacts.forEach { phoneContact ->
                        if (!ourPhoneNumbers.containsKey(phoneContact.phone)) {
                            scope.launch { dao.insert(phoneContact) }
                        } else {
                            var isChanged = false
                            val ourContact = ourPhoneNumbers.getValue(phoneContact.phone)

                            if (ourContact.name != phoneContact.photoUri) {
                                ourContact.name = phoneContact.name
                                isChanged = true
                            }
                            if (ourContact.photoUri != phoneContact.photoUri) {
                                ourContact.photoUri = phoneContact.photoUri
                                isChanged = true
                            }
                            if (isChanged) {
                                scope.launch { dao.update(ourContact) }
                            }
                        }
                    }
                    scope.launch { dao.insertAll(contactsToInsert) }
                    ourContactsData.removeObserver(this)
                }})
            }}
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ContactRoomDatabase? = null

        fun getDatabase (context: Context, scope: CoroutineScope): ContactRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ContactRoomDatabase::class.java,
                    "contact_database"
                ).fallbackToDestructiveMigration()
                .addCallback(ContactDatabaseCallback(context, scope))
                .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}