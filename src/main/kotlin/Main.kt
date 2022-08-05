@file:Suppress("FunctionName")

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.*

/**
 *  Number of random numbers.
 *  For each the Collatz sequence will be calculated and displayed.
 *  - 100-200 nodes is usually fast on modern computers
 *  - 1000 is demanding, but still OK
 *  - 10000 you should expect some lags
 */
const val numberOfNodes = 1000

/**
 * Random numbers will be drawn from the range 2 to [maxNumberValue]
 */
const val maxNumberValue = 10_000_000L

/**
 * Draw random numbers and calculates the Collatz graph for them
 */
suspend fun initializeGraph(
    graph: MutableState<CollatzGraph>,
    progress: MutableState<Double>,
    isDone: MutableState<Boolean>
    ) {
    withContext(Dispatchers.Default) {
        isDone.value = false
        val nodes = List(numberOfNodes) { (2L..maxNumberValue).random() }
        graph.value.addNodes(nodes) { p -> progress.value = p}
        isDone.value = true
    }
}

fun main() = application {
    val appScope = rememberCoroutineScope()
    val progress = remember { mutableStateOf(0.0) }
    val isDone = remember { mutableStateOf(false) }
    val graph = remember { mutableStateOf(CollatzGraph()) }
    appScope.launch {
        initializeGraph(graph, progress, isDone)
    }
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(width = 1500.dp, height = 1000.dp),
        title = "Collatz Visualization",
        icon = painterResource("Logo.png")
    ) {
        MaterialTheme {
            // It doesn't really makes sense to have a loading screen for graphs < 1000 nodes, but whatever.
            if (!isDone.value) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        progress = progress.value.toFloat(), modifier = Modifier.size(200.dp)
                    )
                    Text("Loading Graph")
                }
            } else {
                var style by remember { mutableStateOf(GraphStyle()) }
                Row(
                    Modifier.padding(5.dp)
                ) {
                    SettingsPanel(style) { style = it }
                    CollatzCanvas(graph.value, style)
                }
            }
        }
    }
}
