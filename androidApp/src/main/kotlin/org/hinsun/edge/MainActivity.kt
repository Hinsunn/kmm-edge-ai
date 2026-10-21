package org.hinsun.edge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.rememberNavBackStack
import org.hinsun.edge.core.managers.LiteRTLMModelDownloadManager
import org.hinsun.edge.presentation.views.navgraph.EdgeAINavGraph
import org.hinsun.edge.presentation.views.navgraph.EdgeAINavRoute
import org.hinsun.edge.presentation.views.navgraph.edgeAiConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        LiteRTLMModelDownloadManager.init(applicationContext)

        setContent {
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
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
)
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