package com.shogek.spinoza.db.applicationDatabaseState

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import androidx.room.Entity


@Entity(tableName = "application_database_state_table")
data class ApplicationDatabaseState(

    @ColumnInfo(name = "conversation_table_last_updated_timestamp")
    var conversationTableLastUpdatedTimestamp: Long,

    @ColumnInfo(name = "contact_table_last_updated_timestamp")
    var contactTableLastUpdatedTimestamp: Long
) {
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Long = 0
}

const val ApplicationDatabaseStateSingletonId: Long = 666L