package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.kyant.backdrop.catalog.components.Ios26Segmented

@Composable
fun SegmentedContent() {
    GlassShowcaseScaffold { backdrop ->
        var segment by remember { mutableIntStateOf(0) }

        Column(
            Modifier.fillMaxWidth().padding(horizontal = 32f.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28f.dp)
        ) {
            BasicText("Segmented", style = TextStyle(Color.White, 24.sp))

            Ios26Segmented(
                options = listOf("Day", "Week", "Month"),
                selectedIndex = { segment },
                onSelectedChange = { segment = it },
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth()
            )

            Ios26Segmented(
                options = listOf("Music", "Podcasts", "Audio"),
                selectedIndex = { segment % 3 },
                onSelectedChange = { segment = it },
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}