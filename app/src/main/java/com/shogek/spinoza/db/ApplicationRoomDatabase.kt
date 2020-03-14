package com.shogek.spinoza.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shogek.spinoza.db.message.Message
import com.shogek.spinoza.db.contact.Contact
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.databaseInformation.DatabaseInformationRepository
import com.shogek.spinoza.db.message.MessageDao
import com.shogek.spinoza.db.contact.ContactDao
import com.shogek.spinoza.db.conversation.ConversationDao
import com.shogek.spinoza.db.databaseInformation.DatabaseInformationDao
import com.shogek.spinoza.db.contact.AndroidContactResolver
import com.shogek.spinoza.db.contact.ContactRepository
import com.shogek.spinoza.db.conversation.AndroidConversationResolver
import com.shogek.spinoza.db.conversation.ConversationRepository
import com.shogek.spinoza.db.databaseInformation.DatabaseInformation
import com.shogek.spinoza.db.message.AndroidMessageResolver
import com.shogek.spinoza.db.message.MessageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Database(
    entities = [
        DatabaseInformation::class,
        Conversation::class,
        Contact::class,
        Message::class
    ],
    version = 22,
    exportSchema = false
)
abstract class ApplicationRoomDatabase : RoomDatabase() {

    abstract fun databaseInformationDao(): DatabaseInformationDao
    abstract fun conversationDao(): ConversationDao
    abstract fun contactDao(): ContactDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: ApplicationRoomDatabase? = null

