package com.shogek.spinoza.db.applicationDatabaseState

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update


@Dao
interface ApplicationDatabaseStateDao {

    @Insert
    // TODO: [Refactor] A singleton should be get and set - creation is not the responsibility of the programmer
    suspend fun createSingleton(state: ApplicationDatabaseState)

    @Query("SELECT * FROM application_database_state_table WHERE id = $ApplicationDatabaseStateSingletonId")
    suspend fun getSingleton(): ApplicationDatabaseState

    @Update
    suspend fun updateSingleton(state: ApplicationDatabaseState)
}