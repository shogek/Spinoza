package com.shogek.spinoza.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.shogek.spinoza.R
import com.shogek.spinoza.cores.MessageListCore
import com.shogek.spinoza.models.Message
import java.lang.IllegalArgumentException

class MessageListRecyclerAdapter(
    private val context: Context,
    private val core: MessageListCore,
    private val messages: MutableList<Message>,
    private val senderPhotoUri: String?
): RecyclerView.Adapter<MessageListRecyclerAdapter.BaseViewHolder>() {

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(message: Message)
    }

    companion object {
        const val TYPE_MESSAGE_OUR = R.layout.message_list_item_ours
        const val TYPE_MESSAGE_THEIRS = R.layout.message_list_item_theirs
        const val TYPE_MESSAGE_THEIRS_NO_IMAGE = R.layout.message_list_item_theirs_no_image
    }

    private val layoutInflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int): Int {
        val message = this.messages[position]
        if (message.isOurs)
            return TYPE_MESSAGE_OUR

        return if (this.shouldHideSenderImage(position))
            TYPE_MESSAGE_THEIRS_NO_IMAGE
        else
            TYPE_MESSAGE_THEIRS
    }

    override fun getItemCount(): Int = this.messages.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemView = this.layoutInflater.inflate(viewType, parent, false)

        return when (viewType) {
            TYPE_MESSAGE_OUR                -> OurMessageViewHolder(itemView)
            TYPE_MESSAGE_THEIRS             -> TheirMessageViewHolder(itemView)
            TYPE_MESSAGE_THEIRS_NO_IMAGE    -> TheirMessageNoImageViewHolder(itemView)
            else -> throw IllegalArgumentException("Unknown ViewHolder type!")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val currentMessage = this.messages[position]

        when (holder) {
            is OurMessageViewHolder -> holder.bind(currentMessage)
            is TheirMessageViewHolder -> holder.bind(currentMessage)
            is TheirMessageNoImageViewHolder -> holder.bind(currentMessage)
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

    inner class OurMessageViewHolder(itemView: View) : BaseViewHolder(itemView) {
        lateinit var message: Message
        private val messageBody: TextView = itemView.findViewById(R.id.tv_ourMessageText)

        override fun bind(message: Message) {
            this.messageBody.text = message.text
        }

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

    inner class TheirMessageViewHolder(itemView: View) : BaseViewHolder(itemView) {
        lateinit var message: Message
        private val messageBody: TextView = itemView.findViewById(R.id.tv_theirMessageText)
        private val contactPhoto: ImageView = itemView.findViewById(R.id.message_list_sender_photo_civ)

        override fun bind(message: Message) {
            this.messageBody.text = message.text
            Glide
                .with(itemView)
                .load(Uri.parse(senderPhotoUri ?: ""))
                .apply(RequestOptions().placeholder(R.drawable.unknown_contact))
                .into(this.contactPhoto)
        }

        init {
            // TODO: [Refactor] Figure out how to reuse the event registration logic
            itemView.setOnLongClickListener {
                core.onLongClickMessage(this.message)
                true
            }

            itemView.setOnClickListener {
                core.onClickMessage()
            }
        }
    }

    inner class TheirMessageNoImageViewHolder(itemView: View) : BaseViewHolder(itemView) {
        lateinit var message: Message
        private val messageBody: TextView = itemView.findViewById(R.id.tv_theirMessageText)

        override fun bind(message: Message) {
            this.messageBody.text = message.text
        }

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