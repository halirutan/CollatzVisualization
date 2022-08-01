@file:Suppress("FunctionName")

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A simple slider that allows to set a [minValue] and [maxValue], shows a [label] and uses
 * [state hoisting](https://developer.android.com/jetpack/compose/state#state-hoisting) to communicate changes
 * to the caller.
 */
@Composable
fun PropertySlider(initialValue: Double, minValue: Double, maxValue: Double, label: String, onChange: (Double) -> Unit) {
    var value by remember { mutableStateOf(initialValue.toFloat()) }
    Column(
        modifier =
        Modifier
            .padding(5.dp)

    ) {
        Row {
            Text(text = "$label: ", fontSize = 10.sp)
            Text(text = "%.2f".format(value), fontSize = 10.sp)
        }
        Slider(
            value = value,
            valueRange = minValue.toFloat()..maxValue.toFloat(),
            onValueChange = { value = it; onChange(it.toDouble()) })
    }
}

/**
 * A panel that shows sliders for all values of [GraphStyle].
 */
@Composable
fun SettingsPanel(style: GraphStyle, onValueChange: (GraphStyle) -> Unit) {
    LazyColumn(Modifier.width(300.dp)) {
        item { Text("Graph Settings",
            color = MaterialTheme.colors.primaryVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 10.dp)
            ) }
        item {
            PropertySlider(
                style.angleShift,
                0.5,
                0.8,
                "Angle Shift"
            ) { newValue -> onValueChange(style.copy(angleShift = newValue)) }
        }

        item {
            PropertySlider(
                style.angleAmplifier,
                0.2,
                0.5,
                "Angle Amplifier"
            ) { newValue -> onValueChange(style.copy(angleAmplifier = newValue)) }
        }
        item {
            PropertySlider(
                style.edgeLengthGamma,
                0.95,
                2.0,
                "Edge-length Gamma"
            ) { newValue -> onValueChange(style.copy(edgeLengthGamma = newValue)) }
        }

        item {
            Text("Stroke Settings",
                color = MaterialTheme.colors.primaryVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }
        item {
            PropertySlider(
                style.strokeMinValue,
                0.0,
                0.1,
                "Minimum Stroke Width"
            ) { newValue -> onValueChange(style.copy(strokeMinValue = newValue)) }
        }
        item {
            PropertySlider(
                style.strokeWidthFactor,
                0.0,
                0.5,
                "Stroke Width Factor"
            ) { newValue -> onValueChange(style.copy(strokeWidthFactor = newValue)) }
        }
        item {
            PropertySlider(
                style.strokeWidthGamma,
                0.5,
                1.5,
                "Stroke Width Gamma"
            ) { newValue -> onValueChange(style.copy(strokeWidthGamma = newValue)) }
        }
        item { Text("Color Settings",
            color = MaterialTheme.colors.primaryVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 10.dp)
        ) }

        item {
            PropertySlider(
                style.colorDirection,
                0.0,
                3.14,
                "Initial Color Direction"
            ) { newValue -> onValueChange(style.copy(colorDirection = newValue)) }
        }
        item {
            PropertySlider(
                style.colorRotations,
                0.0,
                3.0,
                "Number of Color Rotations"
            ) { newValue -> onValueChange(style.copy(colorRotations = newValue)) }
        }
        item {
            PropertySlider(
                style.colorSaturationAmplitude,
                0.0,
                1.0,
                "Color Saturation"
            ) { newValue -> onValueChange(style.copy(colorSaturationAmplitude = newValue)) }
        }
        item {
            PropertySlider(
                style.colorIntensityGamma,
                0.5,
                1.5,
                "Color Intensity Gamma"
            ) { newValue -> onValueChange(style.copy(colorIntensityGamma = newValue)) }
        }
        item {
            PropertySlider(
                style.colorSpeedGamma,
                0.1,
                1.5,
                "Color Speed Gamma"
            ) { newValue -> onValueChange(style.copy(colorSpeedGamma = newValue)) }
        }
    }
}