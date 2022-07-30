@file:Suppress("FunctionName")

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun PropertySlider(v: Double, minValue: Double, maxValue: Double, name: String, onChange: (Double) -> Unit) {
    var value by remember { mutableStateOf(v.toFloat()) }
    Column(modifier =
        Modifier
            .padding(5.dp)

    ) {
        Row {
            Text(text = "$name ", fontSize = 8.sp)

            Text(text = "%.2f".format(value), fontSize = 8.sp)
        }
        Slider(
            value = value,
            valueRange = minValue.toFloat()..maxValue.toFloat(),
            onValueChange = { value = it; onChange(it.toDouble()) })
    }
}

@Composable
fun SettingsPanel(style: GraphStyle, onValueChange: (GraphStyle) -> Unit) {
    Column(
        Modifier
            .width(300.dp)
    ) {
        Text("Graph Settings")
        PropertySlider(
            style.angleShift,
            0.5,
            0.8,
            "Angle Shift"
        ) { newValue -> onValueChange(style.copy(angleShift = newValue)) }

        PropertySlider(
            style.angleAmplifier,
            0.2,
            0.5,
            "Angle Amplifier"
        ) { newValue -> onValueChange(style.copy(angleAmplifier = newValue)) }

        PropertySlider(
            style.edgeLengthGamma,
            0.95,
            2.0,
            "Edge-length Gamma"
        ) { newValue -> onValueChange(style.copy(edgeLengthGamma = newValue)) }


        Text("Stroke Settings")
        PropertySlider(
            style.strokeMinValue,
            0.0,
            0.1,
            "Minimum Stroke Width"
        ) { newValue -> onValueChange(style.copy(strokeMinValue = newValue)) }

        PropertySlider(
            style.strokeWidthFactor,
            0.0,
            0.5,
            "Stroke Width Factor"
        ) { newValue -> onValueChange(style.copy(strokeWidthFactor = newValue)) }

        PropertySlider(
            style.strokeWidthGamma,
            0.5,
            1.5,
            "Stroke Width Gamma"
        ) { newValue -> onValueChange(style.copy(strokeWidthGamma = newValue)) }

        Text("Color Settings")
        PropertySlider(
            style.colorDirection,
            0.0,
            3.14,
            "Initial Color Direction"
        ) { newValue -> onValueChange(style.copy(colorDirection = newValue)) }

        PropertySlider(
            style.colorRotations,
            0.0,
            3.0,
            "Number of Color Rotations"
        ) { newValue -> onValueChange(style.copy(colorRotations = newValue)) }

        PropertySlider(
            style.colorSaturationAmplitude,
            0.0,
            1.0,
            "Color Saturation"
        ) { newValue -> onValueChange(style.copy(colorSaturationAmplitude = newValue)) }

        PropertySlider(
            style.colorIntensityGamma,
            0.5,
            1.5,
            "Color Intensity Gamma"
        ) { newValue -> onValueChange(style.copy(colorIntensityGamma = newValue)) }

        PropertySlider(
            style.colorSpeedGamma,
            0.1,
            1.5,
            "Color Speed Gamma"
        ) { newValue -> onValueChange(style.copy(colorSpeedGamma = newValue)) }

    }
}