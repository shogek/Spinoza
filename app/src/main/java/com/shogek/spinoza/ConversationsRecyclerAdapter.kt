package com.shogek.spinoza

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDateTime

class ConversationsRecyclerAdapter(
    private val context: Context,
    private val conversations: List<DummyConversation>
) : RecyclerView.Adapter<ConversationsRecyclerAdapter.ViewHolder>() {
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
        val itemView = this.layoutInflater.inflate(R.layout.my_row_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation = this.conversations[position]

        holder.sender.text = conversation.sender
        holder.lastMessage.text =
            if (conversation.isMyMessage) "You: ${conversation.message}"
            else                          conversation.message
        if (!conversation.seen) {
            holder.lastMessage.setTypeface(holder.lastMessage.typeface, Typeface.BOLD)
            holder.lastMessage.setTextColor(Color.parseColor("#D8000000"))
        }
        holder.notification.setImageDrawable(
            if (conversation.seen) ContextCompat.getDrawable(this.context, R.drawable.ic_circle_gray_15dp)
            else                   ContextCompat.getDrawable(this.context, R.drawable.ic_circle_green_15dp)
        )
        holder.date.text = getFormattedDate(conversation.date)
        // TODO: holder.senderImage = conversation.image
    }

    override fun getItemCount(): Int {
        return this.conversations.size
    }

    /**
    *   Bind the fields that we use in the view for an individual list item.
    */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sender = itemView.findViewById<TextView>(R.id.tv_sender)
        val lastMessage = itemView.findViewById<TextView>(R.id.tv_lastMessage)
        val senderImage = itemView.findViewById<ImageView>(R.id.iv_sender)
        val notification = itemView.findViewById<ImageView>(R.id.iv_notification)
        val date = itemView.findViewById<TextView>(R.id.tv_messageDate)
    }
}