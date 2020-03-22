package com.shogek.spinoza.ui.messages.list

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
import com.shogek.spinoza.db.message.Message
import java.lang.IllegalArgumentException

class MessageListAdapter(
    context: Context,
    private val onClickMessage: () -> Unit,
    private val onLongClickMessage: (Message) -> Unit
): RecyclerView.Adapter<MessageListAdapter.BaseViewHolder>() {

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(message: Message)
    }

    private var senderPhotoUri: String? = null
    private var messages = listOf<Message>()
    private val layoutInflater = LayoutInflater.from(context)


    private companion object {
        const val TYPE_MESSAGE_OUR = R.layout.message_list_item_ours
        const val TYPE_MESSAGE_THEIRS = R.layout.message_list_item_theirs
        const val TYPE_MESSAGE_THEIRS_NO_IMAGE = R.layout.message_list_item_theirs_no_image
    }


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
            TYPE_MESSAGE_OUR -> OurMessageViewHolder(itemView)
            TYPE_MESSAGE_THEIRS -> TheirMessageViewHolder(itemView)
            TYPE_MESSAGE_THEIRS_NO_IMAGE -> TheirMessageNoImageViewHolder(itemView)
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

    private fun messageLongClicked(message: Message) {
        this.onLongClickMessage(message)
    }

    private fun messageClicked() {
        this.onClickMessage()
    }

    fun setContactImage(photoUri: String?) {
        this.senderPhotoUri = photoUri
    }

    fun setMessages(messages: List<Message>) {
        this.messages = messages
        notifyDataSetChanged()
    }


    inner class OurMessageViewHolder(itemView: View) : BaseViewHolder(itemView) {
        lateinit var message: Message
        private val messageBody: TextView = itemView.findViewById(R.id.tv_ourMessageText)

        override fun bind(message: Message) {
            this.message = message
            this.messageBody.text = message.body
        }

        init {
            itemView.setOnLongClickListener { messageLongClicked(this.message); true }
            itemView.setOnClickListener { messageClicked() }
        }
    }

    inner class TheirMessageViewHolder(itemView: View) : BaseViewHolder(itemView) {
        lateinit var message: Message
        private val messageBody: TextView = itemView.findViewById(R.id.tv_theirMessageText)
        private val contactPhoto: ImageView = itemView.findViewById(R.id.message_list_sender_photo_civ)

        override fun bind(message: Message) {
            this.message = message
            this.messageBody.text = message.body

            Glide.with(itemView)
                 .load(Uri.parse(senderPhotoUri ?: ""))
                 .apply(RequestOptions().placeholder(R.drawable.unknown_contact))
                 .into(this.contactPhoto)
        }

        init {
            itemView.setOnLongClickListener { messageLongClicked(this.message); true }
            itemView.setOnClickListener { messageClicked() }
        }
    }

    inner class TheirMessageNoImageViewHolder(itemView: View) : BaseViewHolder(itemView) {
        lateinit var message: Message
        private val messageBody: TextView = itemView.findViewById(R.id.tv_theirMessageText)

        override fun bind(message: Message) {
            this.message = message
            this.messageBody.text = message.body
        }

        init {
            itemView.setOnLongClickListener { messageLongClicked(this.message); true }
            itemView.setOnClickListener     { messageClicked() }
        }
    }
}