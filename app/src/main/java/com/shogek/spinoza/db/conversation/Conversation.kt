package com.shogek.spinoza.db.conversation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversation_table")
data class Conversation(

    @ColumnInfo(name = "phone")
    /** The sender's phone number. */
    val phone: String,

    @ColumnInfo(name = "snippet")
    /** The latest message in the conversation. */
    val snippet: String,

    @ColumnInfo(name = "snippet_timestamp")
    /** Timestamp when the latest snippet was sent/received. */
    val snippetTimestamp: Long,

    @ColumnInfo(name = "snippet_ours")
    /** Indicates whether the latest message was sent by us. */
    val snippetIsOurs: Boolean,

    @ColumnInfo(name = "snippet_read")
    /** Indicates whether the latest message was read by us. */
    val snippetWasRead: Boolean
) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
}
