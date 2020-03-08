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
    version = 1,
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
                ContactDatabaseHelper.importContactsFromPhone(context, scope, database.contactDao())
            }}
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            if (this.cameFromOnCreate) {
                return
            }

            INSTANCE?.let { database -> scope.launch {
                // TODO: Only update contacts with which the conversation is happening?
                ContactDatabaseHelper.synchronizeOurContactsWithPhoneContacts(context, scope, database.contactDao())
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