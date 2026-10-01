/*
 * Copyright 2026 The Liquid Glass Showcase authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liquidglass.showcase.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.liquidglass.showcase.components.Caption
import com.liquidglass.showcase.components.GlassActionSheet
import com.liquidglass.showcase.components.GlassActivityIndicator
import com.liquidglass.showcase.components.GlassAlert
import com.liquidglass.showcase.components.GlassAvatar
import com.liquidglass.showcase.components.GlassBadge
import com.liquidglass.showcase.components.GlassBottomTabBar
import com.liquidglass.showcase.components.GlassButton
import com.liquidglass.showcase.components.GlassCard
import com.liquidglass.showcase.components.GlassCheckbox
import com.liquidglass.showcase.components.GlassChip
import com.liquidglass.showcase.components.GlassControlCenter
import com.liquidglass.showcase.components.GlassDialog
import com.liquidglass.showcase.components.GlassIcon
import com.liquidglass.showcase.components.GlassIconButton
import com.liquidglass.showcase.components.GlassListDivider
import com.liquidglass.showcase.components.GlassListGroup
import com.liquidglass.showcase.components.GlassListItem
import com.liquidglass.showcase.components.GlassNavBar
import com.liquidglass.showcase.components.GlassPicker
import com.liquidglass.showcase.components.GlassPopoverMenu
import com.liquidglass.showcase.components.GlassProgressBar
import com.liquidglass.showcase.components.GlassRadioButton
import com.liquidglass.showcase.components.GlassSearchBar
import com.liquidglass.showcase.components.GlassSegmentedControl
import com.liquidglass.showcase.components.GlassSlider
import com.liquidglass.showcase.components.GlassStepper
import com.liquidglass.showcase.components.GlassTabItem
import com.liquidglass.showcase.components.GlassTextField
import com.liquidglass.showcase.components.GlassToast
import com.liquidglass.showcase.components.GlassToggle
import com.liquidglass.showcase.core.ios.GlassIcons
import com.liquidglass.showcase.core.ios.IosColors

private val CatalogTabs = listOf(
    GlassTabItem("组件", GlassIcons.Grid),
    GlassTabItem("控制中心", GlassIcons.Sliders),
    GlassTabItem("弹窗", GlassIcons.Ellipsis)
)

/**
 * The main catalog: a glass navigation bar, a glass bottom tab bar and 26 iOS system components
 * built exclusively from the upstream Backdrop 2.0.0 materials.
 */
