package com.shogek.spinoza.db.databaseInformation

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update


@Dao
interface DatabaseInformationDao {

    @Insert
    suspend fun insert(state: DatabaseInformation)

    @Query("SELECT * FROM database_information_table WHERE id = :id")
    suspend fun get(id: Long): DatabaseInformation?

    @Update
    suspend fun update(state: DatabaseInformation)

    @Query("DELETE FROM database_information_table")
    /* FOR DEVELOPMENT USE ONLY */
    suspend fun nuke()
}