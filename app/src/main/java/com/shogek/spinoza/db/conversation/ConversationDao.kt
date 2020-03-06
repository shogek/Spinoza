package com.shogek.spinoza.db.conversation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.OnConflictStrategy

@Dao
interface ConversationDao {

    @Query("DELETE FROM conversation_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM conversation_table WHERE conversation_conversation_id = :id")
    fun get(id: Long): LiveData<Conversation>

    @Query("SELECT * FROM conversation_table")
    fun getAll(): LiveData<List<Conversation>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(conversation: Conversation): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(conversations: List<Conversation>): List<Long>

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun update(conversation: Conversation)

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateAll(conversations: List<Conversation>)

    // TODO: Use '@Update'
    @Query("" +
            "UPDATE conversation_table " +
            "SET conversation_snippet           = :snippet " +
            "   ,conversation_snippet_timestamp = :snippetTimestamp " +
            "   ,conversation_snippet_is_ours   = :snippetIsOurs " +
            "   ,conversation_snippet_was_read  = :snippetWasRead " +
            "WHERE conversation_conversation_id = :id")
    suspend fun update(
        id: Long,
        snippet: String,
        snippetTimestamp: Long,
        snippetIsOurs: Boolean,
        snippetWasRead: Boolean
    )
}