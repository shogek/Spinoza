package com.shogek.spinoza.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shogek.spinoza.Extra
import com.shogek.spinoza.R
import com.shogek.spinoza.activities.MessageListActivity
import com.shogek.spinoza.models.Conversation
import com.shogek.spinoza.utils.DateUtils
import java.time.format.DateTimeFormatter
import java.time.*

class ConversationListRecyclerAdapter(
    private val context: Context,
    private val conversations: Array<Conversation>
) : RecyclerView.Adapter<ConversationListRecyclerAdapter.ViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)

    private fun getFormattedDate(date: LocalDateTime) : String {
        // TODO: [Style] Show short version of day names
        // TODO: [Style] Show month and day numbers with zeroes

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
        val parsed = date.format(formatter)
        val parsedDate = parsed.split(" ")[0].split("-")
        val year = parsedDate[0]
        val month = parsedDate[1]
        val day = parsedDate[2]

        val current = LocalDateTime.now(ZoneOffset.UTC)
        // Use 6 days instead of 7 to not show the same day.
        // Ex.: If today is MONDAY, to not show a message from previous week as also MONDAY but instead MM-dd.
        val lastWeek = current.minusDays(6)

        // If not this year
        if (current.year != date.year)
            return "${year}-${month}-${day}"
        // If not this month
        if (current.month != date.month)
            return "${month}-${day}"
        // If not this week
        if (date.isBefore(lastWeek))
            return "${month}-${day}"
        // If not today
        if (current.dayOfMonth != date.dayOfMonth)
            return date.dayOfWeek.toString()

        return "${date.hour}:${date.minute}"
    }

    /**
    *   Create the view to display an individual list item.
    */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Pass 'false' so that the RecyclerView would take care of that for us.
        val itemView = this.layoutInflater.inflate(R.layout.conversation_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation = this.conversations[position]

        holder.conversationId = conversation.threadId
        holder.sender.text = conversation.getDisplayName()

        holder.lastMessage.text =
            if (conversation.isOurs)
                "You: ${conversation.latestMessageText}"
            else
                conversation.latestMessageText

        if (!conversation.wasRead) {
            holder.lastMessage.setTypeface(holder.lastMessage.typeface, Typeface.BOLD)
            holder.lastMessage.setTextColor(Color.parseColor("#D8000000"))
            holder.notification.visibility = View.VISIBLE
        }

        val date = DateUtils.getUTCLocalDateTime(conversation.latestMessageTimestamp)
        val properDate = "\u00B7 ${getFormattedDate(date)}"
        holder.date.text = properDate

        if (conversation.contact?.photoUri != null) {
            holder.senderImage.setImageURI(Uri.parse(conversation.contact?.photoUri))
        } else {
            holder.senderImage.setImageResource(R.drawable.ic_placeholder_face_24dp)
        }
    }

    override fun getItemCount(): Int {
        return this.conversations.size
    }

    /**
    *   Bind the fields that we use in the view for an individual list item.
    *   Class is marked as 'inner' so we could the pass the constructor's context to an intent.
    */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sender: TextView = itemView.findViewById(R.id.tv_sender)
        val lastMessage: TextView = itemView.findViewById(R.id.tv_lastMessage)
        val senderImage: ImageView = itemView.findViewById(R.id.iv_sender)
        val notification: ImageView = itemView.findViewById(R.id.iv_notification)
        val date: TextView = itemView.findViewById(R.id.tv_messageDate)

        var conversationId: Number? = null

        init {
            itemView.setOnClickListener {
                val intent = Intent(context, MessageListActivity::class.java)
                intent.putExtra(Extra.GOAL, Extra.ConversationList.MessageList.OpenConversation.GOAL)
                intent.putExtra(Extra.ConversationList.MessageList.OpenConversation.CONVERSATION_ID, this.conversationId)
                context.startActivity(intent)
            }
        }
    }
}