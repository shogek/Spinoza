package com.shogek.spinoza.db.message

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessageDao {

    @Query("SELECT * FROM message_table WHERE conversation_id = :conversationId ORDER BY timestamp ASC")
    fun getAllObservable(conversationId: Long): LiveData<List<Message>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: Message)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(messages: List<Message>)

    @Query("DELETE FROM message_table")
    /* FOR DEVELOPMENT USE ONLY */
    suspend fun nuke()
}