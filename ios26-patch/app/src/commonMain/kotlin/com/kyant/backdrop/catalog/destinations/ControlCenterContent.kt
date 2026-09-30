package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.catalog.GlassShowcaseScaffold
import com.kyant.backdrop.catalog.components.Ios26Switch
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.RoundedRectangle

@Composable
fun ControlCenterContent() {
    GlassShowcaseScaffold { backdrop ->
        var wifi by remember { mutableStateOf(true) }
        var bluetooth by remember { mutableStateOf(false) }
        var airplane by remember { mutableStateOf(false) }
        var battery by remember { mutableStateOf(true) }

        Column(
            Modifier.fillMaxWidth().padding(24f.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20f.dp)
        ) {
            BasicText("Control Center", style = TextStyle(Color.White, 22.sp, FontWeight.SemiBold))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14f.dp)
            ) {
                ToggleTile("飞行模式", airplane, backdrop) { airplane = it }
                ToggleTile("Wi-Fi", wifi, backdrop) { wifi = it }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14f.dp)
            ) {
                ToggleTile("蓝牙", bluetooth, backdrop) { bluetooth = it }
                ToggleTile("电池", battery, backdrop) { battery = it }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(52f.dp)
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedRectangle(18f.dp) },
                        effects = {
                            vibrancy()
                            blur(6f.dp.toPx())
                            lens(12f.dp.toPx(), 20f.dp.toPx())
                        },
                        highlight = { Highlight.Default },
                        onDrawSurface = {
                            drawRect(Color(0xFF007AFF).copy(alpha = 0.35f))
                        }
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicText(
                    "☀️ 亮度",
                    Modifier.padding(start = 18f.dp),
                    style = TextStyle(Color.White, 16.sp, FontWeight.Medium)
                )
                BasicText(
                    "65%",
                    Modifier.padding(end = 18f.dp).align(Alignment.CenterEnd),
                    style = TextStyle(Color.White.copy(alpha = 0.8f), 14.sp)
                )
            }
        }
    }
}

@Composable
private fun ToggleTile(
    label: String,
    checked: Boolean,
    backdrop: Backdrop,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        Modifier
            .weight(1f)
            .height(72f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(20f.dp) },
                effects = {
                    vibrancy()
                    blur(6f.dp.toPx())
                    lens(12f.dp.toPx(), 20f.dp.toPx(), depthEffect = true)
                },
                highlight = { Highlight.Default },
                onDrawSurface = {
                    drawRect(
                        if (checked) Color(0xFF34C759).copy(alpha = 0.35f)
                        else Color.White.copy(alpha = 0.1f)
                    )
                }
            )
            .padding(12f.dp)
    ) {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            BasicText(
                label,
                style = TextStyle(Color.White, 14.sp, FontWeight.Medium)
            )
            Box(
                Modifier.align(Alignment.End)
            ) {
                Ios26Switch(checked, onCheckedChange, backdrop)
            }
        }
    }
}