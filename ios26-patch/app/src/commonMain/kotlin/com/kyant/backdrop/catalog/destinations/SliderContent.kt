package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.catalog.GlassShowcaseScaffold
import com.kyant.backdrop.catalog.components.Ios26Slider

@Composable
fun SliderContent() {
    GlassShowcaseScaffold { backdrop ->
        var brightness by remember { mutableFloatStateOf(0.65f) }
        var volume by remember { mutableFloatStateOf(0.4f) }
        var temperature by remember { mutableFloatStateOf(0.55f) }

        Column(
            Modifier.fillMaxWidth().padding(horizontal = 32f.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28f.dp)
        ) {
            BasicText("Slider", style = TextStyle(Color.White, 24.sp))

            SliderRow("Brightness", brightness, backdrop) { brightness = it }
            SliderRow("Volume", volume, backdrop) { volume = it }
            SliderRow("Temperature", temperature, backdrop) { temperature = it }
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    backdrop: com.kyant.backdrop.Backdrop,
    onValueChange: (Float) -> Unit
) {
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8f.dp)
    ) {
        BasicText(
            label,
            style = TextStyle(Color.White.copy(alpha = 0.8f), 14.sp)
        )
        Ios26Slider(value = { value }, onValueChange = onValueChange, backdrop = backdrop)
    }
}