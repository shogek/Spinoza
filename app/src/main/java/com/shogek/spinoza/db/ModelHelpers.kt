package com.shogek.spinoza.db

import android.telephony.PhoneNumberUtils
import com.shogek.spinoza.db.contact.Contact
import com.shogek.spinoza.db.conversation.Conversation

object ModelHelpers {

    /**
     * Assign a contact to a conversation if their phone numbers match.
     * @param onlyMatches true: return only matches (contacless conversations discarded), else: return same list
     */
    fun matchByPhone(
        conversations: List<Conversation>,
        contacts: List<Contact>,
        onlyMatches: Boolean
    ): List<Conversation> {
        val result = mutableListOf<Conversation>()

        for (conversation in conversations) {
            if (conversation.contactId != null) {
                continue
            }

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