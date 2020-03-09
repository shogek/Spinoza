package com.shogek.spinoza.db.state

import android.content.Context
import android.telephony.PhoneNumberUtils
import androidx.lifecycle.Observer
import com.shogek.spinoza.db.contact.Contact
import com.shogek.spinoza.db.contact.ContactDao
import com.shogek.spinoza.db.contact.ContactDatabaseHelper
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationDao
import com.shogek.spinoza.db.conversation.ConversationDatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object CommonDatabaseState {

    /** Populate database with conversations already in the phone. */
    fun importConversationsFromPhone(
        context: Context,
        scope: CoroutineScope,
        contactDao: ContactDao,
        conversationDao: ConversationDao
    ) {
        val phoneConversations = ConversationDatabaseHelper.retrieveAllPhoneConversations(context.contentResolver)
        if (phoneConversations.isEmpty()) {
            return
        }

        val contactCount = ContactDatabaseHelper.retrievePhoneContactCount(context.contentResolver)
        if (contactCount == null || contactCount < 1) {
            scope.launch { conversationDao.insertAll(phoneConversations) }
            return
        }

        val contactData = contactDao.getAll()
        contactData.observeForever(object : Observer<List<Contact>> { override fun onChanged(ourContacts: List<Contact>?) {
            if (ourContacts == null || ourContacts.isEmpty()) {
                // DO NOT REMOVE OBSERVER HERE - we already know there are contacts to be imported - we only need to wait
                return
            }

            val matched = matchByPhone(phoneConversations, ourContacts, onlyMatches = false)
            scope.launch { conversationDao.insertAll(matched) }
            contactData.removeObserver(this)
        }})
    }

    /** Insert contacts if we found matching conversations for it. */
    fun synchronizeDatabaseWithExistingConversationsAndContacts(
        context: Context,
        scope: CoroutineScope,
        conversationDao: ConversationDao
    ) {
        val conversationData = conversationDao.getAll()
        conversationData.observeForever(object : Observer<List<Conversation>> { override fun onChanged(conversations: List<Conversation>?) {
            conversationData.removeObserver(this)

            val contactless = conversations?.filter { it.contactId == null }
            if (contactless == null || contactless.isEmpty()) {
                return
            }

            // TODO: [Optimize] Query contacts we do not already have saved in our database
            val contacts = ContactDatabaseHelper.retrieveAllPhoneContacts(context.contentResolver)
            if (contacts.isEmpty()) {
                return
            }

            val matched = matchByPhone(contactless, contacts, onlyMatches = true)
            scope.launch {
                matched.forEach { conversation ->
                    // TODO: Contact database should check and insert new contacts
                    conversationDao.update(conversation)
                }
            }
        }})
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
}