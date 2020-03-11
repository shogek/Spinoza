package com.shogek.spinoza.db.databaseInformation

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import androidx.room.Entity


@Entity(tableName = "database_information_table")
data class DatabaseInformation(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "conversation_table_last_updated_timestamp")
    var conversationTableLastUpdatedTimestamp: Long,

    @ColumnInfo(name = "contact_table_last_updated_timestamp")
    var contactTableLastUpdatedTimestamp: Long
)