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
import androidx.compose.runtime.mutableFloatStateOf
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
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.RoundedRectangle

@composable
fun PlaygroundContent() {
    GlassShowcaseScaffold { backdrop ->
        var heavy by remember { mutableFloatStateOf(1f) }

        Column(
            Modifier.fillMaxWidth().padding(24f.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20f.dp)
        ) {
            BasicText("Glass Playground", style = TextStyle(Color.White, 22.sp, FontWeight.SemiBold))

            GlassPanel(
                "浅折射",
                backdrop,
                refraction = 8f,
                refractionAmount = 14f,
                chromatic = false
            )
            GlassPanel(
                "深折射 + 色散",
                backdrop,
                refraction = 16f,
                refractionAmount = 30f,
                chromatic = true
            )
            GlassPanel(
                "强模糊",
                backdrop,
                refraction = 6f,
                refractionAmount = 10f,
                chromatic = false,
                heavyBlur = true
            )
        }
    }
}

@Composable
private fun GlassPanel(
    title: String,
    backdrop: Backdrop,
    refraction: Float,
    refractionAmount: Float,
    chromatic: Boolean,
    heavyBlur: Boolean = false
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(96f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(24f.dp) },
                effects = {
                    vibrancy()
                    if (heavyBlur) blur(14f.dp.toPx()) else blur(4f.dp.toPx())
                    lens(
                        refraction.dp.toPx(),
                        refractionAmount.dp.toPx(),
                        chromaticAberration = chromatic,
                        depthEffect = true
                    )
                },
                highlight = { Highlight.Default }
            )
            .padding(16f.dp)
    ) {
        Column(
            Modifier.align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(2f.dp)
        ) {
            BasicText(
                title,
                style = TextStyle(Color.White, 16.sp, FontWeight.SemiBold)
            )
            BasicText(
                if (chromatic) "色散折射" else if (heavyBlur) "磨砂玻璃" else "液态折射",
                style = TextStyle(Color.White.copy(alpha = 0.65f), 12.sp)
            )
        }
        BasicText(
            "IOS26",
            Modifier.align(Alignment.TopEnd),
            style = TextStyle(Color.White.copy(alpha = 0.5f), 12.sp, FontWeight.Bold)
        )
    }
}