package org.hinsun.edge.di

import androidx.room3.RoomDatabase
import org.hinsun.edge.database.EdgeAIDatabase
import org.hinsun.edge.database.roomDatabase
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

//
// File        : CoreModule.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//

@Module
@ComponentScan("org.hinsun.edge")
@Configuration
class CoreModule {

    // RoomDatabase.Builder comes from the platform module (AndroidModule / IosModule)
    @Single
    fun database(
        builder: RoomDatabase.Builder<EdgeAIDatabase>
    ): EdgeAIDatabase = roomDatabase(builder)
}

@KoinApplication
object EdgeAIKoinApp