@Composable
fun BoxScope.CatalogScreen(
    backdrop: LayerBackdrop,
    isLightTheme: Boolean
) {
    var tabIndex by remember { mutableIntStateOf(0) }

    var showDialog by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var toastText by remember { mutableStateOf("已复制到剪贴板") }

    LaunchedEffect(showToast) {
        if (showToast) {
            kotlinx.coroutines.delay(2000)
            showToast = false
        }
    }

    when (tabIndex) {
        0 -> ComponentsTab(
            backdrop = backdrop,
            isLightTheme = isLightTheme,
            onShowDialog = { showDialog = true },
            onShowSheet = { showSheet = true },
            onShowToast = {
                toastText = "液态玻璃已更新"
                showToast = true
            }
        )

        1 -> ControlCenterTab(backdrop = backdrop, isLightTheme = isLightTheme)

        else -> OverlaysTab(
            backdrop = backdrop,
            isLightTheme = isLightTheme,
            onShowDialog = { showDialog = true },
            onShowAlert = { showAlert = true },
            onShowSheet = { showSheet = true },
            onShowMenu = { showMenu = true },
            onShowToast = {
                toastText = "操作已完成"
                showToast = true
            }
        )
    }

    // 10. 导航栏 — upstream `tutorials/glass-bottom-bar` material
    GlassNavBar(
        title = when (tabIndex) {
            0 -> "液态玻璃组件"
            1 -> "控制中心"
            else -> "弹窗与提示"
        },
        backdrop = backdrop,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding()
            .padding(horizontal = 16f.dp, vertical = 8f.dp)
            .fillMaxWidth(),
        isLightTheme = isLightTheme,
        trailing = {
            GlassIcon(
                icon = GlassIcons.Gear,
                tint = IosColors.content(isLightTheme).copy(alpha = 0.7f),
                size = 20f.dp
            )
        }
    )

    // 11. 底部标签栏 — upstream `components/LiquidBottomTabs.kt` material
    GlassBottomTabBar(
        tabs = CatalogTabs,
        selectedTabIndex = { tabIndex },
        onTabSelected = { tabIndex = it },
        backdrop = backdrop,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(horizontal = 16f.dp, vertical = 8f.dp)
            .fillMaxWidth(),
        isLightTheme = isLightTheme
    )

    // 12–14, 23, 24 — overlays
    GlassDialog(
        visible = showDialog,
        onDismiss = { showDialog = false },
        backdrop = backdrop,
        title = "液态玻璃弹窗",
        message = "本弹窗严格复用仓库 DialogContent 的 AGSL 参数：colorControls(brightness, saturation = 1.5)、" +
                "blur(16.dp)、lens(24.dp, 48.dp, depthEffect = true) 与 Highlight.Plain。",
        onConfirm = { showDialog = false },
        isLightTheme = isLightTheme
    )

    GlassAlert(
        visible = showAlert,
        onDismiss = { showAlert = false },
        backdrop = backdrop,
        title = "是否保留玻璃质感？",
        message = "提示框沿用同一套弹窗玻璃材质，边缘高光与圆角形变完全一致。",
        isLightTheme = isLightTheme
    )

    GlassActionSheet(
        visible = showSheet,
        onDismiss = { showSheet = false },
        backdrop = backdrop,
        title = "操作表",
        actions = listOf("用玻璃分享", "添加到玻璃收藏", "复制玻璃链接", "彻底删除"),
        isLightTheme = isLightTheme
    )

    GlassPopoverMenu(
        visible = showMenu,
        onDismiss = { showMenu = false },
        backdrop = backdrop,
        items = listOf("重命名", "复制", "移动到…", "删除"),
        isLightTheme = isLightTheme
    )

    GlassToast(
        visible = showToast,
        backdrop = backdrop,
        text = toastText,
        isLightTheme = isLightTheme
    )
}

