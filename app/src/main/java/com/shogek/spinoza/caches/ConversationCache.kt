package com.shogek.spinoza.caches

import android.content.ContentResolver
import com.shogek.spinoza.models.Conversation
import com.shogek.spinoza.models.Message
import com.shogek.spinoza.repositories.ConversationRepository

object ConversationCache {
    /** Telephony.Sms.Conversations.THREAD_ID returns Conversation */
    private val cache: HashMap<Number, Conversation> = HashMap()

    fun get(threadId: Number) : Conversation? {
        return this.cache.getOrDefault(threadId.toInt(), null)
    }

    fun notifyMessageReceived(
        threadId: Number,
        message: Message
    ) {
        val conversation = this.cache[threadId]!!
        conversation.latestMessageTimestamp = message.dateTimestamp
        conversation.latestMessageText = message.text
    }

    fun getAll(
        resolver: ContentResolver,
        clearCache: Boolean = false
    ) : Array<Conversation> {
        // Return cached result if possible
        if (this.cache.isNotEmpty() && !clearCache) {
            return this.cache.values.toTypedArray()
        }

        val conversations = ConversationRepository.getAll(resolver)

        // Cache the result
        conversations.forEach { c -> this.cache[c.threadId] = c }

        return conversations
    }
}