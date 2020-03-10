package com.shogek.spinoza.db.conversation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.OnConflictStrategy


@Dao
interface ConversationDao {

    @Delete
    suspend fun delete(conversation: Conversation)

    @Query("SELECT * FROM conversation_table WHERE id = :id")
    fun getObservable(id: Long): LiveData<Conversation>

    @Query("SELECT * FROM conversation_table WHERE id = :id")
    suspend fun get(id: Long): Conversation

    @Query("SELECT * FROM conversation_table")
    suspend fun getAll(): List<Conversation>

    @Query("SELECT * FROM conversation_table")
    fun getAllObservable(): LiveData<List<Conversation>>

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
            "SET snippet           = :snippet " +
            "   ,snippet_timestamp = :snippetTimestamp " +
            "   ,snippet_is_ours   = :snippetIsOurs " +
            "   ,snippet_was_read  = :snippetWasRead " +
            "WHERE id = :id")
    suspend fun update(
        id: Long,
        snippet: String,
        snippetTimestamp: Long,
        snippetIsOurs: Boolean,
        snippetWasRead: Boolean
    )
}