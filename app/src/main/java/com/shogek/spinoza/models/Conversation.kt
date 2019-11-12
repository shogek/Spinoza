package com.shogek.spinoza.models

import android.net.Uri
import java.time.LocalDateTime

class Conversation(
    val sender: String,
    val message: String,
    val isMyMessage: Boolean,
    val seen: Boolean,
    val date: LocalDateTime,
    val senderImage: Uri?
) {
}