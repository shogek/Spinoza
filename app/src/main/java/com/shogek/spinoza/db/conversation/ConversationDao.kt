package com.shogek.spinoza.db.conversation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ConversationDao {

    @Query("DELETE FROM conversation_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM conversation_table WHERE id = :id")
    fun get(id: Long): LiveData<Conversation>

    @Query("SELECT * FROM conversation_table")
    fun getAll(): LiveData<List<Conversation>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(conversation: Conversation): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(conversations: List<Conversation>): List<Long>

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