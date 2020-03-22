package com.shogek.spinoza.db.conversation

import androidx.room.Embedded
import androidx.room.Relation
import com.shogek.spinoza.db.contact.Contact
import com.shogek.spinoza.db.message.Message

data class ConversationAndContactAndMessages (

    @Embedded
    val conversation: Conversation,

    @Relation(
        parentColumn = "contact_id",
        entityColumn = "id"
    )
    val contact: Contact?,

    @Relation(
        parentColumn = "id",
        entityColumn = "conversation_id"
    )
    val messages: List<Message>
)