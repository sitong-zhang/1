package com.kyant.backdrop.catalog.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.Capsule
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun Ios26Slider(
    value: () -> Float,
    onValueChange: (Float) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val accent = Color(0xFF007AFF)
    val progress = remember { Animatable(value()) }

    LaunchedEffect(Unit) {
        snapshotFlow { value() }
            .distinctUntilChanged()
            .collect { target ->
                progress.animateTo(target, androidx.compose.animation.core.spring(dampingRatio = 0.7f))
            }
    }

    BoxWithConstraints(
        modifier
            .height(32f.dp)
            .fillMaxWidth()
    ) {
        val trackWidthPx = constraints.maxWidth.toFloat()
        val thumbSizePx = 26f.dp.toPx()

        Box(
            Modifier
                .align(Alignment.CenterStart)
                .clip(Capsule())
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = {
                        blur(3f.dp.toPx())
                        lens(6f.dp.toPx(), 10f.dp.toPx())
                    },
                    onDrawSurface = {
                        drawRect(Color(0xFF787878).copy(alpha = 0.3f))
                    }
                )
                .height(8f.dp)
                .fillMaxWidth()
        )

        Box(
            Modifier
                .align(Alignment.CenterStart)
                .clip(Capsule())
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = {
                        blur(2f.dp.toPx())
                        lens(4f.dp.toPx(), 8f.dp.toPx())
                    },
                    onDrawSurface = {
                        drawRect(accent)
                    }
                )
                .height(8f.dp)
                .graphicsLayer {
                    scaleX = progress.value
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                }
                .fillMaxWidth()
        )

        Box(
            Modifier
                .align(Alignment.CenterStart)
                .graphicsLayer {
                    translationX = progress.value * (trackWidthPx - thumbSizePx)
                }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = {
                        blur(2f.dp.toPx())
                        lens(6f.dp.toPx(), 10f.dp.toPx(), chromaticAberration = true)
                    },
                    highlight = { Highlight.Default },
                    onDrawSurface = {
                        drawRect(Color.White.copy(alpha = 0.9f))
                    }
                )
                .size(26f.dp)
                .pointerInput(trackWidthPx) {
                    detectTapGestures { offset ->
                        onValueChange((offset.x / trackWidthPx).coerceIn(0f, 1f))
                    }
                    detectDragGestures { change, _ ->
                        change.consume()
                        onValueChange((change.position.x / trackWidthPx).coerceIn(0f, 1f))
                    }
                }
        )
    }
}