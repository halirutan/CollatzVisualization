@file:Suppress("FunctionName")

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * Color gradient based on the publication
 * ["A colour scheme for the display of astronomical intensity images"](https://astron-soc.in/bulletin/11June/289392011.pdf).
 * It uses a squashed helix around the diagonal (the gray line) of the color cube.
 * @param l lambda in the range of [0, 1] that corresponds to how far we travelled along the helix
 * @param s direction of the predominant colour deviation from black at the start of the colour scheme in range [0, 2Pi]
 * @param r number of rotations that are made around the diagonal in the color cube
 * @param h amplitude that controls the saturation i.e. how much to deviate from the gray diagonal
 * @param g gamma factor to emphasize low intensity values (g < 1.0) or high intensity values (g > 1.0)
 */
fun astroIntensity(l: Double, s: Double, r: Double, h: Double, g: Double): Color {
    val lg = l.pow(g)
    // The next 2 lines are the expressions for psi and a in the paper on page 292 right after eq. (2)
    val psi = 2.0 * Math.PI * (s / 3.0 + r * l)
    val a = h * lg * (1.0 - lg) / 2.0

    // The rest is the expanded matrix multiplication of eq. (2)
    val cosPsi = cos(psi)
    val sinPsi = sin(psi)
    val red = (lg + a * (-0.14861 * cosPsi + 1.78277 * sinPsi)).coerceAtLeast(0.0).coerceAtMost(1.0).toFloat()
    val green = (lg + a * (-0.29227 * cosPsi - 0.90649 * sinPsi)).coerceAtLeast(0.0).coerceAtMost(1.0).toFloat()
    val blue = (lg + a * (1.97294 * cosPsi)).coerceAtLeast(0.0).coerceAtMost(1.0).toFloat()
    return Color(
        red = red, green = green, blue = blue
    )
}

/**
 * Provides access to styling the rendered graph and allows for both adjusting the colors and the length and angle
 * of edges.
 */
data class GraphStyle(
    // Properties for adjusting the lines
    val angleShift: Double = 0.71,
    val angleAmplifier: Double = 0.29,
    val edgeLengthGamma: Double = 1.11,

    // Properties for how lines are stroked
    val strokeMinValue: Double = 0.1,
    val strokeWidthFactor: Double = 0.27, //0.38,
    val strokeWidthGamma: Double = 0.81, //0.04

    // Properties for adjust line colors
    val colorDirection: Double = 1.59,
    val colorRotations: Double = 1.19,
    val colorSaturationAmplitude: Double = 1.0,
    val colorIntensityGamma: Double = 0.93,
    val colorSpeedGamma: Double = 0.42,

    )

/**
 * Helper class since the line calculation is already pretty intricate and I don't want to mix it with
 * Compose drawing calls
 */
data class StyledLine(
    val startOffset: Offset, val endOffset: Offset, val strokeWidth: Float, val color: Color
)

/**
 * Wrapper to encapsulate both the styled lines to be drawn as well as the viewing-rectangle that
 * contains the maximum coordinates of the graph.
 */
data class ViewData(
    val lines: List<StyledLine>, val viewRect: Rect
)

/**
 * Turns a graph and a graph-style into the actual line segments.
 * It visits all nodes in the Collatz graph starting at the root node 1 and from node to node it makes a small
 * line segment.
 * Depending on if the node has an even or uneven ID, the next line will be angled differently.
 * While this is basically the gist, there are quite some parameters that styled along the way.
 * E.g.
 *
 * - the length of the line depends on the node number
 * - the stroke style will depend on the depth of the node in the tree
 * - the color of the line will also depend on the nodes properties
 */
fun calculateLines(g: CollatzGraph, style: GraphStyle): ViewData {
    var xMin = Float.MAX_VALUE
    var yMin = Float.MAX_VALUE
    var xMax = Float.MIN_VALUE
    var yMax = Float.MIN_VALUE

    val result = mutableListOf<StyledLine>()
    val angles = mutableMapOf(0L to 0.0)
    val positions = mutableMapOf(0L to Offset(0f, 0f))
    g.visitGraph({ node ->
        // Calculate new point that connects the parent node with the current one.
        val parentId = node.parent.id
        val prevAngle = angles[parentId] ?: throw RuntimeException("Angle for ID $parentId doesn't exist")
        val prevPosition = positions[parentId] ?: throw RuntimeException("Position for ID $parentId doesn't exist")
        val r = node.id / (1.0 + node.id.toDouble().pow(style.edgeLengthGamma))
        val phi = prevAngle + style.angleAmplifier * (style.angleShift - 2.0 * node.id.mod(2))
        val position = prevPosition + Offset((r * cos(phi)).toFloat(), (r * sin(phi)).toFloat())

        // Update size of view rect
        xMin = min(xMin, position.x)
        xMax = max(xMax, position.x)
        yMin = min(yMin, position.y)
        yMax = max(yMax, position.y)

        // Store values for current node since it's needed for calculating the next node
        angles[node.id] = phi
        positions[node.id] = position


        val scaledDepth = node.depth.toDouble() / g.maxNodeValue()
        val t = scaledDepth.pow(style.colorSpeedGamma)
        val strokeWidth = max(
            style.strokeMinValue, style.strokeWidthFactor * (1.0 - scaledDepth.pow(style.strokeWidthGamma))
        )
        result.add(
            StyledLine(
                startOffset = prevPosition,
                endOffset = position,
                strokeWidth = strokeWidth.toFloat(),
                color = astroIntensity(
                    t,
                    style.colorDirection,
                    style.colorRotations,
                    style.colorSaturationAmplitude,
                    style.colorIntensityGamma
                )
            )
        )
    })
    // Pay attention: The definition of Rect top and bottom is not what you expect.
    return ViewData(result, Rect(xMin, yMin, xMax, yMax))
}

/**
 * Canvas that just renders the styled line primitives.
 */
@Composable
fun CollatzCanvas(graph: CollatzGraph, style: GraphStyle) {
    val viewData = calculateLines(graph, style)
    Canvas(modifier = Modifier.size(1000.dp).padding(60.dp)) {
        val viewMatrix = calculateViewMatrix(viewData.viewRect, size.width, size.height)
        withTransform({
            transform(viewMatrix)
        }) {
            viewData.lines.forEach { l ->
                drawLine(
                    start = l.startOffset,
                    end = l.endOffset,
                    strokeWidth = l.strokeWidth,
                    color = l.color,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Calculates the view matrix that centers the graph on the canvas.
 * The matrix uses [homogeneous coordinates](https://en.wikipedia.org/wiki/Homogeneous_coordinates) to create a mapping
 * from the coordinates of the graph vertices to pixel coordinates.
 * The trick with homogeneous coordinates is that it uses a 3x3 (or in that case 4x4) matrix to represent
 * transformations in 2D because with a 2x2 matrix it would not be possible to represent translations (i.e. moving
 * the coordinate system).
 *
 * The mapping will keep the aspect ratio of the graph so that it's not deformed.
 *
 * @param viewRect Provides the rectangle (min and max coordinates) for the graph vertices.
 * @param width Width of the canvas
 * @param height Height of the canvas
 */
fun calculateViewMatrix(
    viewRect: Rect, width: Float, height: Float
): Matrix {
    // To keep the aspect ratio of the graph, we use uniform scaling in both x and y direction
    val l = max(viewRect.width, viewRect.height)
    val scale = (min(width, height)) / l

    // Calculate the necessary shifts to center the graph on the canvas
    val left = viewRect.center.x - l / 2f
    val bottom = viewRect.center.y + l / 2f
    // Corrections to align the graph in the center and not at the border
    val leftCorrection = max(0f, width - height) / 2f
    val bottomCorrection = max(0f, height - width) / 2f
    val matrixValues = floatArrayOf(
        scale,
        0f,
        0f,
        0f,
        0f,
        -scale,
        0f,
        0f,
        0f,
        0f,
        1f,
        0f,
        -left * scale + leftCorrection,
        bottom * scale + bottomCorrection,
        0f,
        1f
    )
    return Matrix(matrixValues)
}

