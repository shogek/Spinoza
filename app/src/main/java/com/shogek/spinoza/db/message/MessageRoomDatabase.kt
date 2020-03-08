package com.shogek.spinoza.db.message

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shogek.spinoza.db.conversation.Conversation

@Database(
    entities = [Message::class, Conversation::class],
    version = 1,
    exportSchema = false
)
abstract class MessageRoomDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: MessageRoomDatabase? = null

        fun getDatabase (context: Context): MessageRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MessageRoomDatabase::class.java,
                    "message_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}