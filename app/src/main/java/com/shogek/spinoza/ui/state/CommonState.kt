package com.shogek.spinoza.ui.state

object CommonState {

    /** Stores the ID of the conversation that is currently opened (if one is). */
    private var openedConversationId: Long? = null

    /** Indicate to the whole application, that currently the user is in a specific conversation. */
    fun setCurrentOpenConversationId(id: Long) {
        this.openedConversationId = id
    }

    fun getCurrentOpenConversationId(): Long? {
        return this.openedConversationId
    }

    /** Indicate to the whole application, that currently the user is not in a conversation. */
    fun clearCurrentOpenConversationId() {
        this.openedConversationId = null
    }
}