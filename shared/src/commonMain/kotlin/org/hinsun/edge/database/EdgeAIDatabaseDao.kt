package org.hinsun.edge.database

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import org.hinsun.edge.database.entites.Conversation
import org.hinsun.edge.database.entites.EdgeML

//
// File        : EdgeAIDatabaseDao.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//

@Dao
interface EdgeAIDatabaseDao {

    // region Conversation
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(conversation: Conversation): Long

    @Upsert
    suspend fun upsert(conversation: Conversation): Long

    @Update
    suspend fun update(conversation: Conversation): Int

    @Query("SELECT * FROM conversations")
    fun conversations(): Flow<List<Conversation>>

    @Delete
    suspend fun delete(conversation: Conversation): Int

    // endregion Conversation

    // region EdgeML
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(edgeML: EdgeML): Long

    @Upsert
    suspend fun upsert(edgeML: EdgeML): Long

    @Update
    suspend fun update(edgeML: EdgeML): Int

    @Query("SELECT * FROM edge_mls")
    fun edgeMls(): Flow<List<EdgeML>>

    @Delete
    suspend fun delete(edgeML: EdgeML): Int

    // endregion EdgeML

}