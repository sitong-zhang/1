package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.catalog.GlassShowcaseScaffold
import com.kyant.backdrop.catalog.components.Ios26Button
import com.kyant.backdrop.catalog.components.Ios26RoundedButton

@Composable
fun ButtonsContent() {
    GlassShowcaseScaffold { backdrop ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20f.dp)
        ) {
            BasicText(
                "Buttons",
                style = TextStyle(Color.White, 24.sp)
            )

            Ios26Button({}, backdrop, text = "透明玻璃按钮")
            Ios26Button(
                {},
                backdrop,
                text = "蓝色玻璃按钮",
                tint = Color(0xFF007AFF)
            )
            Ios26Button(
                {},
                backdrop,
                text = "橙色玻璃按钮",
                tint = Color(0xFFFF9500)
            )
            Ios26Button(
                {},
                backdrop,
                text = "绿色玻璃按钮",
                tint = Color(0xFF34C759)
            )
            Ios26RoundedButton({}, backdrop, text = "圆角玻璃按钮")
        }
    }
}