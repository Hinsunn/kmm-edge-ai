package org.hinsun.edge.database

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase

//
// File        : EdgeAIDatabase.android.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//

fun databaseBuilder(context: Context): RoomDatabase.Builder<EdgeAIDatabase> {
    val applicationContext = context.applicationContext
    val databaseFile = applicationContext.getDatabasePath("edge_ai_database.db")
    return Room.databaseBuilder<EdgeAIDatabase>(
        context = applicationContext,
        name = databaseFile.absolutePath,
    )
}