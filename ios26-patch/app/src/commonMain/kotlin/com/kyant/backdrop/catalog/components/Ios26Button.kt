package com.kyant.backdrop.catalog.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.Capsule

@Composable
fun Ios26Button(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    text: String = "Button",
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    textColor: Color = Color.White
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressProgress = remember { Animatable(0f) }
    LaunchedEffect(isPressed) {
        pressProgress.animateTo(if (isPressed) 1f else 0f, spring(0.6f, 400f, 0.001f))
    }
    Box(
        modifier
            .graphicsLayer {
                val progress = pressProgress.value
                val scale = 1f + 0.035f * progress
                scaleX = scale
                scaleY = scale
            }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(2f.dp.toPx())
                    lens(14f.dp.toPx(), 26f.dp.toPx(), depthEffect = true, chromaticAberration = true)
                },
                onDrawSurface = {
                    if (tint.isSpecified) {
                        drawRect(tint.copy(alpha = 0.65f), blendMode = androidx.compose.ui.graphics.BlendMode.Hue)
                        drawRect(tint.copy(alpha = 0.55f))
                    }
                    if (surfaceColor.isSpecified) {
                        drawRect(surfaceColor)
                    }
                }
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .height(52f.dp)
            .padding(horizontal = 24f.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicText(text, style = TextStyle(color = textColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold))
    }
}

@Composable
fun Ios26RoundedButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 18f.dp,
    text: String = "Continue",
    tint: Color = Color(0xFF007AFF)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressProgress = remember { Animatable(0f) }
    LaunchedEffect(isPressed) {
        pressProgress.animateTo(if (isPressed) 1f else 0f, spring(0.6f, 400f, 0.001f))
    }
    Box(
        modifier
            .graphicsLayer {
                val progress = pressProgress.value
                scaleX = 1f + 0.03f * progress
                scaleY = 1f + 0.03f * progress
            }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { com.kyant.shapes.RoundedRectangle(cornerRadius) },
                effects = {
                    vibrancy()
                    blur(3f.dp.toPx())
                    lens(10f.dp.toPx(), 18f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(tint.copy(alpha = 0.5f), blendMode = androidx.compose.ui.graphics.BlendMode.Hue)
                    drawRect(tint.copy(alpha = 0.4f))
                }
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .height(56f.dp)
            .padding horizontal = 20f.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicText(text, style = TextStyle(Color.White, 17.sp, FontWeight.SemiBold))
    }
}