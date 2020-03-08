package com.shogek.spinoza.db.message

import androidx.room.*
import com.shogek.spinoza.db.conversation.Conversation

@Entity(
    tableName = "message_table",
    foreignKeys = [ForeignKey(
        entity = Conversation::class,
        parentColumns = ["id"],
        childColumns = ["conversation_id"],
        onUpdate = ForeignKey.NO_ACTION,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("conversation_id")]
)
data class Message(

    @ColumnInfo(name = "conversation_id")
    /** The 'Conversation' to which the SMS belongs to. */
    val conversationId: Long,

    @ColumnInfo(name = "body")
    /** The text content of the SMS message. */
    val body: String,

    @ColumnInfo(name = "timestamp")
    /** Timestamp when the SMS was sent/received. */
    val timestamp: Long,

    @ColumnInfo(name = "is_ours")
    /** Indicates whether the SMS was sent by us. */
    val isOurs: Boolean
) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
}