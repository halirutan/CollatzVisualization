@file:Suppress("FunctionName")

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState


fun main() = application {
    // initialize the graph
    val numberOfNodes = 200
    val maxNumberValue = 1_000_000L
    val nodes = List(numberOfNodes) { (2L..maxNumberValue).random() }
    val graph = CollatzGraph()
    graph.addNodes(nodes)

    var style by remember { mutableStateOf(GraphStyle()) }
    var graphLines by remember { mutableStateOf(calculateLines(graph, style)) }
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(width = 1500.dp, height = 1000.dp),
        title = "Collatz Visualization",
        icon = painterResource("Logo.png")
    )
    {
        MaterialTheme {
            Row(
                Modifier
                    .padding(5.dp)
            ) {
                SettingsPanel(style) { style = it; graphLines = calculateLines(graph, style) }
                CollatzCanvas(graphLines.lines, graphLines.viewRect)
            }
        }
    }
}
