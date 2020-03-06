package com.shogek.spinoza.ui.messages.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.shogek.spinoza.db.conversation.Conversation
import com.shogek.spinoza.db.conversation.ConversationDao
import com.shogek.spinoza.db.conversation.ConversationRoomDatabase
import com.shogek.spinoza.db.message.Message
import com.shogek.spinoza.db.message.MessageDao
import com.shogek.spinoza.db.message.MessageRoomDatabase

class MessageListViewModel(application: Application) : AndroidViewModel(application) {

    private val conversationDao: ConversationDao = ConversationRoomDatabase.getDatabase(application, viewModelScope).conversationDao()
    private val messageDao: MessageDao = MessageRoomDatabase.getDatabase(application).messageDao()
    lateinit var conversation: LiveData<Conversation>
    lateinit var messages: LiveData<List<Message>>

    fun init(conversationId: Long): MessageListViewModel {
        this.messages = this.messageDao.getAllMessages(conversationId)
        this.conversation = this.conversationDao.getObservable(conversationId)
        return this
    }
}