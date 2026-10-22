package org.hinsun.edge.di

import androidx.room3.RoomDatabase
import org.hinsun.edge.database.EdgeAIDatabase
import org.hinsun.edge.database.databaseBuilder
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

//
// File        : IosModule.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//

@Module
@Configuration
class IosModule {
    
    @Single
    fun roomBuilder(): RoomDatabase.Builder<EdgeAIDatabase> = databaseBuilder()
}
