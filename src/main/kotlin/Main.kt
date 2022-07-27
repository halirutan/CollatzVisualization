import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application


@Composable
fun app() {
    val numberOfNodes = 1000
    val maxNumberValue = 1_000_000L
    val nodes = List(numberOfNodes) { (2L..maxNumberValue).random()}

    val graphStyle = GraphStyle()
    val graph = CollatzGraph()
    graph.addNodes(nodes)
    MaterialTheme {
        val (lines, viewRect) = calculateLines(graph, graphStyle)
        collatzCanvas(lines, viewRect)
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        app()
    }
}
