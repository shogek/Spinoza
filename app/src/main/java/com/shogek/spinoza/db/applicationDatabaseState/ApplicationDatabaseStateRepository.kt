package com.shogek.spinoza.db.applicationDatabaseState

import android.content.Context
import com.shogek.spinoza.db.ApplicationRoomDatabase
import kotlinx.coroutines.CoroutineScope


class ApplicationDatabaseStateRepository(
    context: Context,
    scope: CoroutineScope
) {

    private val dao = ApplicationRoomDatabase.getDatabase(context, scope).applicationDatabaseStateDao()

    suspend fun createSingleton(
        conversationsLastUpdated: Long,
        contactsLastUpdated: Long
    ) {
        val instance = ApplicationDatabaseState(conversationsLastUpdated, contactsLastUpdated)
        instance.id = ApplicationDatabaseStateSingletonId
        dao.createSingleton(instance)
    }

    suspend fun getSingleton(): ApplicationDatabaseState {
        return dao.getSingleton()
    }

    suspend fun updateSingleton(state: ApplicationDatabaseState) {
        state.id = ApplicationDatabaseStateSingletonId
        dao.updateSingleton(state)
    }
}