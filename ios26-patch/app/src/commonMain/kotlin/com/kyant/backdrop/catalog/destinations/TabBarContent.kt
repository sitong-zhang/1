package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.catalog.GlassShowcaseScaffold
import com.kyant.backdrop.catalog.components.Ios26TabBar

@Composable
fun TabBarContent() {
    GlassShowcaseScaffold { backdrop ->
        var selected by remember { mutableIntStateOf(0) }

        Column(
            Modifier.fillMaxWidth().padding(24f.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32f.dp)
        ) {
            BasicText("Tab Bar", style = TextStyle(Color.White, 24.sp))

            BasicText(
                "选中: ${listOf("Home", "Search", "Library", "Profile")[selected]}",
                style = TextStyle(Color.White.copy(alpha = 0.7f), 14.sp)
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 60f.dp)
            ) {
                Ios26TabBar(
                    tabs = listOf("Home", "Search", "Library", "Profile"),
                    selectedIndex = { selected },
                    onTabSelected = { selected = it },
                    backdrop = backdrop,
                    modifier = Modifier.systemBarsPadding()
                )
            }
        }
    }
}