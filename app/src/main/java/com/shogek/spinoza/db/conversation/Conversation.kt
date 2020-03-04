package com.shogek.spinoza.db.conversation

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shogek.spinoza.db.contact.Contact

@Entity(tableName = "conversation_table")
data class Conversation(

    @Embedded
    /** The 'Contact' with which the conversation is happening. */
    var contact: Contact?,

    @ColumnInfo(name = "conversation_phone")
    /** The sender's phone number. */
    val phone: String,

    @ColumnInfo(name = "conversation_snippet")
    /** The latest message in the conversation. */
    var snippet: String,

    @ColumnInfo(name = "conversation_snippet_timestamp")
    /** Timestamp when the latest snippet was sent/received. */
    var snippetTimestamp: Long,

    @ColumnInfo(name = "conversation_snippet_is_ours")
    /** Indicates whether the latest message was sent by us. */
    var snippetIsOurs: Boolean,

    @ColumnInfo(name = "conversation_snippet_was_read")
    /** Indicates whether the latest message was read by us. */
    var snippetWasRead: Boolean
) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "conversation_conversation_id")
    var conversationId: Long = 0
}
