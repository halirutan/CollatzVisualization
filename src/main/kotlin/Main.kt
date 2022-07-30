@file:Suppress("FunctionName")

import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

//@Composable
//fun CollatzPanel() {
//    // initialize the graph
//    val numberOfNodes = 1000
//    val maxNumberValue = 1_000_00L
//    val nodes = List(numberOfNodes) { (2L..maxNumberValue).random() }
//    val graph = CollatzGraph()
//    graph.addNodes(nodes)
//
//    val graphStyle = GraphStyle()
//    MaterialTheme {
//        val (lines, viewRect) = calculateLines(graph, graphStyle)
//        CollatzCanvas(lines, viewRect)
//    }
//}

fun main() = application {
    // initialize the graph
    val numberOfNodes = 100
    val maxNumberValue = 1_000_00L
    val nodes = List(numberOfNodes) { (2L..maxNumberValue).random() }
    val graph = CollatzGraph()
    graph.addNodes(nodes)

    var style by remember { mutableStateOf(GraphStyle()) }
    var graphLines by remember { mutableStateOf(calculateLines(graph, style)) }
    Window(onCloseRequest = ::exitApplication) {
        MaterialTheme {
            Row {
                SettingsPanel(style) { style = it; graphLines = calculateLines(graph, style) }
                CollatzCanvas(graphLines.lines, graphLines.viewRect)
            }
        }
    }
}
