package com.shogek.spinoza.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shogek.spinoza.models.Conversation
import com.shogek.spinoza.repositories.ConversationRepository

class ConversationListViewModel(
    application: Application
) : AndroidViewModel(application) {

    var conversations: LiveData<List<Conversation>> = MutableLiveData()

    init {
        this.conversations = ConversationRepository(application.applicationContext).getAll()
    }
}