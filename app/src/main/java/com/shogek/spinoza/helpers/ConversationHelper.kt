package com.shogek.spinoza.helpers

import com.shogek.spinoza.models.Contact
import com.shogek.spinoza.models.Conversation

object ConversationHelper {
    /**
     * Assign 'Contact' records to appropriate 'Conversation' records by matching phone numbers.
     */
    fun matchContactsWithConversations(conversations: List<Conversation>, contacts: List<Contact>) {
        conversations.forEach { conversation ->
            contacts.forEach { contact ->
                if (conversation.senderPhone == contact.strippedPhone) {
                    conversation.contact = contact
                }
            }
        }
    }
}