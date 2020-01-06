package com.shogek.spinoza.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shogek.spinoza.Extra
import com.shogek.spinoza.R
import com.shogek.spinoza.activities.MessageListActivity
import com.shogek.spinoza.models.Conversation
import com.shogek.spinoza.utils.DateUtils
import java.lang.IllegalArgumentException
import java.time.format.DateTimeFormatter
import java.time.*
import java.time.format.TextStyle
import java.util.*

class ConversationListRecyclerAdapter(
    private val context: Context,
    private val conversations: Array<Conversation>
) : RecyclerView.Adapter<ConversationListRecyclerAdapter.BaseViewHolder>() {

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(conversation: Conversation)
    }

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_CONVERSATION_READ = 1
        const val TYPE_CONVERSATION_UNREAD = 2
    }

    private val layoutInflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int) : Int {
        // TODO: [Bug] First conversation item is hidden because of header
        if (position == 0)
            return TYPE_HEADER

        val conversation = this.conversations[position]
        return if  (conversation.wasRead)
            TYPE_CONVERSATION_READ
        else
            TYPE_CONVERSATION_UNREAD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val itemView = this.layoutInflater.inflate(R.layout.conversation_list_header, parent, false)
                HeaderViewHolder(itemView)
            }
            TYPE_CONVERSATION_READ -> {
                val itemView = this.layoutInflater.inflate(R.layout.conversation_list_item_read, parent, false)
                ReadConversationViewHolder(itemView)
            }
            TYPE_CONVERSATION_UNREAD -> {
                val itemView = this.layoutInflater.inflate(R.layout.conversation_list_item_unread, parent, false)
                UnreadConversationViewHolder(itemView)
            }
            else -> throw IllegalArgumentException("Unknown ViewHolder type!")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val conversation = this.conversations[position]

        when (holder) {
            is ReadConversationViewHolder   -> holder.bind(conversation)
            is UnreadConversationViewHolder -> holder.bind(conversation)
            is HeaderViewHolder             -> holder.bind(conversation)
            else -> throw IllegalArgumentException("Unknown ViewHolder type!")
        }
    }

    override fun getItemCount(): Int {
        return this.conversations.size
    }

    private fun getFormattedDate(date: LocalDateTime) : String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm")
        val parsed = date.format(formatter)
        val parsedDateAndTime = parsed.split(" ")

        val parsedDate = parsedDateAndTime[0].split("-")
        val year = parsedDate[0]
        val month = parsedDate[1]
        val day = parsedDate[2]

        val parsedTime = parsedDateAndTime[1].split(":")
        val hour = parsedTime[0]
        val minute = parsedTime[1]

        val current = LocalDateTime.now(ZoneOffset.UTC)
        // Use 6 days instead of 7 to not show the same day.
        // Ex.: If today is MONDAY, to not show a message from previous week as also MONDAY but instead MM-dd.
        val lastWeek = current.minusDays(6)

        // If not this year
        if (current.year != date.year)
            return "${year}-${month}-${day}"

        // If not this month
        if (current.month != date.month)
            return "$day ${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}"

        // If not this week (used to avoid confusion - check definition of 'lastWeek')
        if (date.isBefore(lastWeek))
            return "$day ${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}"

        // If not today
        if (current.dayOfMonth != date.dayOfMonth)
            return date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

        return "$hour:$minute"
    }

    private fun openConversation(conversationId: Number) {
        val intent = Intent(context, MessageListActivity::class.java)
        intent.putExtra(Extra.GOAL, Extra.ConversationList.MessageList.OpenConversation.GOAL)
        intent.putExtra(Extra.ConversationList.MessageList.OpenConversation.CONVERSATION_ID, conversationId)
        context.startActivity(intent)
    }

    inner class ReadConversationViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val sender: TextView = itemView.findViewById(R.id.tv_sender)
        private val lastMessage: TextView = itemView.findViewById(R.id.tv_lastMessage)
        private val senderImage: ImageView = itemView.findViewById(R.id.iv_sender)
        private val date: TextView = itemView.findViewById(R.id.tv_messageDate)
        private lateinit var conversationId: Number

        init {
            itemView.setOnClickListener { openConversation(this.conversationId) }
        }

        override fun bind(conversation: Conversation) {
            conversationId = conversation.threadId
            sender.text = conversation.getDisplayName()

            this.lastMessage.text =
                if (conversation.latestMessageIsOurs)
                    "You: ${conversation.latestMessageText}"
                else
                    conversation.latestMessageText

            val date = DateUtils.getUTCLocalDateTime(conversation.latestMessageTimestamp)
            val properDate = "\u00B7 ${getFormattedDate(date)}"
            this.date.text = properDate

            if (conversation.contact?.photoUri != null) {
                this.senderImage.setImageURI(Uri.parse(conversation.contact?.photoUri))
            } else {
                this.senderImage.setImageResource(R.drawable.unknown_contact)
            }
        }
    }

    // TODO: [Refactor] Find a way to easily reuse the logic
    inner class UnreadConversationViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val sender: TextView = itemView.findViewById(R.id.tv_sender)
        private val lastMessage: TextView = itemView.findViewById(R.id.tv_lastMessage)
        private val senderImage: ImageView = itemView.findViewById(R.id.iv_sender)
        private val date: TextView = itemView.findViewById(R.id.tv_messageDate)
        private lateinit var conversationId: Number

        init {
            itemView.setOnClickListener { openConversation(this.conversationId) }
        }

        override fun bind(conversation: Conversation) {
            conversationId = conversation.threadId
            sender.text = conversation.getDisplayName()

            this.lastMessage.text =
                if (conversation.latestMessageIsOurs)
                    "You: ${conversation.latestMessageText}"
                else
                    conversation.latestMessageText

            val date = DateUtils.getUTCLocalDateTime(conversation.latestMessageTimestamp)
            val properDate = "\u00B7 ${getFormattedDate(date)}"
            this.date.text = properDate

            if (conversation.contact?.photoUri != null) {
                this.senderImage.setImageURI(Uri.parse(conversation.contact?.photoUri))
            } else {
                this.senderImage.setImageResource(R.drawable.unknown_contact)
            }
        }
    }

    // TODO: [Task] Enable filtering of conversations
    inner class HeaderViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val search: EditText = itemView.findViewById(R.id.et_conversationSearch)

        override fun bind(conversation: Conversation) { }
    }
}