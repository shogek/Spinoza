package com.shogek.spinoza.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.shogek.spinoza.R
import com.shogek.spinoza.cores.MessageListCore
import com.shogek.spinoza.models.Message

class MessageListRecyclerAdapter(
    private val context: Context,
    private val core: MessageListCore,
    private val messages: MutableList<Message>,
    private val senderPhotoUri: String?
): RecyclerView.Adapter<MessageListRecyclerAdapter.ViewHolder>() {

    companion object {
        // Used to differentiate view holders
        const val MESSAGE_OURS = 0
        const val MESSAGE_THEIRS = 1
    }

    private val layoutInflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int): Int {
        val message = this.messages[position]
        return if (message.isOurs) MESSAGE_OURS else MESSAGE_THEIRS
    }

    override fun getItemCount(): Int = this.messages.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = if (viewType == MESSAGE_OURS)
            R.layout.message_list_item_ours
        else
            R.layout.message_list_item_theirs

        val itemView = this.layoutInflater.inflate(view, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentMessage = this.messages[position]
        holder.message = currentMessage

        val viewHolder = holder.ourMessage ?: holder.theirMessage
        viewHolder.text = currentMessage.text

        // TODO: [Bug] RecyclerView reuses ViewHolders that have the sender's image hidden.
        // That means that in places, where the image should be visible, it isn't, because the logic is not reevaluated
        if (!currentMessage.isOurs && this.shouldHideSenderImage(position)) {
            holder.senderPhoto.visibility = View.INVISIBLE
        } else if (holder.senderPhoto != null) {
            if (this.senderPhotoUri != null) {
                holder.senderPhoto.setImageURI(Uri.parse(this.senderPhotoUri))
            } else {
                holder.senderPhoto.setImageResource(R.drawable.unknown_contact)
            }
        }

        // First message of the conversation
        if (position == 0) {
            return
        } else {
            // TODO: [Style] First message should have top margin
            // Add an extra top margin if the previous message's sender doesn't match the current one
//            val previousMessage = this.messages[position - 1]
//            if (previousMessage.isOurs != currentMessage.isOurs) {
//                val layoutParams = viewHolder.layoutParams as (RelativeLayout.LayoutParams)
//                layoutParams.setMargins(
//                    layoutParams.leftMargin,
//                    layoutParams.topMargin + UnitUtils.asPixels(8f, this.context.resources),
//                    layoutParams.rightMargin,
//                    layoutParams.bottomMargin)
//                viewHolder.layoutParams = layoutParams
//            }
        }
    }

    private fun shouldHideSenderImage(position: Int) : Boolean {
        // Last message in conversation
        if (this.messages.size - 1 == position) {
            return false
        }

        val nextMessage = this.messages[position + 1]
        if (nextMessage.isOurs) {
            return false
        }

        return true
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        lateinit var message: Message

        val ourMessage = itemView.findViewById<TextView>(R.id.tv_ourMessageText)
        val theirMessage = itemView.findViewById<TextView>(R.id.tv_theirMessageText)
        val senderPhoto = itemView.findViewById<ImageView>(R.id.message_list_sender_photo_civ)

        init {
            itemView.setOnLongClickListener {
                core.onLongClickMessage(this.message)
                true
            }

            itemView.setOnClickListener {
                core.onClickMessage()
            }
        }
    }
}