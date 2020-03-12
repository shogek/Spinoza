package com.shogek.spinoza.db.conversation

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import androidx.room.Entity
import androidx.room.Ignore
import com.shogek.spinoza.db.contact.Contact
import com.shogek.spinoza.db.message.Message

@Entity(tableName = "conversation_table")
class Conversation(

    @ColumnInfo(name = "android_id")
    /**
     * The ID of the conversation as it is stored in the phone (Telephony.Sms.Conversations.THREAD_ID)
     * (indicates that the record was not created by our application, but imported from the phone)
     */
    val androidId: Long?,

    @ColumnInfo(name = "contact_id")
    /** The contact with which the conversation is happening. */
    var contactId: Long?,

    @ColumnInfo(name = "phone")
    /** The sender's phone number. */
    val phone: String,

    @ColumnInfo(name = "snippet")
    /** The latest message in the conversation. */
    var snippet: String,

    @ColumnInfo(name = "snippet_timestamp")
    /** Timestamp when the latest snippet was sent/received. */
    var snippetTimestamp: Long,

    @ColumnInfo(name = "snippet_is_ours")
    /** Indicates whether the latest message was sent by us. */
    var snippetIsOurs: Boolean,

    @ColumnInfo(name = "snippet_was_read")
    /** Indicates whether the latest message was read by us. */
    var snippetWasRead: Boolean
) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0

    @Ignore
    var contact: Contact? = null

    @Ignore
    var messages: List<Message>? = null
}
