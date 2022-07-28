import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class CollatzTest {

    @Test
    fun astroIntensityTest() {
        val col = astroIntensity(0.1, 2.49, 0.76, 1.815, 1.3)
        assertEquals(0f, col.red)
        assertEquals(0.0627451f, col.green)
        assertEquals(0.12156863f, col.blue)
    }

    @Test
    fun testSimpleGraphHasCorrectNodeDepths() {
        val g = CollatzGraph()
        g.addNodes(listOf(256))
        var depth = 0
        g.visitGraph({
            assertEquals(depth++, it.depth)
        })
    }

    @Test
    fun testCorrectSimpleGraph() {
        val values = mutableListOf<Long>(1, 2, 4, 8, 16, 5, 10, 3, 6, 12, 24)
        val g = CollatzGraph()
        g.addNodes(listOf(24))
        g.visitGraph({
            assertEquals(values.first(), it.id)
            values.removeAt(0)
        })
    }

    @Test
    fun testViewMatrixMapping() {
        val width = 640f
        val height = 480f
        val viewRect = Rect(-1f, -1f, 1f, 1f)
        val m = calculateViewMatrix(viewRect, width, height)

        m.map(Offset(0f, 0f)).also { (x, y) ->
            assertEquals(width / 2f, x)
            assertEquals(height / 2f, y)
        }

        m.map(Offset(-1f, 0f)).also { (x, y) ->
            assertEquals(0f, x)
            assertEquals(height / 2f, y)
        }

        m.map(Offset(0f, -1f)).also { (x, y) ->
            assertEquals(width / 2f, x)
            assertEquals(height, y)
        }

        m.map(Offset(1f, 1f)).also { (x, y) ->
            assertEquals(width, x)
            assertEquals(0f, y)
        }
    }
}