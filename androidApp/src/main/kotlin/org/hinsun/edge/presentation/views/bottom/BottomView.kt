package org.hinsun.edge.presentation.views.bottom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.hinsun.edge.presentation.ui.FloatingAssistant
import org.hinsun.edge.presentation.ui.OrbOverlay
import org.hinsun.edge.presentation.ui.preloadEnergyOrbV4
import org.hinsun.edge.presentation.views.download.DownloadView
import org.hinsun.edge.presentation.views.ml.MLView
import org.hinsun.edge.presentation.views.navgraph.EdgeAINavGraph
import org.hinsun.edge.presentation.views.navgraph.EdgeAINavRoute
import org.hinsun.edge.presentation.views.navgraph.edgeAiConfig
import org.hinsun.edge.presentation.views.session.SessionView
import org.hinsun.edge.presentation.views.waveform.AudioWaveformView
import org.hinsun.edge.ui.NotFoundView

private val configuration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(BottomNavRoute.Session::class, BottomNavRoute.Session.serializer())
            subclass(BottomNavRoute.Waveform::class, BottomNavRoute.Waveform.serializer())
            subclass(BottomNavRoute.Download::class, BottomNavRoute.Download.serializer())
            subclass(BottomNavRoute.ML::class, BottomNavRoute.ML.serializer())
        }
    }
}

@Composable
fun BottomView() {
    val navBackStack = rememberNavBackStack(
        configuration = configuration,
        BottomNavRoute.Session
    )

    var showOrb by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // Parse the orb animation in the background so tapping the mascot opens it instantly.
    LaunchedEffect(Unit) { preloadEnergyOrbV4(context) }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    BottomNavRoute.routes.forEach { key ->
                        NavigationBarItem(
                            selected = navBackStack.lastOrNull() == key,
                            onClick = {
                                if (navBackStack.lastOrNull() != key) {
                                    navBackStack.clear()
                                    navBackStack.add(key)
                                }
                            },
                            icon = { Icon(key.icon, contentDescription = key.label) },
                            label = { Text(key.label) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavDisplay(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                backStack = navBackStack,
                onBack = { navBackStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = { key ->
                    when (key) {
                        is BottomNavRoute.Session -> NavEntry(key) { SessionView() }
                        is BottomNavRoute.Waveform -> NavEntry(key) { AudioWaveformView() }
                        is BottomNavRoute.Download -> NavEntry(key) { DownloadView() }
                        is BottomNavRoute.ML -> NavEntry(key) { MLView() }
                        else -> NavEntry(key) { NotFoundView() }
                    }
                }
            )
        }

        if (!showOrb) {
            FloatingAssistant(size = 48.dp) { showOrb = true }
        } else {
            // Orb v4 (dotLottie) like iOS; pass `orb = { EnergyOrbShader(it) }` for the v1 shader.
            OrbOverlay { showOrb = false }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppAndroidPreview() {
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