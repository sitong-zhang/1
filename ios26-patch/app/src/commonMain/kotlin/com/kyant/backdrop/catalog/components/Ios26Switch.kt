package com.kyant.backdrop.catalog.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.Capsule

@Composable
fun Ios26Switch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val fraction = remember { Animatable(if (checked) 1f else 0f) }
    LaunchedEffect(checked) {
        fraction.animateTo(
            if (checked) 1f else 0f,
            spring(1f, 900f, 0.001f)
        )
    }

    val thumbSize = 30f.dp
    val trackWidth = 62f.dp
    val trackHeight = 36f.dp
    val travelPx = with(density) { (trackWidth - thumbSize).toPx() - 6.dp.toPx() }

    Box(
        modifier
            .semantics { role = Role.Switch }
            .size(trackWidth, trackHeight)
            .pointerInput(checked) {
                detectTapGestures {
                    onCheckedChange(!checked)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            Modifier
                .size(trackWidth, trackHeight)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = {
                        blur(4f.dp.toPx())
                        lens(6f.dp.toPx(), 12f.dp.toPx())
                    },
                    onDrawSurface = {
                        drawRect(
                            if (checked) Color(0xFF34C759) else Color(0xFF787878).copy(alpha = 0.3f)
                        )
                    }
                )
        )

        Box(
            Modifier
                .align(Alignment.CenterStart)
                .graphicsLayer {
                    translationX = 3f.dp.toPx() + fraction.value * travelPx
                }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = {
                        blur(2f.dp.toPx())
                        lens(6f.dp.toPx(), 8f.dp.toPx(), chromaticAberration = true)
                    },
                    highlight = { Highlight.Default },
                    onDrawSurface = {
                        drawRect(Color.White.copy(alpha = 0.85f))
                    }
                )
                .size(thumbSize)
        )
    }
}