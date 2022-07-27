import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.withTransform
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
        red = red,
        green = green,
        blue = blue
    )
}

/**
 * Provides access to styling the rendered graph and allows for both adjusting the colors and the length and angle
 * of edges.
 */
data class GraphStyle(
    val edgeLengthGamma: Double = 1.0, //1.24,
    val angleAmplifier: Double = 0.29,
    val angleShift: Double = 0.7,
    val colorDirection: Double = 2.4,
    val colorRotations: Double = 1.0, //2.18,
    val colorSaturationAmplitude: Double = 1.815,
    val colorIntensityGamma: Double = 1.6, //1.64,
    val colorSpeedGamma: Double = 0.01, //0.04,
    val strokeWidthFactor: Double = 0.38,
    val strokeWidthGamma: Double = 0.04
)

/**
 * Helper class since the line calculation is already pretty intricate and I don't want to mix it with
 * Compose drawing calls
 */
data class StyledLine(
    val startOffset: Offset,
    val endOffset: Offset,
    val strokeWidth: Float,
    val color: Color
)

data class ViewData(
    val lines: List<StyledLine>,
    val viewRect: Rect
)

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
        //  val t = ((node.id - 1.0) / maxNodeValue).pow(style.colorSpeedGamma)
        val t = 1.0 - ((node.count - 1.0) / g.root.count).pow(style.colorSpeedGamma)
        val strokeWidth = style.strokeWidthFactor * ((node.count.toDouble() / g.root.count).pow(style.strokeWidthGamma))
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

@Composable
fun collatzCanvas(lines: List<StyledLine>, viewRect: Rect) = Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val viewMatrix = calculateViewMatrix(viewRect, width, height)

        withTransform(
            {
                transform(viewMatrix)
            }) {
            for (l in lines) {
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


/**
 * Calculates the view matrix that centers the graph on the canvas.
 */
private fun calculateViewMatrix(
    viewRect: Rect,
    width: Float,
    height: Float
): Matrix {
    val maxSize = max(viewRect.width, viewRect.height)
    val (left, bottom) = viewRect.bottomLeft
    val viewMatrix = floatArrayOf(
        width / maxSize, 0f, 0f, 0f,
        0f, -height / maxSize, 0f, 0f,
        0f, 0f, 1f, 0f,
        -left * width / viewRect.width, bottom * height / viewRect.height, 0f, 1f
    )
    return Matrix(viewMatrix)
}

