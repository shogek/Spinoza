package com.shogek.spinoza.db.state

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.util.Log
import com.shogek.spinoza.db.contact.Contact
import com.shogek.spinoza.db.contact.ContactDao
import com.shogek.spinoza.db.contact.ContactDatabaseHelper
import com.shogek.spinoza.db.contact.ContactRoomDatabase
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationDao
import com.shogek.spinoza.db.conversation.ConversationDatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object CommonDatabaseState {

    private val TAG = CommonDatabaseState.javaClass.simpleName

    /** Populate database with conversations and contacts already in the phone. */
    fun populateDatabaseWithExistingConversationsAndContacts(
        context: Context,
        scope: CoroutineScope,
        conversationDao: ConversationDao
    ) {
        val phoneConversations = ConversationDatabaseHelper.retrieveAllPhoneConversations(context.contentResolver)
        if (phoneConversations.isEmpty()) {
            Log.i(TAG, ::populateDatabaseWithExistingConversationsAndContacts.name + " - no conversations found in phone")
            return
        }

        val phoneContacts = ContactDatabaseHelper.retrieveAllPhoneContacts(context.contentResolver)
        if (phoneContacts.isEmpty()) {
            scope.launch { conversationDao.insertAll(phoneConversations) }
            Log.i(TAG, ::populateDatabaseWithExistingConversationsAndContacts.name + " - no contacts found in phone")
            return
        }

        val contactDao = ContactRoomDatabase.getDatabase(context, scope).contactDao()
        val matchedConversations = matchByPhone(phoneConversations, phoneContacts)
        insertToDatabase(scope, conversationDao, contactDao, matchedConversations)
    }

    /** Insert conversations and their contacts to databases. */
    private fun insertToDatabase(
        scope: CoroutineScope,
        conversationDao: ConversationDao,
        contactDao: ContactDao,
        conversations: List<Conversation>
    ) = scope.launch {
        conversations.forEach { conversation ->
            if (conversation.contact != null) {
                contactDao.insert(conversation.contact!!)
            }
            conversationDao.insert(conversation)
        }
    }

    /** Assign a contact to a conversation if their phone numbers match. */
    private fun matchByPhone(
        conversations: List<Conversation>,
        contacts: List<Contact>
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
                conversation.contact = match
            }
            result.add(conversation)
        }

        return result
    }
}