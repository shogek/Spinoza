package com.shogek.spinoza.db.conversation

import android.content.Context
import androidx.lifecycle.Observer
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shogek.spinoza.db.contact.Contact
import com.shogek.spinoza.db.contact.ContactRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [Conversation::class, Contact::class],
    version = 4,
    exportSchema = false
)
abstract class ConversationRoomDatabase : RoomDatabase() {

    abstract fun conversationDao(): ConversationDao

    private class ConversationDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        // TODO: FINISH
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database -> scope.launch {
                val conversationDao = database.conversationDao()
                conversationDao.deleteAll()
                val conversations = ConversationDatabaseHelper.retrieveAllPhoneConversations(context.contentResolver)

                val contactDao = ContactRoomDatabase.getDatabase(context, scope).contactDao()
                val contactData = contactDao.getAll()
                contactData.observeForever(object : Observer<List<Contact>> {
                    override fun onChanged(contacts: List<Contact>?) {
                        // No contacts to map with
                        if (contacts == null || contacts.isEmpty()) {
                            scope.launch { conversationDao.insertAll(conversations) }
                            contactData.removeObserver(this)
                            return
                        }

                        // Create a dictionary - key: contact's phone, value: contact itself
                        val contactPhoneToContact = contacts.associateBy({it.phone}, {it})
                        conversations.forEach { conversation ->
                            if (contactPhoneToContact.containsKey(conversation.phone)) {
                                conversation.contactId = contactPhoneToContact.getValue(conversation.phone).id
                            }
                        }
                        scope.launch { conversationDao.insertAll(conversations) }
                        contactData.removeObserver(this)
                    }
                })
            } }
        }
    }



    companion object {
        @Volatile
        private var INSTANCE: ConversationRoomDatabase? = null

        fun getDatabase (
            context: Context,
            scope: CoroutineScope
        ): ConversationRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ConversationRoomDatabase::class.java,
                    "conversation_database"
                ).addCallback(ConversationDatabaseCallback(context, scope))
                 .fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}