@Composable
private fun ComponentsTab(
    backdrop: Backdrop,
    isLightTheme: Boolean,
    onShowDialog: () -> Unit,
    onShowSheet: () -> Unit,
    onShowToast: () -> Unit
) {
    val contentColor = IosColors.content(isLightTheme)

    var toggleOn by remember { mutableStateOf(true) }
    var sliderValue by remember { mutableFloatStateOf(0.4f) }
    var stepperValue by remember { mutableIntStateOf(3) }
    var segmentedIndex by remember { mutableIntStateOf(1) }
    var textValue by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(true) }
    var radioIndex by remember { mutableIntStateOf(0) }
    var pickerIndex by remember { mutableIntStateOf(1) }

    val infiniteTransition = rememberInfiniteTransition(label = "progress")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progressValue"
    )

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16f.dp, end = 16f.dp, top = 104f.dp, bottom = 132f.dp),
        verticalArrangement = Arrangement.spacedBy(14f.dp)
    ) {
        item {
            // 1. 卡片
            GlassCard(
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                surfaceColor = Color.White.copy(alpha = 0.3f)
            ) {
                Column(
                    Modifier.padding(20f.dp),
                    verticalArrangement = Arrangement.spacedBy(8f.dp)
                ) {
                    SectionTitle("1 · 卡片 Card", contentColor)
                    Caption(
                        "vibrancy() + lens(16.dp, 32.dp)，G2 连续圆角 32dp，默认 Highlight / Shadow。",
                        contentColor.copy(alpha = 0.62f)
                    )
                }
            }
        }

        item {
            // 2. 按钮
            SectionTitle("2 · 按钮 Button", contentColor)
            Column(verticalArrangement = Arrangement.spacedBy(10f.dp)) {
                GlassButton(onClick = onShowToast, backdrop = backdrop) {
                    BasicText("透明液态按钮", style = TextStyle(contentColor, 15f.sp))
                }
                GlassButton(
                    onClick = onShowToast,
                    backdrop = backdrop,
                    surfaceColor = Color.White.copy(alpha = 0.3f)
                ) {
                    BasicText("表面液态按钮", style = TextStyle(contentColor, 15f.sp))
                }
                GlassButton(
                    onClick = onShowDialog,
                    backdrop = backdrop,
                    tint = IosColors.AccentLight
                ) {
                    BasicText("着色液态按钮", style = TextStyle(IosColors.onAccent(), 15f.sp))
                }
            }
            Spacer(10f.dp)
            Caption(
                "vibrancy() + blur(2.dp) + lens(12.dp, 24.dp)，着色使用 BlendMode.Hue（同仓库 LiquidButton）。",
                contentColor.copy(alpha = 0.62f)
            )
        }

        item {
            // 3. 图标按钮 · 4. 步进器
            SectionTitle("3 · 图标按钮 Icon Button", contentColor)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12f.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    onClick = onShowToast,
                    backdrop = backdrop,
                    icon = GlassIcons.Heart,
                    contentColor = contentColor
                )
                GlassIconButton(
                    onClick = onShowToast,
                    backdrop = backdrop,
                    icon = GlassIcons.Share,
                    contentColor = contentColor
                )
                GlassIconButton(
                    onClick = onShowToast,
                    backdrop = backdrop,
                    icon = GlassIcons.Plus,
                    tint = IosColors.AccentLight
                )
            }
            Spacer(18f.dp)
            SectionTitle("4 · 步进器 Stepper", contentColor)
            GlassStepper(
                value = stepperValue,
                onValueChange = { stepperValue = it },
                backdrop = backdrop,
                contentColor = contentColor
            )
        }

        item {
            // 5. 开关 · 6. 滑块
            SectionTitle("5 · 开关 Toggle", contentColor)
            GlassToggle(
                selected = { toggleOn },
                onSelect = { toggleOn = it },
                backdrop = backdrop,
                isLightTheme = isLightTheme
            )
            Spacer(18f.dp)
            SectionTitle("6 · 滑块 Slider", contentColor)
            GlassSlider(
                value = { sliderValue },
                onValueChange = { sliderValue = it },
                backdrop = backdrop,
                isLightTheme = isLightTheme
            )
            Spacer(6f.dp)
            Caption(
                "拖动玻璃滑块：blur(8.dp × (1 − press)) + lens(10.dp × press, 14.dp × press, chromaticAberration = true)。",
                contentColor.copy(alpha = 0.62f)
            )
        }

        item {
            // 7. 分段控件
            SectionTitle("7 · 分段控件 Segmented Control", contentColor)
            GlassSegmentedControl(
                segments = listOf("全部", "进行中", "已完成"),
                selectedIndex = segmentedIndex,
                onSelected = { segmentedIndex = it },
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                isLightTheme = isLightTheme
            )
        }

        item {
            // 8. 输入框 · 9. 搜索框
            SectionTitle("8 · 输入框 Text Field", contentColor)
            GlassTextField(
                value = textValue,
                onValueChange = { textValue = it },
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                isLightTheme = isLightTheme
            )
            Spacer(18f.dp)
            SectionTitle("9 · 搜索框 Search Bar", contentColor)
            GlassSearchBar(
                query = query,
                onQueryChange = { query = it },
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                isLightTheme = isLightTheme
            )
        }

        item {
            // 18. 复选框 · 19. 单选按钮
            SectionTitle("18 · 复选框 Checkbox", contentColor)
            Row(
                horizontalArrangement = Arrangement.spacedBy(14f.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassCheckbox(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    backdrop = backdrop,
                    isLightTheme = isLightTheme
                )
                Caption(
                    if (checked) "已选中" else "未选中",
                    contentColor.copy(alpha = 0.7f)
                )
            }
            Spacer(18f.dp)
            SectionTitle("19 · 单选按钮 Radio", contentColor)
            Row(horizontalArrangement = Arrangement.spacedBy(14f.dp)) {
                listOf("标准", "大号", "紧凑").forEachIndexed { index, label ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6f.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassRadioButton(
                            selected = radioIndex == index,
                            onClick = { radioIndex = index },
                            backdrop = backdrop,
                            isLightTheme = isLightTheme
                        )
                        Caption(label, contentColor.copy(alpha = 0.7f))
                    }
                }
            }
        }

        item {
            // 20. 标签 · 21. 头像 · 22. 角标
            SectionTitle("20 · 标签 Chip", contentColor)
            Row(horizontalArrangement = Arrangement.spacedBy(10f.dp)) {
                GlassChip("液态玻璃", backdrop, selectedColor = IosColors.AccentLight, contentColor = contentColor)
                GlassChip("折射", backdrop, isSelected = true, selectedColor = IosColors.AccentLight)
                GlassChip("色散", backdrop, contentColor = contentColor)
            }
            Spacer(18f.dp)
            SectionTitle("21 · 头像 Avatar · 22 · 角标 Badge", contentColor)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16f.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    GlassAvatar(
                        initials = "LG",
                        backdrop = backdrop,
                        surfaceColor = Color.White.copy(alpha = 0.3f),
                        contentColor = contentColor
                    )
                    GlassBadge(
                        count = 3,
                        backdrop = backdrop,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
                GlassAvatar(
                    initials = "2.0",
                    backdrop = backdrop,
                    size = 48f.dp,
                    surfaceColor = IosColors.AccentLight.copy(alpha = 0.85f),
                    contentColor = IosColors.onAccent()
                )
            }
        }

        item {
            // 15. 列表项
            SectionTitle("15 · 列表项 List Item", contentColor)
            GlassListGroup(backdrop = backdrop, isLightTheme = isLightTheme) {
                GlassListItem(
                    title = "液态玻璃",
                    subtitle = "Backdrop 2.0.0 · AGSL 着色器",
                    icon = GlassIcons.Droplet,
                    backdrop = backdrop,
                    isLightTheme = isLightTheme,
                    onClick = onShowToast
                )
                GlassListDivider(isLightTheme, startPadding = 62f.dp)
                GlassListItem(
                    title = "折射高度 / 强度",
                    subtitle = "lens(24.dp, 48.dp, depthEffect = true)",
                    icon = GlassIcons.Sliders,
                    backdrop = backdrop,
                    isLightTheme = isLightTheme,
                    onClick = onShowSheet
                )
                GlassListDivider(isLightTheme, startPadding = 62f.dp)
                GlassListItem(
                    title = "色散",
                    subtitle = "7 段色散采样 · chromaticAberration",
                    icon = GlassIcons.Star,
                    backdrop = backdrop,
                    isLightTheme = isLightTheme,
                    onClick = onShowToast
                )
            }
        }

        item {
            // 16. 进度条 · 17. 活动指示器
            SectionTitle("16 · 进度条 Progress Bar", contentColor)
            GlassProgressBar(
                progress = progress,
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                isLightTheme = isLightTheme
            )
            Spacer(18f.dp)
            SectionTitle("17 · 活动指示器 Activity Indicator", contentColor)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16f.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassActivityIndicator(backdrop = backdrop, isLightTheme = isLightTheme)
                Caption(
                    "玻璃容器：vibrancy() + blur(2.dp) + lens(12.dp, 24.dp)",
                    contentColor.copy(alpha = 0.62f)
                )
            }
        }

        item {
            // 25. 选择器
            SectionTitle("25 · 选择器 Picker", contentColor)
            GlassPicker(
                items = listOf("液态玻璃", "毛玻璃", "金属", "亚克力"),
                selectedIndex = pickerIndex,
                onSelected = { pickerIndex = it },
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                isLightTheme = isLightTheme
            )
        }
    }
}

