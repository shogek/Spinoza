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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.shogek.spinoza.R
import com.shogek.spinoza.activities.MessageListActivity
import com.shogek.spinoza.models.Conversation
import java.time.LocalDateTime

class ConversationListRecyclerAdapter(
    private val context: Context,
    private val conversations: List<Conversation>
) : RecyclerView.Adapter<ConversationListRecyclerAdapter.ViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)

    private fun getFormattedDate(date: LocalDateTime) : String {
        // TODO: Show short version of day names
        // TODO: Show month and day numbers with zeroes
        val current = LocalDateTime.now()
        // If last year - show day, month and year
        if (current.year != date.year) return "${date.year}-${date.month.value}-${date.dayOfMonth}"
        // If this month - day and month
        if (current.month != date.month) return "${date.month.value}-${date.dayOfMonth}"
        // If this week - show weekday
        if (current.dayOfWeek != date.dayOfWeek) return date.dayOfWeek.toString()
        // If today - show hour and minute
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

        holder.senderId = conversation.sender

        holder.sender.text = conversation.sender
        holder.lastMessage.text =
            if (conversation.isMyMessage)
                "You: ${conversation.message}"
            else
                conversation.message

        if (!conversation.seen) {
            holder.lastMessage.setTypeface(holder.lastMessage.typeface, Typeface.BOLD)
            holder.lastMessage.setTextColor(Color.parseColor("#D8000000"))
        }

        val bubble =
            if (conversation.seen)
                R.drawable.ic_notification_bubble_inactive_15dp
            else
                R.drawable.ic_notification_bubble_active_15dp
        holder.notification.setImageDrawable(ContextCompat.getDrawable(this.context, bubble))

        holder.date.text = getFormattedDate(conversation.date)
        // TODO: holder.senderImage = conversation.image
        holder.senderImage.setImageURI(Uri.parse("content://com.android.contacts/contacts/1/photo"))
//        if (conversation.senderImage != null) {
//            holder.senderImage.setImageURI(conversation.senderImage)
//        }
    }

    override fun getItemCount(): Int {
        return this.conversations.size
    }

    /**
    *   Bind the fields that we use in the view for an individual list item.
    *   Class is marked as 'inner' so we could the pass the constructor's context to an intent.
    */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sender = itemView.findViewById<TextView>(R.id.tv_sender)
        val lastMessage = itemView.findViewById<TextView>(R.id.tv_lastMessage)
        val senderImage = itemView.findViewById<ImageView>(R.id.iv_sender)
        val notification = itemView.findViewById<ImageView>(R.id.iv_notification)
        val date = itemView.findViewById<TextView>(R.id.tv_messageDate)

        var senderId: String? = null

        /**
         *  When a row is clicked - open the corresponding conversation.
         */
        init {
            itemView.setOnClickListener {
                val intent = Intent(context, MessageListActivity::class.java)
                intent.putExtra("SENDER_NAME", this.sender.text.toString())
                context.startActivity(intent)
            }
        }
    }
}