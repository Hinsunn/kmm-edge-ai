package org.hinsun.edge.presentation.views.bottom

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backpack
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Hexagon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface BottomNavRoute : NavKey {

    val icon: ImageVector
    val label: String

    @Serializable
    data object Session : BottomNavRoute, NavKey {
        override val icon: ImageVector = Icons.Outlined.Hexagon
        override val label: String = "Session"
    }

    @Serializable
    data object Waveform : BottomNavRoute, NavKey {
        override val icon: ImageVector = Icons.Outlined.GraphicEq
        override val label: String = "Waveform"
    }

    @Serializable
    data object Download : BottomNavRoute, NavKey {
        override val icon: ImageVector = Icons.Outlined.Hexagon
        override val label: String = "Download"
    }

    @Serializable
    data object ML : BottomNavRoute, NavKey {
        override val icon: ImageVector = Icons.Outlined.Hexagon
        override val label: String = "MLs"
    }

    companion object {
        val routes = listOf(Session, Waveform, Download, ML)
    }
}
