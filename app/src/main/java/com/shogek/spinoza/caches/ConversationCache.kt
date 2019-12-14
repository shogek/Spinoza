package com.shogek.spinoza.caches

import android.content.ContentResolver
import com.shogek.spinoza.models.Conversation
import com.shogek.spinoza.repositories.ConversationRepository

object ConversationCache {
    /** Telephony.Sms.Conversations.THREAD_ID returns Conversation */
    private val conversations: HashMap<Number, Conversation> = HashMap()

    fun get(threadId: Number) : Conversation? {
        return this.conversations.getOrDefault(threadId.toInt(), null)
    }

    fun getAll(resolver: ContentResolver,
               clearCache: Boolean = false
    ) : Array<Conversation> {
        // Return cached result if possible
        if (this.conversations.isNotEmpty() && !clearCache) {
            return this.conversations.values.toTypedArray()
        }

        val conversations = ConversationRepository.getAll(resolver)

        // Cache the result
        conversations.forEach { c -> this.conversations[c.threadId] = c }

        return conversations
    }
}