package com.shogek.spinoza.ui.messages.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.shogek.spinoza.db.ApplicationRoomDatabase
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationDao
import com.shogek.spinoza.db.message.Message
import com.shogek.spinoza.db.message.MessageDao

class MessageListViewModel(application: Application) : AndroidViewModel(application) {

    private val conversationDao: ConversationDao
    private val messageDao: MessageDao

    lateinit var conversation: LiveData<Conversation>
    lateinit var messages: LiveData<List<Message>>

    init {
        val database = ApplicationRoomDatabase.getDatabase(application, viewModelScope)
        this.conversationDao = database.conversationDao()
        this.messageDao = database.messageDao()
    }

    fun init(conversationId: Long): MessageListViewModel {
        this.messages = this.messageDao.getAllMessages(conversationId)
        this.conversation = this.conversationDao.getObservable(conversationId)
        return this
    }
}