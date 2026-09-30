package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.catalog.GlassShowcaseScaffold
import com.kyant.backdrop.catalog.components.Ios26Switch

@Composable
fun SwitchContent() {
    GlassShowcaseScaffold { backdrop ->
        var wifi by remember { mutableStateOf(true) }
        var bluetooth by remember { mutableStateOf(false) }
        var airplane by remember { mutableStateOf(false) }
        var darkMode by remember { mutableStateOf(true) }

        Column(
            Modifier.fillMaxWidth().padding(horizontal = 40f.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24f.dp)
        ) {
            BasicText("Switch", style = TextStyle(Color.White, 24.sp))

            SwitchRow("Wi-Fi", wifi, backdrop) { wifi = it }
            SwitchRow("Bluetooth", bluetooth, backdrop) { bluetooth = it }
            SwitchRow("Airplane Mode", airplane, backdrop) { airplane = it }
            SwitchRow("Dark Mode", darkMode, backdrop) { darkMode = it }
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    backdrop: Backdrop,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicText(
            label,
            style = TextStyle(Color.White, 17.sp)
        )
        Ios26Switch(checked, onCheckedChange, backdrop)
    }
}