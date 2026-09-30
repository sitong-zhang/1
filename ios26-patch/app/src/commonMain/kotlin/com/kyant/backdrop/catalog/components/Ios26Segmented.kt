package com.kyant.backdrop.catalog.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.Capsule

@Composable
fun Ios26Segmented(
    options: List<String>,
    selectedIndex: () -> Int,
    onSelectedChange: (Int) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val anim = remember { Animatable(selectedIndex().toFloat()) }
    LaunchedEffect(selectedIndex()) {
        anim.animateTo(selectedIndex().toFloat(), spring(1f, 600f, 0.001f))
    }

    BoxWithConstraints(
        modifier
            .height(36f.dp)
            .fillMaxWidth()
            .padding(horizontal = 2f.dp)
    ) {
        val segmentWidth = constraints.maxWidth / options.size

        Box(
            Modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = {
                        blur(4f.dp.toPx())
                        lens(8f.dp.toPx(), 12f.dp.toPx())
                    },
                    onDrawSurface = {
                        drawRect(Color.Black.copy(alpha = 0.15f))
                    }
                )
                .fillMaxWidth()
                .height(32f.dp)
                .align(Alignment.Center)
        )

        Box(
            Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 2f.dp)
                .graphicsLayer {
                    translationX = anim.value * (segmentWidth - 2.dp.toPx())
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
                        drawRect(Color.White.copy(alpha = 0.7f))
                    }
                )
                .height(28f.dp)
                .graphicsLayer { scaleX = 1f }
                .fillMaxWidth(1f / options.size)
        )

        Row(
            Modifier
                .fillMaxWidth()
                .height(32f.dp)
                .align(Alignment.Center)
        ) {
            options.forEachIndexed { index, label ->
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable(interactionSource = interactionSource, indication = null) {
                            onSelectedChange(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(
                        label,
                        style = TextStyle(
                            color = if (index == selectedIndex()) Color.Black else Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}