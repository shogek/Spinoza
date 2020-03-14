package com.shogek.spinoza.db.conversation

import android.content.Context
import androidx.lifecycle.LiveData
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

    /** SIDE EFFECT - deletes associated messages */
    suspend fun deleteAll(conversations: List<Conversation>) {
        if (conversations.isEmpty()) {
            return
        }

        // TODO: Move to common reusable func
        val conversationIds = conversations.map { it.id }
        messageDao.deleteAllByConversationIds(conversationIds)
        conversationDao.deleteAll(conversations)
    }

    suspend fun deleteAllByIds(conversationIds: List<Long>) {
        if (conversationIds.isEmpty()) {
            return
        }

        // TODO: Move to common reusable func
        messageDao.deleteAllByConversationIds(conversationIds)
        conversationDao.deleteAllByIds(conversationIds)
    }

    suspend fun getByContactIds(contactIds: List<Long>): List<Conversation> {
        if (contactIds.isEmpty()) {
            return listOf()
        }
        return conversationDao.getByContactIds(contactIds)
    }

    suspend fun getAllAndroid(): List<Conversation> {
        return conversationDao.getAllAndroid()
    }

    suspend fun getAll(): List<Conversation> {
        return conversationDao.getAll()
    }

    suspend fun getAll(ids: List<Long>): List<Conversation> {
        if (ids.isEmpty()) {
            return listOf()
        }
        return conversationDao.getAll(ids)
    }

    fun getAllObservable(): LiveData<List<Conversation>> {
        return conversationDao.getAllObservable()
    }

    suspend fun get(id: Long): Conversation {
        return conversationDao.get(id)
    }

    fun getObservable(id: Long): LiveData<Conversation> {
        return conversationDao.getObservable(id)
    }

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

    suspend fun updateAll(conversations: List<Conversation>) {
        if (conversations.isNotEmpty()) {
            conversationDao.updateAll(conversations)
        }
    }

    suspend fun update(
        id: Long,
        snippet: String,
        snippetTimestamp: Long,
        snippetIsOurs: Boolean,
        snippetWasRead: Boolean
    ) {
        conversationDao.update(id, snippet, snippetTimestamp, snippetIsOurs, snippetWasRead)
    }
}