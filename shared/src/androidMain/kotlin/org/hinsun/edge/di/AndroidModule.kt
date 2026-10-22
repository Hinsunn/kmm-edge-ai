package org.hinsun.edge.di

import android.content.Context
import androidx.room3.RoomDatabase
import org.hinsun.edge.database.EdgeAIDatabase
import org.hinsun.edge.database.databaseBuilder
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

//
// File        : AndroidModule.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//

@Module
@Configuration
class AndroidModule {
    // Context is provided by androidContext(...) at startKoin
    @Single
    fun roomBuilder(context: Context): RoomDatabase.Builder<EdgeAIDatabase> = databaseBuilder(context)
}
