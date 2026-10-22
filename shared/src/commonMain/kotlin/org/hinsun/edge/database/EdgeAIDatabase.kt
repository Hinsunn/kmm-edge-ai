package org.hinsun.edge.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.hinsun.edge.database.entites.Conversation
import org.hinsun.edge.database.entites.EdgeML
import org.hinsun.edge.database.entites.Message

//
// File        : EdgeAIDatabase.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//


@Database(
    entities = [
        Message::class,
        Conversation::class,
        EdgeML::class
    ],
    version = 1,
)
@ConstructedBy(EdgeAIDatabaseConstructor::class)
abstract class EdgeAIDatabase : RoomDatabase() {
    abstract fun dao(): EdgeAIDatabaseDao
}

expect object EdgeAIDatabaseConstructor : RoomDatabaseConstructor<EdgeAIDatabase> {
    override fun initialize(): EdgeAIDatabase
}

fun roomDatabase(builder: RoomDatabase.Builder<EdgeAIDatabase>): EdgeAIDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}