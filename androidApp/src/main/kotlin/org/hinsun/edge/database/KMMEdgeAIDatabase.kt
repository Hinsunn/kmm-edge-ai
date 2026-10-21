package org.hinsun.edge.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.hinsun.edge.database.entites.LiteRTLMEntity

@Database(entities = [LiteRTLMEntity::class], version = 1)
abstract class KMMEdgeAIDatabase : RoomDatabase() {

    abstract fun dao(): KMMEdgeAIDatabaseDao

    companion object {
        @Volatile
        private var instance: KMMEdgeAIDatabase? = null

        fun get(context: Context): KMMEdgeAIDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                KMMEdgeAIDatabase::class.java,
                "kmm_edge_ai.db"
            ).build().also { instance = it }
        }
    }
}
