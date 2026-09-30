package com.kyant.backdrop.catalog

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

val defaultShowcaseBackground: Brush =
    Brush.linearGradient(
        listOf(
            Color(0xFF0F2027),
            Color(0xFF2C5364),
            Color(0xFF203A43)
        )
    )

val lightShowcaseBackground: Brush =
    Brush.linearGradient(
        listOf(
            Color(0xFFA1C4FD),
            Color(0xFFC2E9FB),
            Color(0xFFE0C3FC)
        )
    )

/**
 * IOS26 原创展示框：
 * 渐变背景作为折射采样源，所有组件悬浮其上展示液态玻璃折射效果
 */
@Composable
fun GlassShowcaseScaffold(
    background: Brush = defaultShowcaseBackground,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(backdrop: LayerBackdrop) -> Unit
) {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val backdrop = rememberLayerBackdrop()

        Canvas(
            Modifier
                .layerBackdrop(backdrop)
                .then(modifier)
                .fillMaxSize()
        ) {
            drawRect(background)
            // 装饰性光斑，让折射/模糊效果更明显
            drawCircle(
                Color.White.copy(alpha = 0.35f),
                radius = size.minDimension * 0.18f,
                center = Offset(size.width * 0.25f, size.height * 0.28f)
            )
            drawCircle(
                Color(0x66FF9F0A),
                radius = size.minDimension * 0.13f,
                center = Offset(size.width * 0.8f, size.height * 0.22f)
            )
            drawCircle(
                Color(0x5500C7BE),
                radius = size.minDimension * 0.16f,
                center = Offset(size.width * 0.15f, size.height * 0.78f)
            )
            drawCircle(
                Color(0x66FF2D55),
                radius = size.minDimension * 0.11f,
                center = Offset(size.width * 0.85f, size.height * 0.8f)
            )
        }

        content(backdrop)
    }
}