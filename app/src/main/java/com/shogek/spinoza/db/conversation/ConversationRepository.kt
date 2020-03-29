package com.shogek.spinoza.db.conversation

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.CoroutineScope
import com.shogek.spinoza.db.ApplicationRoomDatabase
import com.shogek.spinoza.db.ModelHelpers


class ConversationRepository(
    context: Context,
    scope: CoroutineScope
) {

    private val conversationDao = ApplicationRoomDatabase.getDatabase(context, scope).conversationDao()
    private val contactDao = ApplicationRoomDatabase.getDatabase(context, scope).contactDao()
    private val messageDao = ApplicationRoomDatabase.getDatabase(context, scope).messageDao()


    suspend fun get(conversationId: Long): Conversation {
        return conversationDao.get(conversationId)
    }

    fun getObservable(conversationId: Long): LiveData<Conversation> {
        val conversationData = conversationDao.getObservable(conversationId)
        return Transformations.map(conversationData, ::combineContactAndMessages)
    }

    suspend fun getByContactId(contactId: Long): Conversation {
        val contactData = conversationDao.getByContactId(contactId)
        return combineContact(listOf(contactData)).first()
    }

    fun getWithContactAndMessagesObservable(conversationId: Long): LiveData<Conversation> {
        return Transformations.map(conversationDao.getWithContactAndMessagesObservable(conversationId), ::combineContactAndMessages)
    }

    fun getAllWithContactsObservable(): LiveData<List<Conversation>> {
        return Transformations.map(conversationDao.getAllWithContactsObservable(), ::combineContact)
    }

    suspend fun getByPhone(phone: String): Conversation? {
        val pair = conversationDao.getByPhone(phone)
            ?: return null
        return combineContact(listOf(pair)).first()
    }

    /** Tries to get a conversation by a contact ID, if it fails - it creates a new conversation. */
    fun getByContactIdObservable(contactId: Long): LiveData<Conversation> {
        val conversationData = conversationDao.getByContactIdObservable(contactId)
        return Transformations.map(conversationData, ::combineContactAndMessages)
    }

    // TODO: [Check] Is this still needed?
    /** SIDE EFFECT - updates foreign key for associated contact */
    suspend fun insertAll(conversations: List<Conversation>): List<Long> {
        if (conversations.isEmpty()) {
            return listOf()
        }

        val contactless = conversations.filter { it.contactId == null }
        if (contactless.isEmpty()) {
            return conversationDao.insertAll(conversations)
        }

        val contacts = contactDao.getAll()
        val matched = ModelHelpers.matchByPhone(conversations, contacts, onlyMatches = false)
        return conversationDao.insertAll(matched)
    }

    suspend fun update(conversation: Conversation) {
        conversationDao.update(conversation)
    }

    /** SIDE EFFECT - deletes associated messages */
    suspend fun deleteAll(conversations: List<Conversation>) {
        if (conversations.isEmpty()) {
            return
        }

        val conversationIds = conversations.map { it.id }
        messageDao.deleteAllByConversationIds(conversationIds)
        conversationDao.deleteAll(conversations)
    }

    // TODO: [Refactor] Accept single items as well
    private fun combineContact(pairs: List<ConversationAndContact>): List<Conversation> {
        val conversations = mutableListOf<Conversation>()

        for (pair in pairs) {
            pair.conversation.contact = pair.contact
            conversations.add(pair.conversation)
        }

        return conversations
    }

    private fun combineContactAndMessages(item: ConversationAndContactAndMessages): Conversation {
        item.conversation.contact = item.contact
        item.conversation.messages = item.messages.sortedBy { it.timestamp }
        return item.conversation
    }
}