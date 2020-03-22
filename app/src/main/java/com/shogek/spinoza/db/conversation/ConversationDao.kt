package com.shogek.spinoza.db.conversation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Transaction
import androidx.room.OnConflictStrategy


@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversation_table WHERE id = :conversationId")
    fun getObservable(conversationId: Long): LiveData<ConversationAndContactAndMessages>

    @Query("SELECT * FROM conversation_table WHERE contact_id = :contactId")
    fun getByContactIdObservable(contactId: Long): LiveData<ConversationAndContactAndMessages>

    @Query("SELECT * FROM conversation_table WHERE contact_id = :contactId")
    suspend fun getByContactId(contactId: Long): ConversationAndContact

    @Query("SELECT * FROM conversation_table WHERE contact_id IN (:contactIds)")
    suspend fun getByContactIdsWithMessages(contactIds: List<Long>): List<ConversationAndContactAndMessages>

    @Query("SELECT * FROM conversation_table WHERE phone = :phone")
    suspend fun getByPhone(phone: String): ConversationAndContact?

    @Query("SELECT * FROM conversation_table WHERE id = :id")
    @Transaction
    fun getWithContactAndMessagesObservable(id: Long): LiveData<ConversationAndContactAndMessages>

    @Query("SELECT * FROM conversation_table")
    suspend fun getAll(): List<Conversation>

    @Query("SELECT * FROM conversation_table WHERE snippet != \"\"")
    @Transaction
    fun getAllWithContactsObservable(): LiveData<List<ConversationAndContact>>

    @Query("SELECT * FROM conversation_table WHERE contact_id IS NULL")
    suspend fun getContactless(): List<Conversation>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(conversations: List<Conversation>): List<Long>

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun update(conversation: Conversation)

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateAll(conversations: List<Conversation>)

    @Delete
    suspend fun deleteAll(conversations: List<Conversation>)


    @Query("DELETE FROM conversation_table")
    /* FOR DEVELOPMENT USE ONLY */
    suspend fun nuke()
}