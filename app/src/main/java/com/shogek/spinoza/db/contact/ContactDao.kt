package com.shogek.spinoza.db.contact

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.OnConflictStrategy

@Dao
interface ContactDao {

    @Query("SELECT * FROM contact_table")
    fun getAll(): LiveData<List<Contact>>

    @Query("SELECT * FROM contact_table WHERE contact_contact_id = :contactId")
    fun get(contactId: Long): Contact

    @Update
    suspend fun update(contact: Contact)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: Contact): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(contacts: List<Contact>): List<Long>

    @Query("DELETE FROM contact_table")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(contact: Contact)
}