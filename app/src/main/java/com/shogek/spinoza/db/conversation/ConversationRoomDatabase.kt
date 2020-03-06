package com.shogek.spinoza.db.conversation

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shogek.spinoza.db.contact.Contact
import com.shogek.spinoza.db.state.CommonDatabaseState
import kotlinx.coroutines.CoroutineScope

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

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            this.cameFromOnCreate = true

            INSTANCE?.let { database ->
                CommonDatabaseState.populateDatabaseWithExistingConversationsAndContacts(context, scope, database.conversationDao())
            }
        }

        // TODO: Finish
//        override fun onOpen(db: SupportSQLiteDatabase) {
//            super.onOpen(db)
//            if (this.cameFromOnCreate) {
//                return
//            }
//
//            INSTANCE?.let { database -> scope.launch {
//                /** Check if contact records were created for previously unknown numbers and associate them with conversations. */
//                val conversationDao = database.conversationDao()
//                val conversationData = conversationDao.getAll()
//                conversationData.observeForever(object : Observer<List<Conversation>> { override fun onChanged(conversations: List<Conversation>?) {
//                    conversationData.removeObserver(this)
//                    // TODO: Test path
//                    if (conversations == null || conversations.isEmpty()) {
//                        // 1. All conversations were deleted while another app was used as default messaging app
//                        // 2. No conversations were found on the phone
//                        scope.launch { conversationDao.deleteAll() }
//                        return
//                    }
//
//                    val contactDao = ContactRoomDatabase.getDatabase(context, scope).contactDao()
//                    val contactData = contactDao.getAll()
//                    contactData.observeForever(object : Observer<List<Contact>> { override fun onChanged(contacts: List<Contact>?) {
//                        contactData.removeObserver(this)
//                        // TODO: Test path
//                        if (contacts == null || contacts.isEmpty()) {
//                            // 1. The psychopath deleted all his contacts
//                            // 2. New phone who dis
//                            scope.launch {
//                                conversations.forEach { it.contact = null }
//                                conversationDao.updateAll(conversations)
//                                contactDao.deleteAll()
//                            }
//                            return
//                        }
//
//                        // We have conversations and we contacts - check if a conversation is missing it's associated contact
//                        ConversationDatabaseHelper.pairContactlessConversationsWithContacts(scope, conversationDao, conversations, contacts)
//                    }})
//                }})
//            }}
//        }
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