package com.shogek.spinoza.caches

import android.content.ContentResolver
import com.shogek.spinoza.models.Message
import com.shogek.spinoza.repositories.MessageRepository

object MessageCache {
    /** Telephony.Sms.Conversations.THREAD_ID returns a list of that conversation Messages */
    private val cache: HashMap<Number, Array<Message>> = HashMap()

    fun getAll(resolver: ContentResolver,
               threadId: Number
    ) : Array<Message> {
        // Return cached result if possible
        if (this.cache.containsKey(threadId)) {
            return this.cache[threadId]!!
        }

        val messages = MessageRepository.getAll(resolver, threadId)

        // Cache the result
        this.cache[threadId] = messages

        return messages
    }
}