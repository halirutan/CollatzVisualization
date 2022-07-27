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
    fun testSimpleGraphHasCorrectNodeCount() {
        val g = CollatzGraph()
        g.addNodes(listOf(256))
        g.visitGraph({
            if (it.id == 1L) {
                assertEquals(2, it.count)
            } else {
                assertEquals(1, it.count)
            }
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
}