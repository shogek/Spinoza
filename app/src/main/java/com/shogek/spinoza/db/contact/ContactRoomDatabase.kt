package com.shogek.spinoza.db.contact

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [Contact::class],
    version = 2,
    exportSchema = false
)
abstract class ContactRoomDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao

    private class ContactDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database -> scope.launch {
                val contactDao = database.contactDao()
                contactDao.deleteAll()

                // Import contacts for phone
                val contacts = ContactDatabaseHelper.retrieveAllPhoneContacts(context.contentResolver)
                contacts.forEach { contactDao.insert(it) }
            } }
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