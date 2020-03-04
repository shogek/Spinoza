package com.shogek.spinoza.db.contact

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContactDao {

    @Query("SELECT * FROM contact_table")
    fun getAll(): LiveData<List<Contact>>

    @Update
    suspend fun update(contact: Contact)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: Contact): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(contacts: List<Contact>): List<Long>

    @Query("DELETE FROM contact_table")
    suspend fun deleteAll()
}