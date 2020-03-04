package com.shogek.spinoza.ui.conversation.list

import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.bumptech.glide.Glide
import com.shogek.spinoza.Extra
import com.shogek.spinoza.R
import com.shogek.spinoza.activities.MessageListActivity
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.utils.DateUtils
import java.lang.IllegalArgumentException
import java.security.InvalidParameterException
import java.time.format.DateTimeFormatter
import java.time.*
import java.time.format.TextStyle
import java.util.*

class ConversationListAdapter(
    private val context: Context,
    private val viewModel: ConversationListViewModel
) : RecyclerView.Adapter<ConversationListAdapter.BaseViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    private var originalConversations = listOf<Conversation>()
    private var filteredConversations: MutableList<Conversation> = mutableListOf<Conversation>().apply { addAll(originalConversations) }

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(conversation: Conversation?)
    }

    private companion object {
        const val TYPE_HEADER               = R.layout.conversation_list_item_header
        const val TYPE_CONVERSATION_READ    = R.layout.conversation_list_item_read
        const val TYPE_CONVERSATION_UNREAD  = R.layout.conversation_list_item_unread

        fun getFormattedDate(date: LocalDateTime) : String {
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
    }

    override fun getItemViewType(position: Int) : Int {
        if (position == 0)
            return TYPE_HEADER

        val conversation = this.originalConversations[position - 1] // -1 for header
        return if  (conversation.snippetIsOurs)
            TYPE_CONVERSATION_READ
        else
            TYPE_CONVERSATION_UNREAD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return if (viewType == TYPE_HEADER) {
            val itemView = this.layoutInflater.inflate(viewType, parent, false)
            HeaderViewHolder(itemView)
        } else {
            val itemView = this.layoutInflater.inflate(viewType, parent, false)
            ConversationViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is ConversationViewHolder -> {
                val conversation = this.filteredConversations[position - 1] // -1 for header
                holder.bind(conversation)
            }
            is HeaderViewHolder -> holder.bind(null)
            else -> throw IllegalArgumentException("Unknown ViewHolder type!")
        }
    }

    override fun getItemCount(): Int {
        return this.filteredConversations.size + 1 // +1 for header
    }

    private fun filter(phrase: String) {
        this.filteredConversations.clear()

        if (phrase.isEmpty()) {
            this.filteredConversations.addAll(this.originalConversations)
        } else {
            val lowerCasePhrase = phrase.toLowerCase()
            val filtered = this.originalConversations.filter { c -> c.phone.toLowerCase().contains(lowerCasePhrase) }
            this.filteredConversations.addAll(filtered)
        }

        notifyDataSetChanged()
    }

    fun setConversations(conversations: List<Conversation>) {
        this.originalConversations = conversations
        this.filteredConversations = conversations.toMutableList()
        notifyDataSetChanged()
    }

    fun openConversationOptionsDialog(conversationId: Long) {
        MaterialDialog(context).show {
            title(R.string.conversation_list_item_options_title)

            val textArchive = context.getString(R.string.conversation_list_item_option_archive)
            val textDelete  = context.getString(R.string.conversation_list_item_option_delete)
            val textMute    = context.getString(R.string.conversation_list_item_option_mute)
            val textUnread  = context.getString(R.string.conversation_list_item_option_unread)
            val textIgnore  = context.getString(R.string.conversation_list_item_option_ignore)
            val textBlock   = context.getString(R.string.conversation_list_item_option_block)

            listItems(items = listOf(textArchive, textDelete, textMute, textUnread, textIgnore, textBlock)) { _, _, text ->
                when (text) {
                    textArchive -> viewModel.archiveConversation(conversationId)
                    textDelete  -> viewModel.deleteConversation(conversationId)
                    textMute    -> viewModel.muteConversation(conversationId)
                    textUnread  -> viewModel.markAsUnreadConversation(conversationId)
                    textIgnore  -> viewModel.ignoreConversation(conversationId)
                    textBlock   -> viewModel.blockConversation(conversationId)
                    else -> throw InvalidParameterException("Unknown conversation action!")
                }
            }
        }
    }

    inner class ConversationViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val sender: TextView = itemView.findViewById(R.id.tv_sender)
        private val lastMessage: TextView = itemView.findViewById(R.id.tv_lastMessage)
        private val senderImage: ImageView = itemView.findViewById(R.id.iv_sender)
        private val date: TextView = itemView.findViewById(R.id.tv_messageDate)
        private var conversationId: Long = -1

        init {
            itemView.setOnClickListener {
                val intent = Intent(context, MessageListActivity::class.java)
                intent.putExtra(Extra.GOAL, Extra.ConversationList.MessageList.OpenConversation.GOAL)
                intent.putExtra(Extra.ConversationList.MessageList.OpenConversation.CONVERSATION_ID, this.conversationId)
                context.startActivity(intent)
                // TODO: [Bug] Find a simple way to clear search after exiting activity
            }

            itemView.setOnLongClickListener { openConversationOptionsDialog(conversationId); true }
        }

        override fun bind(conversation: Conversation?) {
            // TODO: Why can 'conversation' be null?
            conversationId = conversation!!.id
            sender.text = conversation.phone

            this.lastMessage.text =
                if (conversation.snippetIsOurs)
                    "You: ${conversation.snippetIsOurs}"
                else
                    conversation.snippet

            val date = DateUtils.getUTCLocalDateTime(conversation.snippetTimestamp)
            val properDate = "\u00B7 ${getFormattedDate(date)}"
            this.date.text = properDate

            Glide.with(itemView)
                .load(R.drawable.unknown_contact)
                .into(this.senderImage)
        }
    }

    inner class HeaderViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val search: EditText = itemView.findViewById(R.id.et_conversationSearch)

        init {
            this.search.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = filter(s.toString())
            })
        }

        override fun bind(conversation: Conversation?) { }
    }
}