package com.shogek.spinoza.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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

    private val filteredConversations: MutableList<Conversation> = mutableListOf<Conversation>().apply { addAll(conversations) }

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(conversation: Conversation?)
    }

    companion object {
        const val TYPE_HEADER               = R.layout.conversation_list_item_header
        const val TYPE_CONVERSATION_READ    = R.layout.conversation_list_item_read
        const val TYPE_CONVERSATION_UNREAD  = R.layout.conversation_list_item_unread
    }

    private val layoutInflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int) : Int {
        if (position == 0)
            return TYPE_HEADER

        val conversation = this.conversations[position - 1] // -1 for header
        return if  (conversation.wasRead)
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
            is ConversationViewHolder   -> {
                val conversation = this.filteredConversations[position - 1] // -1 for header
                holder.bind(conversation)
            }
            is HeaderViewHolder         -> holder.bind(null)
            else -> throw IllegalArgumentException("Unknown ViewHolder type!")
        }
    }

    override fun getItemCount(): Int {
        return this.filteredConversations.size + 1 // +1 for header
    }

    private fun filter(phrase: String) {
        this.filteredConversations.clear()

        if (phrase.isEmpty()) {
            this.filteredConversations.addAll(this.conversations)
        } else {
            val lowerCasePhrase = phrase.toLowerCase()
            val filtered = this.conversations.filter { c -> c.getDisplayName().toLowerCase().contains(lowerCasePhrase) }
            this.filteredConversations.addAll(filtered)
        }

        notifyDataSetChanged()
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

    inner class ConversationViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val sender: TextView = itemView.findViewById(R.id.tv_sender)
        private val lastMessage: TextView = itemView.findViewById(R.id.tv_lastMessage)
        private val senderImage: ImageView = itemView.findViewById(R.id.iv_sender)
        private val date: TextView = itemView.findViewById(R.id.tv_messageDate)
        private lateinit var conversationId: Number

        init {
            itemView.setOnClickListener {
                val intent = Intent(context, MessageListActivity::class.java)
                intent.putExtra(Extra.GOAL, Extra.ConversationList.MessageList.OpenConversation.GOAL)
                intent.putExtra(Extra.ConversationList.MessageList.OpenConversation.CONVERSATION_ID, this.conversationId)
                context.startActivity(intent)
                // TODO: [Bug] Find a simple way to clear search after exiting activity
            }

            itemView.setOnLongClickListener {
                val view = layoutInflater.inflate(R.layout.conversation_list_popup_menu, null)
                val width = LinearLayout.LayoutParams.MATCH_PARENT
                val height = LinearLayout.LayoutParams.MATCH_PARENT
                val focusable = true // lets taps outside the popup to dismiss it
                val popupWindow = PopupWindow(view, width, height, focusable)
                popupWindow.showAtLocation(itemView, Gravity.CENTER, 0, 0)

                // TODO: [Feature] Implement archive conversation functionality
                val buttonArchive = view.findViewById<TextView>(R.id.tv_archiveConversation)
                buttonArchive.setOnClickListener { Toast.makeText(context, "Conversation archived", Toast.LENGTH_SHORT).show(); popupWindow.dismiss() }

                // TODO: [Feature] Implement delete conversation functionality
                val buttonDelete = view.findViewById<TextView>(R.id.tv_deleteConversation)
                buttonDelete.setOnClickListener { Toast.makeText(context, "Conversation deleted", Toast.LENGTH_SHORT).show(); popupWindow.dismiss() }

                // TODO: [Feature] Implement mute conversation functionality
                val buttonMute = view.findViewById<TextView>(R.id.tv_muteConversation)
                buttonMute.setOnClickListener { Toast.makeText(context, "Conversation muted", Toast.LENGTH_SHORT).show(); popupWindow.dismiss() }

                // TODO: [Feature] Implement mark conversation as unread functionality
                val buttonUnread = view.findViewById<TextView>(R.id.tv_markConversationAsUnread)
                buttonUnread.setOnClickListener { Toast.makeText(context, "Conversation marked as unread", Toast.LENGTH_SHORT).show(); popupWindow.dismiss() }

                // TODO: [Feature] Implement ignore conversation functionality
                val buttonIgnore = view.findViewById<TextView>(R.id.tv_ignoreConversationMessages)
                buttonIgnore.setOnClickListener { Toast.makeText(context, "Conversation ignored", Toast.LENGTH_SHORT).show(); popupWindow.dismiss() }

                // TODO: [Feature] Implement block conversation functionality
                val buttonBlock = view.findViewById<TextView>(R.id.tv_blockConversationMessages)
                buttonBlock.setOnClickListener { Toast.makeText(context, "Conversation blocked", Toast.LENGTH_SHORT).show(); popupWindow.dismiss() }

                view.setOnTouchListener { _, _ ->
                    popupWindow.dismiss()
                    true
                }
                true
            }
        }

        override fun bind(conversation: Conversation?) {
            conversationId = conversation!!.threadId
            sender.text = conversation.getDisplayName()

            this.lastMessage.text =
                if (conversation.latestMessageIsOurs)
                    "You: ${conversation.latestMessageText}"
                else
                    conversation.latestMessageText

            val date = DateUtils.getUTCLocalDateTime(conversation.latestMessageTimestamp)
            val properDate = "\u00B7 ${getFormattedDate(date)}"
            this.date.text = properDate

            Glide
                .with(itemView)
                .load(Uri.parse(conversation.contact?.photoUri ?: ""))
                .apply(RequestOptions().placeholder(R.drawable.unknown_contact))
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