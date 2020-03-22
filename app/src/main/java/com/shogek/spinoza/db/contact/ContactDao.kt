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

    @Update
    suspend fun updateAll(contacts: List<Contact>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(contacts: List<Contact>): List<Long>

    @Delete
    suspend fun deleteAll(contacts: List<Contact>)


    @Query("DELETE FROM contact_table")
    /* FOR DEVELOPMENT USE ONLY */
    suspend fun nuke()
}