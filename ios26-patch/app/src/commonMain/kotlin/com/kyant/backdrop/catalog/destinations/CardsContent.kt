package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun CardsContent() {
    GlassShowcaseScaffold { backdrop ->
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24f.dp),
            verticalArrangement = Arrangement.spacedBy(20f.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicText("Cards", style = TextStyle(Color.White, 24.sp))

            MusicCard(backdrop)
            WeatherCard(backdrop)
            NowPlayingCard(backdrop)
        }
    }
}

@Composable
private fun MusicCard(backdrop: Backdrop) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(88f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(22f.dp) },
                effects = {
                    vibrancy()
                    blur(6f.dp.toPx())
                    lens(10f.dp.toPx(), 16f.dp.toPx())
                },
                highlight = { Highlight.Default },
                onDrawSurface = {
                    drawRect(Color.White.copy(alpha = 0.12f))
                }
            )
            .padding(16f.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14f.dp)
        ) {
            Box(
                Modifier.size(56f.dp).clip(RoundedRectangle(12f.dp))
            ) {
                androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(56f.dp)) {
                    drawCircle(Color(0xFFFF2D55), radius = size.minDimension / 2)
                    drawCircle(Color(0xFFAF62DE), radius = size.minDimension / 3,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height * 0.3f))
                }
            }
            Column(Modifier.weight(1f)) {
                BasicText(
                    "Liquid Dreams",
                    style = TextStyle(Color.White, 16.sp, FontWeight.SemiBold)
                )
                BasicText(
                    "Nova Waves · Album",
                    style = TextStyle(Color.White.copy(alpha = 0.7f), 13.sp)
                )
            }
            BasicText("▶", style = TextStyle(Color.White, 22.sp))
        }
    }
}

@Composable
private fun WeatherCard(backdrop: Backdrop) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(72f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(22f.dp) },
                effects = {
                    vibrancy()
                    blur(6f.dp.toPx())
                    lens(10f.dp.toPx(), 16f.dp.toPx())
                },
                highlight = { Highlight.Default },
                onDrawSurface = {
                    drawRect(Color(0xFF4A90D9).copy(alpha = 0.25f))
                }
            )
            .padding(16f.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2f.d)
            ) {
                BasicText(
                    "Hangzhou",
                    style = TextStyle(Color.White.copy(alpha = 0.8f), 13.sp)
                )
                BasicText(
                    "26°C",
                    style = TextStyle(Color.White, 28.sp, FontWeight.Bold)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6f.dp)
            ) {
                Box(
                    Modifier
                        .size(width = 36f.dp, height = 30f.dp)
                        .clip(CircleShape)
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { RoundedRectangle(8f.dp) },
                            effects = { blur(3f.dp.toPx()) }
                        )
                )
                BasicText("☀️", style = TextStyle(Color.White, 24.sp))
            }
        }
    }
}

@Composable
private fun NowPlayingCard(backdrop: Backdrop) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(64f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(20f.dp) },
                effects = {
                    vibrancy()
                    blur(5f.dp.toPx())
                    lens(8f.dp.toPx(), 12f.dp.toPx())
                },
                highlight = { Highlight.Default },
                onDrawSurface = {
                    drawRect(Color.Black.copy(alpha = 0.2f))
                }
            )
            .padding(horizontal = 16f.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12f.dp)
    ) {
        Box(
            Modifier
                .size(40f.dp)
                .clip(RoundedRectangle(10f.dp))
        ) {
            androidx.compose.foundation.Canvas(Modifier.size(40f.dp)) {
                drawRect(Color(0xFF007AFF))
                drawCircle(Color.White.copy(alpha = 0.4f), radius = size.minDimension * 0.3f)
            }
        }
        Column(Modifier.weight(1f)) {
            BasicText("Now Playing", style = TextStyle(Color.White, 14.sp, FontWeight.SemiBold))
            BasicText("Your favorite track", style = TextStyle(Color.White.copy(alpha = 0.6f), 12.sp))
        }
        BasicText("░░", style = TextStyle(Color.White, 16.sp))
    }
}