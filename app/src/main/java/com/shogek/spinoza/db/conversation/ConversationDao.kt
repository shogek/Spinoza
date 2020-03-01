package com.shogek.spinoza.db.conversation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversation_table")
    fun getAllConversations(): LiveData<List<Conversation>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(conversation: Conversation): Long

    @Query("" +
            "UPDATE conversation_table " +
            "SET snippet = :snippet " +
            "   ,snippet_timestamp = :snippetTimestamp " +
            "   ,snippet_ours = :snippetIsOurs " +
            "   ,snippet_read = :snippetWasRead " +
            "WHERE id = :id")
    suspend fun update(
        id: Long,
        snippet: String,
        snippetTimestamp: Long,
        snippetIsOurs: Boolean,
        snippetWasRead: Boolean
    )
}