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
    fun getAllObservable(): LiveData<List<Contact>>

    @Query("SELECT * FROM contact_table")
    suspend fun getAll(): List<Contact>

    @Query("SELECT * FROM contact_table WHERE id IN (:ids)")
    suspend fun getAll(ids: List<Long>): List<Contact>

    @Query("SELECT * FROM contact_table WHERE id = :id")
    fun get(id: Long): Contact

    @Update
    suspend fun update(contact: Contact)

    @Update
    suspend fun updateAll(contacts: List<Contact>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: Contact): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(contacts: List<Contact>): List<Long>

    @Delete
    suspend fun deleteAll(contacts: List<Contact>)

    @Delete
    suspend fun delete(contact: Contact)

    @Query("DELETE FROM contact_table")
    /* FOR DEVELOPMENT USE ONLY */
    suspend fun nuke()
}