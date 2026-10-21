package org.hinsun.edge.presentation.views.download

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowCircleDown
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.hinsun.edge.core.managers.LiteRTLMModelDownloadManager
import org.hinsun.edge.database.entites.LiteRTLMDownloadStatus
import org.hinsun.edge.database.entites.LiteRTLMEntity

@Composable
fun DownloadView(viewModel: DownloadViewModel = viewModel()) {
    val models by viewModel.models.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
    ) {
        item {
            Text(
                "LiteRTLM Models",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        itemsIndexed(models, key = { _, model -> model.id }) { index, model ->
            if (index > 0) HorizontalDivider()
            DownloadRow(model)
        }
    }
}

@Composable
private fun DownloadRow(model: LiteRTLMEntity) {
    val manager = LiteRTLMModelDownloadManager
    val inProgress = model.downloadStatus != LiteRTLMDownloadStatus.IDLE &&
            model.downloadStatus != LiteRTLMDownloadStatus.COMPLETED

    Column(
        modifier = Modifier.padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    model.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    sizeText(model),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (inProgress) {
                Text(
                    "%.2f".format(model.downloadProgress * 100),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            when (model.downloadStatus) {
                LiteRTLMDownloadStatus.IDLE -> IconButton(onClick = { manager.download(model) }) {
                    Icon(Icons.Outlined.ArrowCircleDown, contentDescription = "Download")
                }
                LiteRTLMDownloadStatus.ENQUEUED, LiteRTLMDownloadStatus.DOWNLOADING ->
                    IconButton(onClick = { manager.pause(model) }) {
                        Icon(Icons.Outlined.PauseCircle, contentDescription = "Pause")
                    }
                LiteRTLMDownloadStatus.PAUSED, LiteRTLMDownloadStatus.FAILED ->
                    IconButton(onClick = { manager.resume(model) }) {
                        Icon(Icons.Outlined.PlayCircle, contentDescription = "Resume")
                    }
                LiteRTLMDownloadStatus.COMPLETED -> IconButton(onClick = { manager.remove(model) }) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        if (inProgress) {
            LinearProgressIndicator(
                progress = { model.downloadProgress.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Text(
            model.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        model.errorMessage?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun sizeText(model: LiteRTLMEntity): String {
    val context = LocalContext.current
    if (model.totalBytes <= 0) return "0 MB"
    val total = Formatter.formatFileSize(context, model.totalBytes)
    return when (model.downloadStatus) {
        LiteRTLMDownloadStatus.IDLE, LiteRTLMDownloadStatus.COMPLETED -> total
        else -> "${Formatter.formatFileSize(context, model.downloadedBytes)}  |  $total"
    }
}
