//
// File        : LiteRTLMModelDownloadManager.kt
// Author      : Tran Van Hoai
// Date        : 21/10/26
//

package org.hinsun.edge.core.managers

import android.content.Context
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.hinsun.edge.BuildConfig
import org.hinsun.edge.database.KMMEdgeAIDatabase
import org.hinsun.edge.database.KMMEdgeAIDatabaseDao
import org.hinsun.edge.database.entites.LiteRTLMDownloadStatus
import org.hinsun.edge.database.entites.LiteRTLMEntity
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Android port of iosApp `LiteRTLMModelDownloadManager`.
 *
 * The Room row is the source of truth (SwiftData on iOS). Bytes land in `<modelPath>.part` and are
 * resumed with an HTTP `Range` request, which plays the role of iOS resume data: pausing, failing or
 * killing the app keeps the part file, and the next start continues from its length.
 */
object LiteRTLMModelDownloadManager {

    /** The maximum number of concurrent downloads allowed, same as iOS. */
    private const val MAX_CONCURRENT_DOWNLOADS = 5
    private const val PROGRESS_SAVE_INTERVAL_MS = 500L
    private const val TIMEOUT_MS = 30_000

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Serializes status transitions; [jobs] is only touched while holding it. */
    private val mutex = Mutex()
    private val jobs = mutableMapOf<String, Job>()

    private lateinit var dao: KMMEdgeAIDatabaseDao
    private lateinit var modelsDirectory: File

    val models: Flow<List<LiteRTLMEntity>> get() = dao.observeModels()

    /** Call once on app start so downloads interrupted by a kill resume without opening the Download tab. */
    fun init(context: Context) {
        if (::dao.isInitialized) return
        dao = KMMEdgeAIDatabase.get(context).dao()
        // noBackupFilesDir: the iOS side excludes Models/ from backup too.
        modelsDirectory = File(context.noBackupFilesDir, "Models").apply { mkdirs() }

        scope.launch {
            mutex.withLock {
                seed()
                reattach()
                pump()
            }

            fetchSize()
        }
    }

    // region Public Methods

    fun download(model: LiteRTLMEntity) = transition(model) { row ->
        if (row.downloadStatus == LiteRTLMDownloadStatus.IDLE || row.downloadStatus == LiteRTLMDownloadStatus.FAILED) {
            save(row.copy(downloadStatus = LiteRTLMDownloadStatus.ENQUEUED))
        }
    }

    fun pause(model: LiteRTLMEntity) = transition(model) { row ->
        when (row.downloadStatus) {
            LiteRTLMDownloadStatus.ENQUEUED -> save(row.copy(downloadStatus = LiteRTLMDownloadStatus.PAUSED))
            LiteRTLMDownloadStatus.DOWNLOADING -> {
                jobs.remove(row.id)?.cancelAndJoin()
                val latest = dao.model(row.id) ?: return@transition
                save(latest.withPartProgress().copy(downloadStatus = LiteRTLMDownloadStatus.PAUSED))
            }

            else -> Unit
        }
    }

    fun resume(model: LiteRTLMEntity) = transition(model) { row ->
        if (row.downloadStatus == LiteRTLMDownloadStatus.PAUSED || row.downloadStatus == LiteRTLMDownloadStatus.FAILED) {
            save(row.copy(downloadStatus = LiteRTLMDownloadStatus.ENQUEUED))
        }
    }

    fun remove(model: LiteRTLMEntity) = transition(model) { row ->
        // Join first so the transfer cannot write the part file again after we delete it.
        jobs.remove(row.id)?.cancelAndJoin()
        fileFor(row).delete()
        partFileFor(row).delete()
        save(
            row.copy(
                downloadStatus = LiteRTLMDownloadStatus.IDLE,
                errorMessage = null,
                downloadProgress = 0.0,
                downloadedBytes = 0,
            )
        )
    }

    // endregion

    // region Private Methods

    private fun transition(model: LiteRTLMEntity, block: suspend (LiteRTLMEntity) -> Unit) {
        scope.launch {
            mutex.withLock {
                // The UI hands us a snapshot; act on the current row.
                dao.model(model.id)?.let { block(it) }
                pump()
            }
        }
    }

    private suspend fun seed() {
        val existing = dao.models().associateBy { it.downloadURL }
        dao.upsert(
            LiteRTLMEntity.availableModels.map { model ->
                // Catalog copy lives in code; refresh it so edits reach rows seeded earlier.
                existing[model.downloadURL]?.copy(name = model.name, summary = model.summary)
                    ?: model
            }
        )

        for (row in dao.models()) {
            if (row.downloadStatus == LiteRTLMDownloadStatus.COMPLETED && !fileFor(row).exists()) {
                save(
                    row.copy(
                        downloadStatus = LiteRTLMDownloadStatus.IDLE,
                        downloadProgress = 0.0,
                        downloadedBytes = 0
                    )
                )
            }
        }
    }

    /** No transfer survives a process death, so whatever was downloading goes back to the queue. */
    private suspend fun reattach() {
        for (row in dao.models()) {
            if (row.downloadStatus == LiteRTLMDownloadStatus.DOWNLOADING) {
                save(row.withPartProgress().copy(downloadStatus = LiteRTLMDownloadStatus.ENQUEUED))
            }
        }
    }

    private suspend fun pump() {
        val models = dao.models()
        var active = models.count { it.downloadStatus == LiteRTLMDownloadStatus.DOWNLOADING }
        for (model in models) {
            if (active >= MAX_CONCURRENT_DOWNLOADS) break
            if (model.downloadStatus != LiteRTLMDownloadStatus.ENQUEUED) continue
            start(model)
            active++
        }
    }

