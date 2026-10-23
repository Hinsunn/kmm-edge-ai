package org.hinsun.edge

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withCreated
import androidx.navigation3.runtime.rememberNavBackStack
import kotlinx.coroutines.launch
import org.hinsun.edge.core.managers.LiteRTLMModelDownloadManager
import org.hinsun.edge.presentation.views.navgraph.EdgeAINavGraph
import org.hinsun.edge.presentation.views.navgraph.EdgeAINavRoute
import org.hinsun.edge.presentation.views.navgraph.edgeAiConfig
import org.pytorch.executorch.EValue
import org.pytorch.executorch.Tensor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        LiteRTLMModelDownloadManager.init(applicationContext)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent { App() }
        // setContent {
        //     val navBackStack = rememberNavBackStack(
        //         configuration = edgeAiConfig,
        //         EdgeAINavRoute.Bottom,
        //     )
        //     MaterialTheme {
        //         EdgeAINavGraph(
        //             modifier = Modifier,
        //             navBackStack = navBackStack,
        //         )
        //     }
        // }

        lifecycleScope.launch {
            lifecycle.withCreated {
                nativeRunner()
            }
        }
    }

    private fun nativeRunner() {
        val shape = longArrayOf(2, 2)

        val a = Tensor.fromBlob(floatArrayOf(1f, 2f, 3f, 4f), shape)
        val b = Tensor.fromBlob(floatArrayOf(5f, 6f, 7f, 8f), shape)

        val dataA = a.dataAsFloatArray
        val dataB = b.dataAsFloatArray

        val result = FloatArray(dataA.size) { i ->
            dataA[i] * dataB[i]
        }

        val c = Tensor.fromBlob(result, shape)
        println("Matrix: ${c.dataAsFloatArray.contentToString()}")
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