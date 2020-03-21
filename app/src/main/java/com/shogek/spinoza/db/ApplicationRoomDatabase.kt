package com.shogek.spinoza.db

import android.content.Context
import android.telephony.PhoneNumberUtils
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
import com.shogek.spinoza.db.conversation.AndroidConversationResolver
import com.shogek.spinoza.db.databaseInformation.DatabaseInformation
import com.shogek.spinoza.db.message.AndroidMessageResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Database(
    entities = [
        DatabaseInformation::class,
        Conversation::class,
        Contact::class,
        Message::class
    ],
    version = 1,
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

            INSTANCE?.let { database -> scope.launch {
                val ourContacts = importAndroidContacts(database.contactDao())
                val ourConversations = importAndroidConversations(database.conversationDao(), ourContacts)
                importAndroidMessages(database.messageDao(), ourConversations)
            }}
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            if (this.cameFromOnCreate) {
                this.cameFromOnCreate = false
                return
            }

            INSTANCE?.let { database -> scope.launch {
                val databaseInformationRepository = DatabaseInformationRepository(context, scope)
                synchronizeAndroidContacts(databaseInformationRepository, database.conversationDao(), database.contactDao())
            }}
        }

        /** Populate our contact database with the phone's contacts. */
        private suspend fun importAndroidContacts(contactDao: ContactDao): List<Contact> {
            val informationRepository = DatabaseInformationRepository(context, scope)
            val information = informationRepository.getSingleton()

            val androidContacts = AndroidContactResolver.retrieveAllAndroidContacts(context.contentResolver)
            information.contactTableLastUpdatedTimestamp = System.currentTimeMillis()
            informationRepository.updateSingleton(information)

            contactDao.insertAll(androidContacts)
            return contactDao.getAll()
        }

        /** Populate our conversation database by importing already existing ones in the phone and creating new empty ones for contacts. */
        private suspend fun importAndroidConversations(
            conversationDao: ConversationDao,
            ourContacts: List<Contact>
        ): List<Conversation> {
            val androidConversations = AndroidConversationResolver.retrieveAllAndroidConversations(context.contentResolver)

            // Keep track of contacts without conversations
            val ourContactTable = ourContacts.associateBy({it.id}, {it}).toMutableMap()

            for (androidConversation in androidConversations) {
                for (ourContact in ourContacts) {
                    if (PhoneNumberUtils.compare(androidConversation.phone, ourContact.phone)) {
                        androidConversation.contactId = ourContact.id
                        ourContactTable.remove(ourContact.id)
                        break
                    }
                }
            }

            conversationDao.insertAll(androidConversations)
            createEmptyConversations(conversationDao, ourContactTable.values.toList())

            return conversationDao.getAll()
        }

        /** Populate our message database by importing already existing ones in the phone. */
        private suspend fun importAndroidMessages(
            messageDao: MessageDao,
            ourConversations: List<Conversation>
        ) {
            val androidMessages = AndroidMessageResolver.retrieveAllAndroidMessages(context.contentResolver, ourConversations)
            messageDao.insertAll(androidMessages)
        }

        /** Update our contact database if any contacts were deleted or upserted in the phone. */
        private suspend fun synchronizeAndroidContacts(
            databaseInformationRepository: DatabaseInformationRepository,
            conversationDao: ConversationDao,
            contactDao: ContactDao
        ) {
            val information = databaseInformationRepository.getSingleton()
            val ourContacts = contactDao.getAll()

            // Delete removed contacts
            val deleted = AndroidContactResolver.retrieveDeletedAndroidContacts(context.contentResolver, ourContacts)
            contactDao.deleteAll(deleted)
            deleteEmptyConversations(conversationDao, deleted)

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
            contactDao.updateAll(copyOverContactValues(ourContacts, updatedContacts))

            // Check if newly created android contacts belong to any conversation
            val ourNewContacts = contactDao.getAll(contactDao.insertAll(newContacts))
            val contactless = conversationDao.getContactless()
            val matches = ModelHelpers.matchByPhone(contactless, ourNewContacts, onlyMatches = true)
            conversationDao.updateAll(matches)

            // Filter out new contacts that were not associated with any conversation
            val newTable = ourNewContacts.associateBy({it.id}, {it}).toMutableMap()
            for (match in matches) {
                if (newTable.containsKey(match.contactId)) {
                    newTable.remove(match.contactId)
                }
            }
            createEmptyConversations(conversationDao, newTable.values.toList())
        }

        /** Create an empty conversation for every contact.
         * Reason: if we have a contact, we are guaranteed to have a conversation == no null checks. */
        private fun createEmptyConversations(
            conversationDao: ConversationDao,
            addedContacts: List<Contact>
        ) = scope.launch {
            val toCreate = mutableListOf<Conversation>()

            for (contact in addedContacts) {
                val conversation = Conversation(
                    null,
                    contact.id,
                    contact.phone,
                    "",
                    System.currentTimeMillis(),
                    snippetIsOurs = true,
                    snippetWasRead = true
                )
                toCreate.add(conversation)
            }

            conversationDao.insertAll(toCreate)
        }

        /** If a deleted contact had an empty conversation - delete it, else, un-associate it from conversation. */
        private fun deleteEmptyConversations(
            conversationDao: ConversationDao,
            deletedOurContacts: List<Contact>
        ) = scope.launch {
            val deletedIds = deletedOurContacts.map { it.id }
            val items = conversationDao.getByContactIdsWithMessages(deletedIds)

            val toDelete = mutableListOf<Conversation>()
            val toUpdate = mutableListOf<Conversation>()
            for (item in items) {
                if (item.messages.isEmpty()) {
                    toDelete.add(item.conversation)
                } else {
                    item.conversation.contactId = null
                    toUpdate.add(item.conversation)
                }
            }
            conversationDao.deleteAll(toDelete)
            conversationDao.updateAll(toUpdate)
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
    }
}