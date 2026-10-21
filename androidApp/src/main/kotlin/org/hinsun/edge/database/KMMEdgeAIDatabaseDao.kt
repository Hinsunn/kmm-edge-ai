package org.hinsun.edge.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.hinsun.edge.database.entites.LiteRTLMEntity

@Dao
interface KMMEdgeAIDatabaseDao {

    @Query("SELECT * FROM litert_lm ORDER BY name")
    fun observeModels(): Flow<List<LiteRTLMEntity>>

    @Query("SELECT * FROM litert_lm ORDER BY updated_at")
    suspend fun models(): List<LiteRTLMEntity>

    @Query("SELECT * FROM litert_lm WHERE id = :id")
    suspend fun model(id: String): LiteRTLMEntity?

    @Upsert
    suspend fun upsert(models: List<LiteRTLMEntity>)

    @Update
    suspend fun update(model: LiteRTLMEntity)

    // Column-only writes so a running transfer never overwrites a status change made meanwhile.
    @Query("UPDATE litert_lm SET downloaded_bytes = :downloadedBytes, total_bytes = :totalBytes, download_progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, downloadedBytes: Long, totalBytes: Long, progress: Double)

    @Query("UPDATE litert_lm SET total_bytes = :totalBytes WHERE id = :id AND total_bytes = 0")
    suspend fun updateTotalBytes(id: String, totalBytes: Long)
}
