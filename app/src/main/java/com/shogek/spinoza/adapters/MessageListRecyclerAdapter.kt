package com.shogek.spinoza.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shogek.spinoza.R
import com.shogek.spinoza.models.Message
import android.widget.RelativeLayout
import com.shogek.spinoza.utils.UnitUtils

class MessageListRecyclerAdapter(
    private val context: Context,
    private val messages: Array<Message>
): RecyclerView.Adapter<MessageListRecyclerAdapter.ViewHolder>() {
    private val MESSAGE_OURS = 0
    private val MESSAGE_THEIRS = 1
    private val layoutInflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int): Int {
        val message = this.messages[position]
        return if (message.isOurs) MESSAGE_OURS else MESSAGE_THEIRS
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = if (viewType == MESSAGE_OURS) R.layout.message_list_item_ours else R.layout.message_list_item_theirs
        val itemView = this.layoutInflater.inflate(view, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentMessage = this.messages[position]
        val viewHolder = holder.ourMessage ?: holder.theirMessage
        viewHolder.text = currentMessage.text

        // First message of the conversation
        if (position == 0) return

        // Add an extra top margin if the previous message's sender doesn't match the current one
        val previousMessage = this.messages[position - 1]
        if (previousMessage.isOurs != currentMessage.isOurs) {
            val layoutParams = viewHolder.layoutParams as (RelativeLayout.LayoutParams)
            layoutParams.setMargins(
                layoutParams.leftMargin,
                layoutParams.topMargin + UnitUtils.asPixels(8f, this.context.resources),
                layoutParams.rightMargin,
                layoutParams.bottomMargin)
            viewHolder.layoutParams = layoutParams
        }
    }

    override fun getItemCount(): Int {
        return this.messages.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val ourMessage = itemView.findViewById<TextView>(R.id.tv_ourMessageText)
        val theirMessage = itemView.findViewById<TextView>(R.id.tv_theirMessageText)
    }
}