@Composable
private fun ControlCenterTab(backdrop: Backdrop, isLightTheme: Boolean) {
    val contentColor = IosColors.content(isLightTheme)
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16f.dp, end = 16f.dp, top = 104f.dp, bottom = 132f.dp),
        verticalArrangement = Arrangement.spacedBy(16f.dp)
    ) {
        item {
            SectionTitle("26 · 控制中心 Control Center", contentColor)
            Caption(
                "vibrancy() + lens(24.dp, 48.dp, depthEffect = true)，Highlight 角度由重力传感器驱动" +
                        "（HighlightStyle.Default(angle = gravityAngle, falloff = 2f)），与仓库 ControlCenterContent 一致。",
                contentColor.copy(alpha = 0.62f)
            )
        }
        item {
            GlassControlCenter(
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                isLightTheme = isLightTheme
            )
        }
    }
}

@Composable
private fun OverlaysTab(
    backdrop: Backdrop,
    isLightTheme: Boolean,
    onShowDialog: () -> Unit,
    onShowAlert: () -> Unit,
    onShowSheet: () -> Unit,
    onShowMenu: () -> Unit,
    onShowToast: () -> Unit
) {
    val contentColor = IosColors.content(isLightTheme)
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16f.dp, end = 16f.dp, top = 104f.dp, bottom = 132f.dp),
        verticalArrangement = Arrangement.spacedBy(14f.dp)
    ) {
        item { SectionTitle("12 · 弹窗 Dialog", contentColor) }
        item { GlassButton(onClick = onShowDialog, backdrop = backdrop) { BasicText("打开液态玻璃弹窗", style = TextStyle(contentColor, 15f.sp)) } }

        item { SectionTitle("13 · 提示框 Alert", contentColor) }
        item { GlassButton(onClick = onShowAlert, backdrop = backdrop, surfaceColor = Color.White.copy(alpha = 0.3f)) { BasicText("打开提示框", style = TextStyle(contentColor, 15f.sp)) } }

        item { SectionTitle("14 · 操作表 Action Sheet", contentColor) }
        item { GlassButton(onClick = onShowSheet, backdrop = backdrop, surfaceColor = Color.White.copy(alpha = 0.3f)) { BasicText("弹出操作表", style = TextStyle(contentColor, 15f.sp)) } }

        item { SectionTitle("23 · 轻提示 Toast", contentColor) }
        item { GlassButton(onClick = onShowToast, backdrop = backdrop, tint = IosColors.SwitchAccentLight) { BasicText("显示轻提示", style = TextStyle(IosColors.onAccent(), 15f.sp)) } }

        item { SectionTitle("24 · 浮层菜单 Popover Menu", contentColor) }
        item { GlassButton(onClick = onShowMenu, backdrop = backdrop) { BasicText("打开浮层菜单", style = TextStyle(contentColor, 15f.sp)) } }

        item {
            Caption(
                "全部浮层共用仓库材质：弹窗/提示框/浮层菜单使用 DialogContent 配方，操作表使用官方文档的 " +
                        "GlassBottomSheet 配方（vibrancy + blur(4.dp) + lens(24.dp, 48.dp, depthEffect)）；" +
                        "浮层内部按钮通过 exportedBackdrop 实现「玻璃叠玻璃」。",
                contentColor.copy(alpha = 0.62f)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, color: Color) {
    BasicText(
        text,
        style = TextStyle(color, 15f.sp, FontWeight.SemiBold)
    )
}

@Composable
private fun Spacer(height: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.layout.Spacer(Modifier.size(0f.dp, height))
}