package com.kyant.backdrop.catalog.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule

@Composable
fun Ios26TabBar(
    tabs: List<String>,
    selectedIndex: () -> Int,
    onTabSelected: (Int) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val anim = remember { Animatable(selectedIndex().toFloat()) }
    LaunchedEffect(selectedIndex()) {
        anim.animateTo(selectedIndex().toFloat(), spring(0.8f, 500f, 0.001f))
    }

    BoxWithConstraints(
        modifier
            .height(60f.dp)
            .fillMaxWidth()
            .padding(horizontal = 8f.dp)
    ) {
        val tabWidth = constraints.maxWidth / tabs.size

        Box(
            Modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = {
                        blur(10f.dp.toPx())
                        lens(18f.dp.toPx(), 24f.dp.toPx())
                    },
                    highlight = { Highlight.Default },
                    shadow = {
                        Shadow(
                            radius = 16f.dp,
                            color = Color.Black.copy(alpha = 0.15f)
                        )
                    },
                    onDrawSurface = {
                        drawRect(Color(0xFF888888).copy(alpha = 0.1f))
                    }
                )
                .fillMaxWidth()
                .height(56f.dp)
                .align(Alignment.Center)
        )

        Box(
            Modifier
                .align(Alignment.CenterStart)
                .graphicsLayer {
                    translationX = anim.value * tabWidth + 4.dp.toPx()
                    scaleX = 0.9f
                    scaleY = 0.9f
                }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = {
                        blur(5f.dp.toPx())
                        lens(8f.dp.toPx(), 10f.dp.toPx())
                    },
                    highlight = { Highlight.Default },
                    onDrawSurface = {
                        drawRect(Color.White.copy(alpha = 0.45f))
                    }
                )
                .fillMaxWidth(1f / tabs.size)
                .height(40f.dp)
        )

        Row(
            Modifier
                .fillMaxWidth()
                .height(56f.dp)
                .align(Alignment.Center),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, label ->
                val interactionSource = remember { MutableInteractionSource() }
                Column(
                    Modifier
                        .weight(1f)
                        .clickable(interactionSource = interactionSource, indication = null) {
                            onTabSelected(index)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        Modifier.height(4f.dp).padding(bottom = 1f.dp)
                    )
                    BasicText(
                        label,
                        style = TextStyle(
                            color = if (index == selectedIndex()) Color.Black.copy(alpha = 0.8f)
                            else Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}