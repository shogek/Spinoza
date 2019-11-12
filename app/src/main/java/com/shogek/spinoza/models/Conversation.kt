package com.shogek.spinoza.models

import android.net.Uri

class Conversation {
    var senderPhone: String = ""
    var senderName: String? = null
    var senderId: String? = null
    var messages: Array<Message> = arrayOf()
    var photo: Uri? = null

    // TODO: Implement
    var isMyMessage: Boolean = true
}