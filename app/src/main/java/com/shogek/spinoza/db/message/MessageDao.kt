package com.shogek.spinoza.db.message

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.OnConflictStrategy


@Dao
interface MessageDao {

    @Query("SELECT * FROM message_table WHERE conversation_id = :conversationId ORDER BY timestamp ASC")
    fun getAllObservable(conversationId: Long): LiveData<List<Message>>

    @Query("SELECT * FROM message_table WHERE android_id NOT NULL")
    suspend fun getAllAndroid(): List<Message>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: Message)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(messages: List<Message>)

    @Query("DELETE FROM message_table WHERE conversation_id IN (:conversationIds)")
    suspend fun deleteAllByConversationIds(conversationIds: List<Long>)

    @Delete
    suspend fun deleteAll(messages: List<Message>)

    @Delete
    suspend fun delete(message: Message)


    @Query("DELETE FROM message_table")
    /* FOR DEVELOPMENT USE ONLY */
    suspend fun nuke()
}