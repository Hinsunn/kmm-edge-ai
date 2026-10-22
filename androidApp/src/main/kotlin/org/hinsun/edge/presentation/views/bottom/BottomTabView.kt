package org.hinsun.edge.presentation.views.bottom

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.filled.Token
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import kotlinx.serialization.Serializable
import org.hinsun.edge.presentation.views.navgraph.EdgeAINavGraph
import org.hinsun.edge.presentation.views.navgraph.EdgeAINavRoute
import org.hinsun.edge.presentation.views.navgraph.edgeAiConfig

@Serializable
sealed interface BottomNavRoute : NavKey {

    val icon: ImageVector
    val label: String

    @Serializable
    data object Session : BottomNavRoute, NavKey {
        override val icon: ImageVector = Icons.Filled.SpaceDashboard
        override val label: String = "Session"
    }

    @Serializable
    data object Waveform : BottomNavRoute, NavKey {
        override val icon: ImageVector = Icons.Filled.Assessment
        override val label: String = "Waveform"
    }

    @Serializable
    data object Download : BottomNavRoute, NavKey {
        override val icon: ImageVector = Icons.Filled.CloudDownload
        override val label: String = "Download"
    }

    @Serializable
    data object ML : BottomNavRoute, NavKey {
        override val icon: ImageVector = Icons.Filled.Token
        override val label: String = "MLs"
    }

    @Serializable
    data object KMMApp : BottomNavRoute, NavKey {
        override val icon: ImageVector = Icons.Filled.ViewAgenda
        override val label: String = "KMM App"
    }

    companion object {
        val routes = listOf(Session, Waveform, Download, ML, KMMApp)
    }
}


@Preview(showBackground = true)
@Composable
fun BottomNavRoutePreview() {
    val navBackStack = rememberNavBackStack(
        configuration = edgeAiConfig,
        EdgeAINavRoute.Bottom,
    )

    MaterialTheme {
        EdgeAINavGraph(
            modifier = Modifier,
            navBackStack = navBackStack,
        )
    }
}