        fun getDatabase (
            context: Context,
            scope: CoroutineScope
        ): ApplicationRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ApplicationRoomDatabase::class.java,
                    "application_database"
                ).addCallback(ApplicationRoomDatabaseCallback(context, scope))
                 .fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                return instance
            }
        }
    }

    private class ApplicationRoomDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        private var cameFromOnCreate = false


        /** Destroy the database before recreating it again. */
        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            INSTANCE?.let { database -> scope.launch {
                database.databaseInformationDao().nuke()
                database.conversationDao().nuke()
                database.contactDao().nuke()
                database.messageDao().nuke()

                onCreate(db)
            }}
        }

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            this.cameFromOnCreate = true

            INSTANCE?.let { scope.launch {
                val contactRepository = ContactRepository(context, scope)
                val messageRepository = MessageRepository(context, scope)
                val conversationRepository = ConversationRepository(context, scope)
                val databaseInformationRepository = DatabaseInformationRepository(context, scope)

                importAndroidContacts(databaseInformationRepository, contactRepository)
                importAndroidConversations(conversationRepository)
                importAndroidMessages(conversationRepository, messageRepository)
            }}
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            if (this.cameFromOnCreate) {
                this.cameFromOnCreate = false
                return
            }

            INSTANCE?.let { scope.launch {
                val contactRepository = ContactRepository(context, scope)
                val messageRepository = MessageRepository(context, scope)
                val conversationRepository = ConversationRepository(context, scope)
                val databaseInformationRepository = DatabaseInformationRepository(context, scope)

                synchronizeAndroidContacts(databaseInformationRepository, contactRepository)
                synchronizeAndroidConversations(conversationRepository)
                synchronizeAndroidMessages(messageRepository, conversationRepository)
            }}
        }

        /** Import new messages or delete removed ones in case we're not the default messaging application. */
        private suspend fun synchronizeAndroidMessages(
            messageRepository: MessageRepository,
            conversationRepository: ConversationRepository
        ) {
            val ourMessages = messageRepository.getAllAndroid()

            // Delete removed messages
            val removedMessages = AndroidMessageResolver.retrieveDeletedAndroidMessages(context.contentResolver, ourMessages)
            messageRepository.deleteAll(removedMessages)

            // Retrieve new messages
            val ourConversations = conversationRepository.getAllAndroid()
            val ourUpdatedMessages = ourMessages.filter { !removedMessages.contains(it) }
            val newAndroidMessages = AndroidMessageResolver.retrieveNewAndroidMessages(context.contentResolver, ourUpdatedMessages, ourConversations)
            messageRepository.insertAll(newAndroidMessages)
        }

        /** Import upserted conversations or delete removed ones in case we're not the default messaging application. */
        private suspend fun synchronizeAndroidConversations(
            conversationRepository: ConversationRepository
        ) {
            val androidConversations = AndroidConversationResolver.retrieveAllAndroidConversations(context.contentResolver)
            val ourConversations = conversationRepository.getAll()

            // Delete removed conversations
            val deletedConversations = AndroidConversationResolver.retrieveDeletedAndroidConversations(context.contentResolver, ourConversations)
            conversationRepository.deleteAll(deletedConversations)

            // Separate contacts to newly added ones and updated old ones
            val newConversations = mutableListOf<Conversation>()
            val updatedConversations = mutableListOf<Conversation>()

            val ourTable = ourConversations
                .filter { it.androidId != null } // our conversations that were imported from phone
                .associateBy({it.androidId}, {it})

            for (androidConversation in androidConversations) {
                val ourConversation = ourTable[androidConversation.androidId]
                if (ourConversation == null) {
                    newConversations.add(androidConversation)
                } else if (androidConversation.snippetTimestamp != ourConversation.snippetTimestamp || // new message
                           androidConversation.snippetWasRead != ourConversation.snippetWasRead) { // current message was seen
                    updatedConversations.add(androidConversation)
                }
            }

            conversationRepository.insertAll(newConversations)
            conversationRepository.updateAll(copyOverConversationValues(ourConversations, updatedConversations))
        }

        /** Update our contact database if any contacts were deleted or upserted in the phone. */
        private suspend fun synchronizeAndroidContacts(
            databaseInformationRepository: DatabaseInformationRepository,
            contactRepository: ContactRepository
        ) {
            val information = databaseInformationRepository.getSingleton()
            val ourContacts = contactRepository.getAll()

            // Delete removed contacts
            val deleted = AndroidContactResolver.retrieveDeletedAndroidContacts(context.contentResolver, ourContacts)
            contactRepository.deleteAll(deleted)

            // Check if any contacts were updated
            val upsertedContacts = AndroidContactResolver.retrieveUpsertedAndroidContacts(context.contentResolver, information.contactTableLastUpdatedTimestamp)
            information.contactTableLastUpdatedTimestamp = System.currentTimeMillis()
            databaseInformationRepository.updateSingleton(information)
            if (upsertedContacts.isEmpty()) {
                return
            }

            // Separate contacts to newly added ones and updated old ones
            val ourContactTable = ourContacts.associateBy({it.androidId}, {it})
            val newContacts = mutableListOf<Contact>()
            val updatedContacts = mutableListOf<Contact>()

            for (upserted in upsertedContacts) {
                if (ourContactTable.containsKey(upserted.androidId)) {
                    updatedContacts.add(upserted)
                } else {
                    newContacts.add(upserted)
                }
            }

            contactRepository.insertAll(newContacts)
            contactRepository.updateAll(copyOverContactValues(ourContacts, updatedContacts))
        }

        /** Populate our contact database with the phone's contacts. */
        private suspend fun importAndroidContacts(
            databaseInformationRepository: DatabaseInformationRepository,
            contactRepository: ContactRepository
        ) {
            val information = databaseInformationRepository.getSingleton()

            val androidContacts = AndroidContactResolver.retrieveAllAndroidContacts(context.contentResolver)
            information.contactTableLastUpdatedTimestamp = System.currentTimeMillis()
            contactRepository.insertAll(androidContacts)
            databaseInformationRepository.updateSingleton(information)
        }

        /** Populate our conversation database by importing already existing ones in the phone. */
        private suspend fun importAndroidConversations(
            conversationRepository: ConversationRepository
        ) {
            val androidConversations = AndroidConversationResolver.retrieveAllAndroidConversations(context.contentResolver)
            conversationRepository.insertAll(androidConversations)
        }

        /** Populate our message database by importing already existing ones in the phone. */
        private suspend fun importAndroidMessages(
            conversationRepository: ConversationRepository,
            messageRepository: MessageRepository
        ) {
            val ourAndroidConversations = conversationRepository.getAllAndroid()
            val androidMessages = AndroidMessageResolver.retrieveAllAndroidMessages(context.contentResolver, ourAndroidConversations)
            messageRepository.insertAll(androidMessages)
        }

        private fun copyOverContactValues(
            ourContacts: List<Contact>,
            androidContacts: List<Contact>
        ): List<Contact> {
            if (ourContacts.isEmpty() || androidContacts.isEmpty()) {
                return listOf()
            }

            val updated = mutableListOf<Contact>()

            val ourContactTable = ourContacts.associateBy({it.androidId}, {it})
            for (androidContact in androidContacts) {
                val ourContact = ourContactTable.getValue(androidContact.androidId)
                ourContact.name = androidContact.name
                ourContact.phone = androidContact.phone
                ourContact.photoUri = androidContact.photoUri
                updated.add(ourContact)
            }

            return updated
        }

        private fun copyOverConversationValues(
            ourConversations: List<Conversation>,
            androidConversations: List<Conversation>
        ): List<Conversation> {
            if (ourConversations.isEmpty() || androidConversations.isEmpty()) {
                return listOf()
            }

            val updated = mutableListOf<Conversation>()

            val ourTable = ourConversations.associateBy({it.androidId}, {it})
            for (androidConversation in androidConversations) {
                val ourConversation = ourTable.getValue(androidConversation.androidId)
                ourConversation.snippet = androidConversation.snippet
                ourConversation.snippetIsOurs = androidConversation.snippetIsOurs
                ourConversation.snippetWasRead = androidConversation.snippetWasRead
                ourConversation.snippetTimestamp = androidConversation.snippetTimestamp
                updated.add(ourConversation)
            }

            return updated
        }
    }
}