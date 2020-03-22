package com.shogek.spinoza.db.conversation

import androidx.room.Embedded
import androidx.room.Relation
import com.shogek.spinoza.db.contact.Contact

data class ConversationAndContact(

    @Embedded
    val conversation: Conversation,

    @Relation(
        parentColumn = "contact_id",
        entityColumn = "id"
    )
    val contact: Contact?
)