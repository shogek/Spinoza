package com.shogek.spinoza.db.message

import androidx.room.*

@Entity(tableName = "message_table")
data class Message(

    @ColumnInfo(name = "android_id")
    /**
     * The ID of the message as it is stored in the phone (Telephony.Sms._ID)
     * (indicates that the record was not created by our application, but imported from the phone)
     */
    val androidId: Long?,

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