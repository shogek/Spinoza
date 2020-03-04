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

        private var cameFromOnCreate = false

        // TODO: FINISH
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            this.cameFromOnCreate = true

            INSTANCE?.let { database -> scope.launch {
                val conversations = ConversationDatabaseHelper.retrieveAllPhoneConversations(context.contentResolver)
                if (conversations.isEmpty()) {
                    // No conversations to import from phone
                    return@launch
                }

                val conversationDao = database.conversationDao()
                val contactDao = ContactRoomDatabase.getDatabase(context, scope).contactDao()
                val contactData = contactDao.getAll()
                contactData.observeForever(object : Observer<List<Contact>> { override fun onChanged(contacts: List<Contact>?) {
                    if (contacts == null || contacts.isEmpty()) {
                        scope.launch { conversationDao.insertAll(conversations) }
                        contactData.removeObserver(this)
                        return
                    }

                    val contactPhoneToContact = contacts.associateBy({it.phone}, {it})
                    conversations.forEach { conversation ->
                        if (contactPhoneToContact.containsKey(conversation.phone)) {
                            conversation.contact = contactPhoneToContact.getValue(conversation.phone)
                        }
                    }
                    scope.launch { conversationDao.insertAll(conversations) }
                    contactData.removeObserver(this)
                }})
            }}
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