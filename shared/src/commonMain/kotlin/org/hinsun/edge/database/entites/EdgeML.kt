package org.hinsun.edge.database.entites

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.time.Clock
import kotlin.uuid.Uuid

//
// File        : LiteRTModel.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//

enum class EdgeMLDownloadStatus {
    IDLE,
    ENQUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

enum class EdgeMLFamily {
    LITERT_LM,
    EXECUTORCH
}

@Entity(
    tableName = "edge_mls",
    foreignKeys = [],
    indices = [],
)
data class EdgeML(
    @PrimaryKey val id: String = Uuid.random().toString(),
    var name: String,
    var summary: String,
    @ColumnInfo(name = "model_path") var modelPath: String,
    @ColumnInfo(name = "model_family") var modelFamily: EdgeMLFamily,

    @ColumnInfo(name = "download_url") var downloadURL: String,
    @ColumnInfo(name = "download_status") var downloadStatus: EdgeMLDownloadStatus = EdgeMLDownloadStatus.IDLE,
    @ColumnInfo(name = "download_progress") var downloadProgress: Double = 0.0,
    @ColumnInfo(name = "downloaded_bytes") var downloadedBytes: Long = 0,
    @ColumnInfo(name = "total_bytes") var totalBytes: Long = 0,

    @ColumnInfo(name = "error_message") var errorMessage: String? = null,
    @ColumnInfo(name = "created_at") var createdAt: Long = Clock.System.now().epochSeconds,
    @ColumnInfo(name = "updated_at") var updatedAt: Long = Clock.System.now().epochSeconds,
)