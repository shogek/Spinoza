package com.shogek.spinoza.db.message

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.OnConflictStrategy


@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: Message)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(messages: List<Message>)

    @Query("DELETE FROM message_table WHERE conversation_id IN (:conversationIds)")
    suspend fun deleteAllByConversationIds(conversationIds: List<Long>)

    @Delete
    suspend fun delete(message: Message)


    @Query("DELETE FROM message_table")
    /* FOR DEVELOPMENT USE ONLY */
    suspend fun nuke()
}