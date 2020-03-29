package com.shogek.spinoza.db.message

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Update
import androidx.room.OnConflictStrategy


@Dao
interface MessageDao {

    @Query("SELECT * FROM message_table WHERE id = :messageId")
    suspend fun get(messageId: Long): Message

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: Message): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(messages: List<Message>)

    @Update
    suspend fun update(message: Message)

    @Query("DELETE FROM message_table WHERE conversation_id IN (:conversationIds)")
    suspend fun deleteAllByConversationIds(conversationIds: List<Long>)

    @Delete
    suspend fun delete(message: Message)


    @Query("DELETE FROM message_table")
    /* FOR DEVELOPMENT USE ONLY */
    suspend fun nuke()
}