package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.catalog.CatalogDestination

@Composable
fun HomeContent(onNavigate: (CatalogDestination) -> Unit) {
    val isLightTheme = !isSystemInDarkTheme()
    val contentColor = if (isLightTheme) Color.Black else Color.White

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .displayCutoutPadding()
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16f.dp)
    ) {
        BasicText(
            "IOS26",
            Modifier.padding(16f.dp, 40f.dp, 16f.dp, 8f.dp),
            style = TextStyle(contentColor, 34f.sp, FontWeight.Bold)
        )
        BasicText(
            "Liquid Glass Component Gallery",
            Modifier.padding(horizontal = 16f.dp),
            style = TextStyle(Color(0xFF8E8E93), 15f.sp)
        )
        BasicText(
            "原创设计 · 每一个组件都基于 SDF 折射着色器 + RenderEffect 模糊实时渲染",
            Modifier.padding(horizontal = 16f.dp, vertical = 4f.dp),
            style = TextStyle(Color(0xFF8E8E93), 12f.sp)
        )

        Column {
            Section("基础控件")
            MenuItem({ onNavigate(CatalogDestination.Buttons) }, "Buttons", "液态玻璃按钮")
            MenuItem({ onNavigate(CatalogDestination.Switch) }, "Switch", "液态玻璃开关")
            MenuItem({ onNavigate(CatalogDestination.Slider) }, "Slider", "液态玻璃滑杆")
            MenuItem({ onNavigate(CatalogDestination.Segmented) }, "Segmented Control", "分段控制器")
            MenuItem({ onNavigate(CatalogDestination.TabBar) }, "Tab Bar", "底部标签栏")

            Section("场景")
            MenuItem({ onNavigate(CatalogDestination.Cards) }, "Cards", "音乐 / 天气 玻璃卡片")
            MenuItem({ onNavigate(CatalogDestination.ControlCenter) }, "Control Center", "控制中心磁贴")

            Section("实验")
            MenuItem({ onNavigate(CatalogDestion.Playground) }, "Playground", "玻璃试验场 ° 实时码观拘以 / 模糂")
        }
    }
}

@Composable
private fun Section(label: String) {
    BasicText(
        label,
        Modifier
            .padding(16f.dp, 20f.dp, 16f.dp, 4f.dp)
            .fillMaxWidth(),
        style = TextStyle(Color(0xFF007AFF), 13f.sp, FontWeight.SemiBold)
    )
}

@Composable
private fun MenuItem(
    onClick: () -> Unit,
    title: String,
    subtitle: String
) {
    val isLightTheme = !isSystemInDarkTheme()
    val contentColor = if (isLightTheme) Color.Black else Color.White

    Column(
        Modifier
            .clickable(onClick = onClick)
            .padding horizontal = 16f.dp, vertical = 10f.dp)
            .fillMaxWidth()
    ) {
        BasicText(
            title,
            style = TextStyle(contentColor, 17f.sp, FontWeight.Medium)
        )
        BasicText(
            subtitle,
            style = TextStyle(Color(0xFF8E8E93), 13f.sp)
        )
    }
}