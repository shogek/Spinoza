package com.shogek.spinoza.events

data class ConversationActionEvent(
    val action: ConversationActions,
    val conversationId: Number
)

// TODO: [Refactor] Implement in separate data classes
enum class ConversationActions {
    ARCHIVE,
    DELETE,
    MUTE,
    UNREAD,
    IGNORE,
    BLOCK
}