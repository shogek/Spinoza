package com.shogek.spinoza.db.message

import androidx.room.*
import com.shogek.spinoza.db.conversation.Conversation

@Entity(
    tableName = "message_table",
    foreignKeys = [ForeignKey(
        entity = Conversation::class,
        parentColumns = ["conversation_conversation_id"],
        childColumns = ["message_conversation_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Message(

    @ColumnInfo(name = "message_conversation_id")
    /** The 'Conversation' to which the SMS belongs to. */
    val conversationId: Long,

    @ColumnInfo(name = "message_body")
    /** The text content of the SMS message. */
    val body: String,

    @ColumnInfo(name = "message_timestamp")
    /** Timestamp when the SMS was sent/received. */
    val timestamp: Long,

    @ColumnInfo(name = "message_is_ours")
    /** Indicates whether the SMS was sent by us. */
    val isOurs: Boolean
) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "message_message_id")
    var messageId: Long = 0
}