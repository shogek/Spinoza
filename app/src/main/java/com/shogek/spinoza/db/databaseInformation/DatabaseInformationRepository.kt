package com.shogek.spinoza.db.databaseInformation

import android.content.Context
import com.shogek.spinoza.db.ApplicationRoomDatabase
import kotlinx.coroutines.CoroutineScope


class DatabaseInformationRepository(
    context: Context,
    scope: CoroutineScope
) {

    private val dao = ApplicationRoomDatabase.getDatabase(context, scope).applicationDatabaseStateDao()
    private val singletonId: Long = 666L


    suspend fun getSingleton(): DatabaseInformation {
        val singleton = dao.get(this.singletonId)
        if (singleton != null) {
            return singleton
        }

        val information = DatabaseInformation(this.singletonId, 0, 0)
        dao.insert(information)
        return dao.get(this.singletonId)!!
    }

    suspend fun updateSingleton(state: DatabaseInformation) {
        dao.update(state)
    }
}