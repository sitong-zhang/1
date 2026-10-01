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

package com.liquidglass.showcase.ios

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One app shown on the simulated iOS home screen.
 *
 * @param glyph line/fill geometry from [IosGlyphs] (generated from ui-icons-hub).
 * @param top / [bottom] the two stops of the icon squircle gradient.
 */
data class IosApp(
    val id: String,
    val label: String,
    val glyph: ImageVector,
    val top: Color,
    val bottom: Color,
    val glyphTint: Color = Color.White
)

/**
 * The simulator's app set: one dock row plus two home-screen pages.
 *
 * Every glyph is taken 1:1 from the `Lucide` set bundled in the user's own asset hub
 * (ui-icons-hub, ISC licence) — see `tools/generate_ios_glyphs.py`. The squircle gradients
 * are the simulator's own decoration and carry no Apple artwork.
 */
object IosAppCatalog {

    val dock: List<IosApp> = listOf(
        IosApp("phone", "电话", IosGlyphs.Phone, Color(0xFF6BE585), Color(0xFF25C55B)),
        IosApp("safari", "Safari", IosGlyphs.Safari, Color(0xFF5AC8FA), Color(0xFF0A6CFF)),
        IosApp("messages", "信息", IosGlyphs.Messages, Color(0xFF74E86B), Color(0xFF1FBF3F)),
        IosApp("music", "音乐", IosGlyphs.Music, Color(0xFFFF6B81), Color(0xFFF5233F))
    )

    /** Page 1 — the "everyday" apps. */
    private val page1: List<IosApp> = listOf(
        IosApp("facetime", "FaceTime", IosGlyphs.FaceTime, Color(0xFF5CE86B), Color(0xFF12B93C)),
        IosApp("calendar", "日历", IosGlyphs.Calendar, Color(0xFFFFFFFF), Color(0xFFE6E6EC), Color(0xFFE0342B)),
        IosApp("photos", "照片", IosGlyphs.Photos, Color(0xFFFFD60A), Color(0xFFFF375F)),
        IosApp("camera", "相机", IosGlyphs.Camera, Color(0xFF9A9AA0), Color(0xFF45454A)),
        IosApp("mail", "邮件", IosGlyphs.Mail, Color(0xFF63B4FF), Color(0xFF0A6CFF)),
        IosApp("notes", "备忘录", IosGlyphs.Notes, Color(0xFFFFF3C4), Color(0xFFFFD84D), Color(0xFF6B5200)),
        IosApp("reminders", "提醒事项", IosGlyphs.Reminders, Color(0xFFFFFFFF), Color(0xFFE6E6EC), Color(0xFF3A3A3C)),
        IosApp("clock", "时钟", IosGlyphs.Clock, Color(0xFF3A3A3C), Color(0xFF101012)),
        IosApp("maps", "地图", IosGlyphs.Maps, Color(0xFF7BE38F), Color(0xFF2E9E4F)),
        IosApp("weather", "天气", IosGlyphs.Weather, Color(0xFF5BC8FF), Color(0xFF0A7BFF)),
        IosApp("news", "新闻", IosGlyphs.News, Color(0xFFFF7A7A), Color(0xFFE01E43)),
        IosApp("stocks", "股市", IosGlyphs.Stocks, Color(0xFF4A4A4E), Color(0xFF1C1C1E)),
        IosApp("books", "图书", IosGlyphs.Books, Color(0xFFFF9F5A), Color(0xFFE56500)),
        IosApp("appstore", "App Store", IosGlyphs.AppStore, Color(0xFF3ED1FF), Color(0xFF0A84FF)),
        IosApp("health", "健康", IosGlyphs.Health, Color(0xFFFF7A9A), Color(0xFFE01542)),
        IosApp("wallet", "钱包", IosGlyphs.Wallet, Color(0xFF5A5A60), Color(0xFF1C1C1E)),
        IosApp("files", "文件", IosGlyphs.Files, Color(0xFF6FC8FF), Color(0xFF0A84FF)),
        IosApp("podcasts", "播客", IosGlyphs.Podcasts, Color(0xFFC97BFF), Color(0xFF7B2DE2)),
        IosApp("tv", "TV", IosGlyphs.Tv, Color(0xFF3A3A3C), Color(0xFF0B0B0C)),
        IosApp("home", "家庭", IosGlyphs.Home, Color(0xFFFFB464), Color(0xFFE06A00))
    )

    /** Page 2 — utilities, plus the door back to the 26-component catalogue. */
    private val page2: List<IosApp> = listOf(
        IosApp("settings", "设置", IosGlyphs.Settings, Color(0xFFC4C4CB), Color(0xFF7E7E86)),
        IosApp("calculator", "计算器", IosGlyphs.Calculator, Color(0xFFFFB340), Color(0xFFE07E00)),
        IosApp("gallery", "组件画廊", IosGlyphs.Grip, Color(0xFF8E7BFF), Color(0xFF5A3BFF)),
        IosApp("tips", "提示", IosGlyphs.Tips, Color(0xFFFFD452), Color(0xFFE08C00), Color(0xFF5A3D00)),
        IosApp("translate", "翻译", IosGlyphs.Translate, Color(0xFF6FA8FF), Color(0xFF1F6FFF)),
        IosApp("measure", "测距仪", IosGlyphs.Measure, Color(0xFFE0E0E6), Color(0xFF9A9AA0), Color(0xFF3A3A3C)),
        IosApp("shortcuts", "快捷指令", IosGlyphs.Shortcuts, Color(0xFF7A8CFF), Color(0xFF3B4FFF)),
        IosApp("fitness", "健身", IosGlyphs.Fitness, Color(0xFF7CFFB2), Color(0xFF00A03F), Color(0xFF00401A)),
        IosApp("voicememos", "语音备忘录", IosGlyphs.VoiceMemos, Color(0xFF3A3A3C), Color(0xFF1C1C1E)),
        IosApp("magnifier", "放大器", IosGlyphs.Magnifier, Color(0xFF9AA0A6), Color(0xFF5A5A60)),
        IosApp("globe", "浏览器", IosGlyphs.Globe, Color(0xFF7ED4FF), Color(0xFF1E88E5)),
        IosApp("flashlight", "手电筒", IosGlyphs.Flashlight, Color(0xFFFFFFFF), Color(0xFFD6D6DC), Color(0xFF3A3A3C)),
        IosApp("headphones", "耳机", IosGlyphs.Headphones, Color(0xFFB0B0B8), Color(0xFF5A5A60)),
        IosApp("timer", "计时器", IosGlyphs.Timer, Color(0xFF3A3A3C), Color(0xFF101012)),
        IosApp("alarm", "闹钟", IosGlyphs.AlarmClock, Color(0xFF3A3A3C), Color(0xFF101012)),
        IosApp("bell", "声音与触感", IosGlyphs.Bell, Color(0xFFFFC56B), Color(0xFFE07E00), Color(0xFF4A2A00))
    )

    val pages: List<List<IosApp>> = listOf(page1, page2)

    /** Every app that can be opened, dock included. */
    val allApps: List<IosApp> = dock + pages.flatten()
}
