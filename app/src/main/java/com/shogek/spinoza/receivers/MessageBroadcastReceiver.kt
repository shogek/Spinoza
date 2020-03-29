package com.shogek.spinoza.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.PhoneNumberUtils
import android.telephony.SmsMessage
import android.util.Log
import com.shogek.spinoza.db.contact.Contact
import com.shogek.spinoza.db.contact.ContactRepository
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationRepository
import com.shogek.spinoza.db.message.Message
import com.shogek.spinoza.db.message.MessageRepository
import com.shogek.spinoza.db.message.MessageType
import com.shogek.spinoza.db.message.MessageType.Companion.toInt
import com.shogek.spinoza.helpers.MessageNotificationHelper
import com.shogek.spinoza.ui.state.CommonState
import kotlinx.coroutines.*

class MessageBroadcastReceiver : BroadcastReceiver() {

    private companion object {
        val LOG = MessageBroadcastReceiver::class.java.simpleName

        /** Extract received SMS body and sender's phone number. */
        fun parseReceivedMessage(intent: Intent): BasicMessage? {
            val timestamp = System.currentTimeMillis() // when was message received

            val pdus = (intent.extras?.get("pdus") as Array<*>).filterIsInstance<ByteArray>()
            val format = intent.extras?.get("format") as String?
            if (pdus.isEmpty() || format == null) {
                Log.e(LOG, "Failed to parse received SMS message")
                return null
            }

            val messageText = pdus.fold("") { acc, bytes -> acc + SmsMessage.createFromPdu(bytes, format).displayMessageBody }
            val senderPhone = SmsMessage.createFromPdu(pdus.first(), format).originatingAddress
            if (senderPhone == null) {
                Log.e(LOG, "Failed to parse received SMS message's sender phone number")
                return null
            }

            return BasicMessage(senderPhone, messageText, timestamp)
        }
    }

    /** Called when a new SMS is received. */
    override fun onReceive(context: Context?, intent: Intent?) = runBlocking {
        if (context == null || intent == null) {
            Log.e(LOG, "'context' or 'intent' is null")
            return@runBlocking
        }

        val basicMessage = parseReceivedMessage(intent)
            ?: return@runBlocking

        val contactRepository = ContactRepository(context, this)
        val messageRepository = MessageRepository(context, this)
        val conversationRepository = ConversationRepository(context, this)

        var conversation: Conversation?
        conversation = conversationRepository.getByPhone(basicMessage.senderPhone)
        // TODO: [Refactor]
        if (conversation != null) {
            MessageNotificationHelper.notify(context, conversation, basicMessage.messageText)
            updateExistingConversation(conversationRepository, messageRepository, conversation, basicMessage)
            return@runBlocking
        }

        conversation = getByContact(conversationRepository, contactRepository, basicMessage.senderPhone)
        if (conversation != null) {
            MessageNotificationHelper.notify(context, conversation, basicMessage.messageText)
            /* We change the phone to the one we received by SMS because this way we get the unformatted variant.
            * Whereas when a contact is created in android it adds all the '(', ')' and '+' and space symbols.
            * Reason: faster second lookup times. */
            conversation.phone = basicMessage.senderPhone
            updateExistingConversation(conversationRepository, messageRepository, conversation, basicMessage)
            return@runBlocking
        }

        val created = createNewConversation(conversationRepository, messageRepository, basicMessage)
        MessageNotificationHelper.notify(context, created, basicMessage.messageText)
    }

    /** Search by comparing the formatted phone numbers of existing contacts. */
    private suspend fun getByContact(
        conversationRepository: ConversationRepository,
        contactRepository: ContactRepository,
        senderPhone: String
    ): Conversation? {
        var foundContact: Contact? = null

        val contacts = contactRepository.getAll()
        for (contact in contacts) {
            if (PhoneNumberUtils.compare(contact.phone, senderPhone)) {
                foundContact = contact
                break
            }
        }

        return if (foundContact == null) null
               else conversationRepository.getByContactId(foundContact.id)
    }

    private suspend fun createNewConversation(
        conversationRepository: ConversationRepository,
        messageRepository: MessageRepository,
        basicMessage: BasicMessage
    ): Conversation {
        // A conversation doesn't exist with the person
        val newConversation = Conversation(
            null, // conversation not imported from phone
            null, // contact will be assigned (if found) when inserting to DB
            basicMessage.senderPhone,
            basicMessage.messageText,
            basicMessage.timestamp,
            snippetIsOurs = false,
            snippetWasRead = false
        )
        val conversationId = conversationRepository.insertAll(listOf(newConversation)).first()
        newConversation.id = conversationId
        val message = Message(
            null, // message not imported from phone
            conversationId,
            basicMessage.messageText,
            basicMessage.timestamp,
            isOurs = false,
            type = MessageType.SENT.toInt()
        )

        messageRepository.insert(message)
        return newConversation
    }

    private suspend fun updateExistingConversation(
        conversationRepository: ConversationRepository,
        messageRepository: MessageRepository,
        existingConversation: Conversation,
        basicMessage: BasicMessage
    ) {
        var snippetWasRead = false

        val currentOpenConversationId = CommonState.getCurrentOpenConversationId()
        if (currentOpenConversationId != null && existingConversation.id == currentOpenConversationId) {
            snippetWasRead = true
        }

        existingConversation.snippetIsOurs = false
        existingConversation.snippetWasRead = snippetWasRead
        existingConversation.snippet = basicMessage.messageText
        existingConversation.snippetTimestamp = basicMessage.timestamp
        conversationRepository.update(existingConversation)

        val message = Message(
            null, // message not imported from phone
            existingConversation.id,
            basicMessage.messageText,
            basicMessage.timestamp,
            isOurs = false,
            type = MessageType.SENT.toInt()
        )

        messageRepository.insert(message)
    }
}

data class BasicMessage(
    val senderPhone: String,
    val messageText: String,
    val timestamp: Long
)
