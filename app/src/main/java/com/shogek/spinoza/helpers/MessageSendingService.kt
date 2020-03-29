package com.shogek.spinoza.helpers

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsManager
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationRepository
import com.shogek.spinoza.db.message.Message
import com.shogek.spinoza.db.message.MessageRepository
import com.shogek.spinoza.db.message.MessageType
import com.shogek.spinoza.db.message.MessageType.Companion.toInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random


/** Use this class to send SMS messages. */
class MessageSendingService(
    private val context: Context,
    private val scope: CoroutineScope
) {

    private var wasInit = false
    private lateinit var messageRepository: MessageRepository
    private lateinit var conversationRepository: ConversationRepository
    /** Used to differentiate between messages being sent.
     * KEY: Unique integer associated with a pending message.
     * VALUE: The pending message itself. */
    private val pendingMessageTable = mutableMapOf<Int, PendingMessage>()

    private object CONSTANTS {
        const val INTENT = "SMS_SENT"
        const val REQUEST_CODE = "REQUEST_CODE"
    }


    private fun init() {
        context.registerReceiver(this.messageReceiver, IntentFilter(CONSTANTS.INTENT))
        this.conversationRepository = ConversationRepository(context, scope)
        this.messageRepository = MessageRepository(context, scope)
        this.wasInit = true
    }

    protected fun finalize() {
        context.unregisterReceiver(this.messageReceiver)
    }

    /** Called when the OS sends an SMS. */
    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context?, arg1: Intent?) {
            if (arg1 == null || arg1.action != CONSTANTS.INTENT) {
                return
            }

            val code = arg1.extras!!.getInt(CONSTANTS.REQUEST_CODE)
            val pendingMessage = pendingMessageTable.remove(code)!!

            if (resultCode == Activity.RESULT_OK) {
                onMessageSendSuccess(pendingMessage)
            } else {
                onMessageSendFail(pendingMessage)
            }
        }
    }

    private fun onMessageSendSuccess(pendingMessage: PendingMessage) = scope.launch {
        updateConversation(pendingMessage)

        pendingMessage.message.type = MessageType.SENT.toInt()
        messageRepository.update(pendingMessage.message)

        pendingMessage.onSuccess(pendingMessage.message)
    }

    private fun onMessageSendFail(pendingMessage: PendingMessage) = scope.launch {
        updateConversation(pendingMessage)

        pendingMessage.message.type = MessageType.FAILED_TO_SEND.toInt()
        messageRepository.update(pendingMessage.message)

        pendingMessage.onError()
    }

    /** Update conversation to reflect the latest SMS sent. */
    private fun updateConversation(pendingMessage: PendingMessage) = scope.launch {
        val conversation = conversationRepository.get(pendingMessage.conversation.id)

        conversation.snippet = pendingMessage.message.body
        conversation.snippetTimestamp = pendingMessage.message.timestamp
        conversation.snippetWasRead = true
        conversation.snippetIsOurs = true

        conversationRepository.update(conversation)
    }

    fun sendMessage(
        conversation: Conversation,
        messageBody: String,
        onSuccess: (Message) -> Unit,
        onError: () -> Unit
    ) = scope.launch {
        if (!wasInit) {
            init()
        }

        val messageToSend = Message(null, conversation.id, messageBody, System.currentTimeMillis(), true, MessageType.SENDING.toInt())
        messageToSend.id = messageRepository.insert(messageToSend)

        val code = Random.nextInt() // used to differentiate between other SMS being sent at the moment
        val pendingMessage = PendingMessage(conversation, messageToSend, onSuccess, onError)
        pendingMessageTable[code] = pendingMessage

        val intent = Intent(CONSTANTS.INTENT)
        intent.putExtra(CONSTANTS.REQUEST_CODE, code)

        val pendingIntent = PendingIntent.getBroadcast(context, code, intent, 0)
        SmsManager
            .getDefault()
            .sendTextMessage(conversation.phone, null, messageBody, pendingIntent, null)
    }


    private data class PendingMessage(
        val conversation: Conversation,
        val message: Message,
        val onSuccess: (Message) -> Unit,
        val onError: () -> Unit
    )
}