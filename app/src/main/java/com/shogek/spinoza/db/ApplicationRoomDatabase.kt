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
import com.shogek.spinoza.db.contact.ContactDatabaseHelper
import com.shogek.spinoza.db.contact.ContactRepository
import com.shogek.spinoza.db.conversation.ConversationDatabaseHelper
import com.shogek.spinoza.db.conversation.ConversationRepository
import com.shogek.spinoza.db.databaseInformation.DatabaseInformation
import com.shogek.spinoza.db.message.MessageDatabaseHelper
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
    version = 12,
    exportSchema = false
)
abstract class ApplicationRoomDatabase : RoomDatabase() {

    abstract fun applicationDatabaseStateDao(): DatabaseInformationDao
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

        // TODO: [Doc] Explain
        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            INSTANCE?.let { database -> scope.launch {
                database.applicationDatabaseStateDao().nuke()
                database.conversationDao().nuke()
                database.contactDao().nuke()
                database.messageDao().nuke()

                onCreate(db)
            }}
        }

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            this.cameFromOnCreate = true

            INSTANCE?.let {
                this.importConversationsAndContactsFromPhone()
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            if (this.cameFromOnCreate) {
                this.cameFromOnCreate = false
                return
            }

            INSTANCE?.let {
                syncConversationsAndContactsWithPhone()
            }
        }

        /** Delete or update our contacts if they were deleted or updated in the phone. */
        // TODO: Sync messages
        // TODO: Refactor this shit show
        private fun syncConversationsAndContactsWithPhone() = scope.launch {
            val conversationRepository = ConversationRepository(context, scope)
            val contactRepository = ContactRepository(context, scope)
            val ourContacts = contactRepository.getAll()
            cleanupDeletedPhoneContacts(ourContacts, contactRepository)

            val databaseInformationRepository = DatabaseInformationRepository(context, scope)
            val information = databaseInformationRepository.getSingleton()
            val upsertedPhoneContacts = ContactDatabaseHelper.retrieveUpsertedPhoneContacts(context.contentResolver, information.contactTableLastUpdatedTimestamp)
            if (upsertedPhoneContacts.isEmpty()) {
                syncPhoneConversations(conversationRepository, contactRepository)
                return@launch
            }

            information.contactTableLastUpdatedTimestamp = System.currentTimeMillis()
            databaseInformationRepository.updateSingleton(information)

            // Separate upserted phone contacts - to newly added ones and updated old ones
            val ourContactTable = ourContacts.associateBy({it.androidId}, {it})
            val newPhoneContacts = mutableListOf<Contact>()
            val updatedPhoneContacts = mutableListOf<Contact>()
            for (upserted in upsertedPhoneContacts) {
                if (ourContactTable.containsKey(upserted.androidId)) {
                    updatedPhoneContacts.add(upserted)
                } else {
                    newPhoneContacts.add(upserted)
                }
            }

            contactRepository.insertAll(newPhoneContacts)
            updateOurContacts(ourContacts, updatedPhoneContacts, contactRepository)


            val contactless = conversationRepository.getAll().filter { it.contactId == null }
            val matched = matchByPhone(contactless, newPhoneContacts, onlyMatches = true)
            conversationRepository.updateAll(matched)

            syncPhoneConversations(conversationRepository, contactRepository)
        }

        // TODO: [Doc] Explain
        private fun syncPhoneConversations(
            conversationRepository: ConversationRepository,
            contactRepository: ContactRepository
        ) = scope.launch {
            val phoneConversations = ConversationDatabaseHelper.retrieveAllPhoneConversations(context.contentResolver)
            val ourConversations = conversationRepository.getAll()
            cleanupDeletedPhoneConversations(ourConversations, conversationRepository)

            val ourTable = ourConversations
                .filter { it.androidId != null } // our conversations that were imported from phone
                .associateBy({it.androidId}, {it})

            val new = mutableListOf<Conversation>()
            val updated = mutableListOf<Conversation>()
            for (phoneConversation in phoneConversations) {
                val ourConversation = ourTable[phoneConversation.androidId]
                if (ourConversation == null) {
                    // New conversation was created
                    new.add(phoneConversation)
                    continue
                }

                if (phoneConversation.snippetTimestamp == ourConversation.snippetTimestamp
                    && phoneConversation.snippetWasRead == ourConversation.snippetWasRead) {
                    continue
                }

                // A phone conversation was updated
                ourConversation.snippet = phoneConversation.snippet
                ourConversation.snippetIsOurs = phoneConversation.snippetIsOurs
                ourConversation.snippetWasRead = phoneConversation.snippetWasRead
                ourConversation.snippetTimestamp = phoneConversation.snippetTimestamp
                updated.add(ourConversation)
            }

            conversationRepository.updateAll(updated)
            val ourContacts = contactRepository.getAll()
            val matched = matchByPhone(new, ourContacts, onlyMatches = false)
            conversationRepository.insertAll(matched)
            // TODO: Take these created/updated conversations and do the same for their messages
        }

        private fun importConversationsAndContactsFromPhone() = scope.launch {
            val databaseInformationRepository = DatabaseInformationRepository(context, scope)
            val information = databaseInformationRepository.getSingleton()

            val phoneConversations = ConversationDatabaseHelper.retrieveAllPhoneConversations(context.contentResolver)
            val conversationRepository = ConversationRepository(context, scope)
            val ourConversationsIds = conversationRepository.insertAll(phoneConversations)
            val ourConversations = conversationRepository.getAll(ourConversationsIds)

            val messages = MessageDatabaseHelper.retrieveMessagesForPhoneConversations(context.contentResolver, ourConversations)
            val contacts = ContactDatabaseHelper.retrieveAllPhoneContacts(context.contentResolver)
            val contactRepository = ContactRepository(context, scope)
            val ourContactIds = contactRepository.insertAll(contacts)
            val ourContacts = contactRepository.getAll(ourContactIds)
            information.contactTableLastUpdatedTimestamp = System.currentTimeMillis()

            val conversationsWithContacts = matchByPhone(ourConversations, ourContacts, onlyMatches = false)
            DatabaseInformationRepository(context, scope).updateSingleton(information)
            conversationRepository.updateAll(conversationsWithContacts)
            MessageRepository(context, scope).insertAll(messages)
        }

        private fun updateOurContacts(
            ourContacts: List<Contact>,
            phoneContacts: List<Contact>,
            contactRepository: ContactRepository
        ) {
            val toUpdate = mutableListOf<Contact>()

            val ourContactTable = ourContacts.associateBy({it.androidId}, {it})
            for (phoneContact in phoneContacts) {
                val ourContact = ourContactTable.getValue(phoneContact.androidId)
                ourContact.name = phoneContact.name
                ourContact.phone = phoneContact.phone
                ourContact.photoUri = phoneContact.photoUri
                toUpdate.add(ourContact)
            }

            scope.launch { contactRepository.updateAll(toUpdate) }
        }

        /**
         * Assign a contact to a conversation if their phone numbers match.
         * @param onlyMatches true: return only matches (contacless conversations discarded), else: return same list
         */
        private fun matchByPhone(
            conversations: List<Conversation>,
            contacts: List<Contact>,
            onlyMatches: Boolean
        ): List<Conversation> {
            val result = mutableListOf<Conversation>()

            for (conversation in conversations) {
                var match: Contact? = null

                for (contact in contacts) {
                    if (PhoneNumberUtils.compare(conversation.phone, contact.phone)) {
                        match = contact
                        break
                    }
                }

                if (match != null) {
                    conversation.contactId = match.id
                    result.add(conversation)
                } else {
                    if (!onlyMatches) {
                        result.add(conversation)
                    }
                }
            }

            return result
        }

        private fun cleanupDeletedPhoneConversations(
            ourConversations: List<Conversation>,
            conversationRepository: ConversationRepository
        ) {
            val deleted = ConversationDatabaseHelper.retrieveDeletedPhoneConversations(context.contentResolver, ourConversations)
            if (deleted.isNotEmpty()) {
                scope.launch { conversationRepository.deleteAll(deleted) }
            }
        }

        private fun cleanupDeletedPhoneContacts(
            ourContacts: List<Contact>,
            contactRepository: ContactRepository
        ) {
            val deleted = ContactDatabaseHelper.retrieveDeletedPhoneContacts(context.contentResolver, ourContacts)
            if (deleted.isNotEmpty()) {
                scope.launch { contactRepository.deleteAll(deleted) }
            }
        }
    }
}