    private suspend fun start(model: LiteRTLMEntity) {
        save(model.copy(downloadStatus = LiteRTLMDownloadStatus.DOWNLOADING, errorMessage = null))
        jobs[model.id] = scope.launch { transfer(model) }
        nsLog("Started download for model ${model.name}")
    }

    private suspend fun transfer(model: LiteRTLMEntity) {
        val error = try {
            downloadPart(model)
            null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.message ?: e.javaClass.simpleName
        }

        val job = currentCoroutineContext().job
        mutex.withLock {
            // Paused or removed meanwhile: that path already owns the row.
            if (jobs[model.id] !== job) return
            jobs.remove(model.id)
            val row = dao.model(model.id) ?: return

            val destination = fileFor(row)
            if (error != null) {
                save(
                    row.withPartProgress()
                        .copy(downloadStatus = LiteRTLMDownloadStatus.FAILED, errorMessage = error)
                )
            } else {
                destination.delete()
                if (partFileFor(row).renameTo(destination)) {
                    save(
                        row.copy(
                            downloadStatus = LiteRTLMDownloadStatus.COMPLETED,
                            downloadProgress = 1.0,
                            downloadedBytes = destination.length(),
                            totalBytes = destination.length(),
                        )
                    )
                    nsLog("Download completed for model ${row.name} at ${destination.path}")
                } else {
                    save(
                        row.copy(
                            downloadStatus = LiteRTLMDownloadStatus.FAILED,
                            errorMessage = "Could not move downloaded file"
                        )
                    )
                }
            }
            pump()
        }
    }

    /** Appends to the part file from its current length. Returns normally once the file is complete. */
    private suspend fun downloadPart(model: LiteRTLMEntity) = coroutineScope {
        val part = partFileFor(model)
        val offset = part.length()
        val connection = open(model.downloadURL, "GET")
        if (offset > 0) connection.setRequestProperty("Range", "bytes=$offset-")

        // A blocked socket read ignores cancellation; disconnecting makes it throw so pause/remove are instant.
        val watcher = launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                awaitCancellation()
            } finally {
                connection.disconnect()
            }
        }

        try {
            val start = when (val code = connection.responseCode) {
                HttpURLConnection.HTTP_PARTIAL -> offset
                HttpURLConnection.HTTP_OK -> 0L // Server ignored the range: start over.
                // Range starts at the end: the previous run wrote everything but died before the rename.
                416 -> if (model.totalBytes > 0 && offset == model.totalBytes) return@coroutineScope else {
                    part.delete()
                    error("HTTP 416")
                }

                else -> error("HTTP $code")
            }
            val length = connection.contentLengthLong
            val total = if (length > 0) start + length else model.totalBytes

            var written = start
            var lastSave = 0L
            dao.updateProgress(model.id, written, total, progress(written, total))

            connection.inputStream.use { input ->
                FileOutputStream(part, start > 0).use { output ->
                    val buffer = ByteArray(256 * 1024)
                    while (true) {
                        ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        written += read

                        val now = SystemClock.elapsedRealtime()
                        if (now - lastSave >= PROGRESS_SAVE_INTERVAL_MS) {
                            lastSave = now
                            dao.updateProgress(model.id, written, total, progress(written, total))
                        }
                    }
                }
            }
            if (total > 0 && written != total) error("Connection closed at $written of $total bytes")
        } finally {
            watcher.cancel()
        }
    }

    private suspend fun fetchSize() {
        for (model in dao.models()) {
            if (model.totalBytes != 0L) continue
            nsLog("Fetching size for model ${model.name} from ${model.downloadURL}")
            try {
                // Follows Hugging Face's redirect to the CDN, whose response has the real length.
                val connection = open(model.downloadURL, "HEAD")
                try {
                    val length = connection.contentLengthLong
                    if (connection.responseCode == HttpURLConnection.HTTP_OK && length > 0) {
                        dao.updateTotalBytes(model.id, length)
                    }
                } finally {
                    connection.disconnect()
                }
            } catch (e: Exception) {
                nsLog("Failed to fetch size for ${model.name}: $e")
            }
        }
    }

    private fun open(url: String, method: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            instanceFollowRedirects = true
            // Transparent gzip would make byte offsets meaningless for Range resume.
            setRequestProperty("Accept-Encoding", "identity")
            // Only send the token when one is configured; a bad token breaks public repos with 401.
            if (BuildConfig.HF_TOKEN.startsWith("hf_")) {
                setRequestProperty("Authorization", "Bearer ${BuildConfig.HF_TOKEN}")
            }
        }

    private suspend fun save(model: LiteRTLMEntity) =
        dao.update(model.copy(updatedAt = System.currentTimeMillis()))

    private fun LiteRTLMEntity.withPartProgress(): LiteRTLMEntity {
        val bytes = partFileFor(this).length()
        return copy(downloadedBytes = bytes, downloadProgress = progress(bytes, totalBytes))
    }

    private fun progress(written: Long, total: Long) =
        if (total > 0) written.toDouble() / total else 0.0

    private fun fileFor(model: LiteRTLMEntity) = File(modelsDirectory, model.modelPath)

    private fun partFileFor(model: LiteRTLMEntity) =
        File(modelsDirectory, "${model.modelPath}.part")

    // endregion

    private fun nsLog(message: String) {
        if (BuildConfig.DEBUG) Log.d("LiteRTLMModelDownloadManager", message)
    }
}
