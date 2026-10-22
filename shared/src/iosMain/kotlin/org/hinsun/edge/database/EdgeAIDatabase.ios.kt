package org.hinsun.edge.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

//
// File        : EdgeAIDatabase.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//

fun databaseBuilder(): RoomDatabase.Builder<EdgeAIDatabase> {
    val databaseFilePath = documentDirectory() + "/edge_ai_database.db"
    return Room.databaseBuilder<EdgeAIDatabase>(
        name = databaseFilePath,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )

    return requireNotNull(documentDirectory?.path)
}