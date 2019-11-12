package com.shogek.spinoza.repositories

import android.content.ContentResolver
import android.provider.Telephony
import android.util.Log
import com.shogek.spinoza.models.Conversation
import com.shogek.spinoza.models.Message
import com.shogek.spinoza.models.MessageType
import com.shogek.spinoza.utils.DateUtils

object SmsRepository {
    fun getConversations(resolver: ContentResolver): Array<Conversation> {
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,      /*      TEXT       ex.: +37067787874       The address of the other party.                                                 */
            Telephony.Sms.PERSON,       /*      INT        ex.: 1                  The ID of the sender of the conversation, if present.                           */
            Telephony.Sms.BODY,         /*      TEXT       ex.: How are you?       The body of the message.                                                        */
            Telephony.Sms.DATE,         /*      LONG       ex.: 1571762897000      The date the message was received.                                              */
            Telephony.Sms.READ,         /*      INT        ex.: 1                  Has the message been read?                                                      */
            Telephony.Sms.SEEN,         /*      INT        ex.: 1                  Has the message been seen? Determines whether we need to show a notification.   */
            Telephony.Sms.THREAD_ID,    /*      INT        ex.: 4                  The thread ID of the message.                                                   */
            Telephony.Sms.TYPE          /*      INT        ex.: 1                  The type of message.                                                            */
            /*
                MESSAGE_TYPE_ALL    = 0;
                MESSAGE_TYPE_INBOX  = 1;
                MESSAGE_TYPE_SENT   = 2;
                MESSAGE_TYPE_DRAFT  = 3;
                MESSAGE_TYPE_OUTBOX = 4;
                MESSAGE_TYPE_FAILED = 5; // for failed outgoing messages
                MESSAGE_TYPE_QUEUED = 6; // for messages to send later
             */
        )
        val selection = null
        val selectionArgs = null
        val sortOrder = "${Telephony.Sms.DATE} ASC"

        val cursor = resolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
            ?: return emptyArray()

        var log = "\n "
        var message: Message
        var messages = arrayOf<Message>()

        while (cursor.moveToNext()) {
            log += "\n"
            message = Message()
            for (columnName in cursor.columnNames) {
                val columnIndex = cursor.getColumnIndex(columnName)
                val columnValue = cursor.getString(columnIndex)

                mapMessageField(message, columnName, columnValue ?: "")

                log += "\n"
                log += "INDEX: $columnIndex \t"
                log += "NAME:  $columnName  \t"
                log +=
                    if (columnName == Telephony.Sms.DATE)
                        "VALUE: ${DateUtils.getDateTime(columnValue)}"
                    else
                        "VALUE: $columnValue"
            }
            messages += message
        }
        Log.w("1", log)
        cursor.close()

        val grouped = groupToConversations(messages)
        addSenders(grouped)
        addSenderImages(resolver, grouped)
        return grouped
}

    private fun mapMessageField(message: Message, column: String, value: String) {
        when (column) {
            Telephony.Sms._ID ->        message.id          = value
            Telephony.Sms.ADDRESS ->    message.sender      = value
            Telephony.Sms.PERSON ->     message.senderId    = value
            Telephony.Sms.BODY ->       message.text        = value
            Telephony.Sms.DATE ->       message.dateSent    = DateUtils.getDateTime(value)
            Telephony.Sms.READ ->       message.isRead      = value == "1"
            Telephony.Sms.SEEN ->       message.isSeen      = value == "1"
            Telephony.Sms.THREAD_ID ->  message.threadId    = value
            Telephony.Sms.TYPE -> {
                when (value.toInt()) {
                    0 -> message.type = MessageType.ALL
                    1 -> message.type = MessageType.INBOX
                    2 -> message.type = MessageType.SENT
                    3 -> message.type = MessageType.DRAFT
                    4 -> message.type = MessageType.OUTBOX
                    5 -> message.type = MessageType.FAILED
                    6 -> message.type = MessageType.QUEUED
                }
            }
        }
    }

    private fun groupToConversations(messages: Array<Message>): Array<Conversation> {
        val dictionary = hashMapOf<String, Conversation>()

        messages.forEach { message ->
            val tId = message.threadId
            if (dictionary.containsKey(tId)) {
                val conversation = dictionary[tId]!!
                conversation.messages += message
            } else {
                val conversation = Conversation()
                conversation.messages += message
                dictionary[tId] = conversation
            }
        }

        return dictionary.values.toTypedArray()
    }

    private fun addSenders(conversations: Array<Conversation>) {
        conversations.forEach { conversation -> iterateMessages(conversation) }
    }

    private fun iterateMessages(conversation: Conversation) {
        conversation.messages.forEach { message ->
            if (!message.isSentByUs()) {
                conversation.senderPhone = message.sender
                conversation.senderId = message.senderId
                return
            }
        }
    }

    private fun addSenderImages(resolver: ContentResolver, conversations: Array<Conversation>) {
        val contacts = ContactRepository.getAllContacts(resolver)

        conversations.forEach { conversation ->
            val trim = "\\s".toRegex()
            val conversationPhone = conversation.senderPhone.replace(trim, "")
            contacts.forEach { contact ->
                if (conversationPhone == contact.phone.replace(trim, "")) {
                    conversation.photo = contact.photo
                    conversation.senderName = contact.name
                }
            }
        }
    }
}