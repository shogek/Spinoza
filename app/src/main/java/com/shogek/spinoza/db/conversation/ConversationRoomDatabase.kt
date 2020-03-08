package com.shogek.spinoza.db.conversation

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shogek.spinoza.db.state.CommonDatabaseState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [Conversation::class],
    version = 1,
    exportSchema = false
)
abstract class ConversationRoomDatabase : RoomDatabase() {

    abstract fun conversationDao(): ConversationDao

    private class ConversationDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        private var cameFromOnCreate = false

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            this.cameFromOnCreate = true

            INSTANCE?.let { database ->
                CommonDatabaseState.importConversationsFromPhone(context, scope, database.conversationDao())
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            if (this.cameFromOnCreate) {
                return
            }

            INSTANCE?.let { database -> scope.launch {
                CommonDatabaseState.synchronizeDatabaseWithExistingConversationsAndContacts(context, scope, database.conversationDao())
            }}
        }
    }


    companion object {
        @Volatile
        private var INSTANCE: ConversationRoomDatabase? = null

        fun getDatabase (
            context: Context,
            scope: CoroutineScope
        ): ConversationRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ConversationRoomDatabase::class.java,
                    "conversation_database"
                ).addCallback(ConversationDatabaseCallback(context, scope))
                 .fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}