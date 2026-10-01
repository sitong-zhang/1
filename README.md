# IOS26 · 安卓液态玻璃应用 —— 完整交接文档

> 本文档由 **TraeCode（模型 DeepSeek-V4.1-Flash）** 生成，用于把一次完整的人机协作对话
> （需求 → 调研 → 决策 → 实现 → 编译验证 → 交付）**无损交接**给另一个 AI 助手。
> 如果你（AI）正在读这份文档：先读完第 1–3 章（需求与事实），再看第 4–6 章（实现与坑），
> 第 8 章是完整的对话原文，可据此判断用户偏好。
>
> 交接时间：2026-10-01 ｜ 交付物：本仓库（源码 + 交接文档 + 已签名 APK）

---

## 0. 一句话概括

按用户要求，**完整调研并 1:1 复用** GitHub 仓库
[Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) 的 **Backdrop 2.0.0**
液态玻璃方案（AGSL 着色器 / 默认参数 / 渲染逻辑一律不改），用 Jetpack Compose 写了一个
展示 **26 个 iOS 风格系统组件** 的原生安卓 App，含**液态玻璃启动加载动画 + 1.2s easeOutCubic 过渡**，
并已在沙箱内**实际编译出已签名的 Release APK**（minSdk 31 / targetSdk 34）。

- 源码：`LiquidGlassShowcase/`（Gradle 工程，可直接 `./gradlew :app:assembleRelease`）
- 安装包：`IOS26.apk`（本仓库根目录，1.15 MB）
- 应用名（桌面显示）：**IOS26**

---

## 1. 用户的原始需求（原文，逐条）

第一条消息（完整原文）：

> 开始开发前必须执行完整调研流程，禁止在未理解实现原理的情况下盲目开发：
>
> 1. 完整通读指定GitHub仓库的README、官方文档、全部示例代码与AGSL着色器源码，彻底掌握其液态玻璃效果的渲染原理、核心参数定义、层级调用逻辑与默认参数值，完全对齐仓库的实现方案后再启动开发。
> 2. 所有引入的辅助依赖、UI组件库必须先在GitHub检索验证，仅选用星标≥1000且处于活跃维护状态的高星开源项目，禁止使用小众、低星、停止维护的第三方库。
> 3. 全程严格遵循仓库的原生实现逻辑，禁止自行修改玻璃折射强度、色散系数、模糊半径、边缘高光亮度、圆角形变等任何核心视觉参数，禁止自创任何玻璃效果实现方式。
>
> 基于 GitHub 仓库 https://github.com/Kyant0/AndroidLiquidGlass 开发一款原生安卓 APK，严格按照该仓库的液态玻璃实现方案完成，禁止自行创造任何玻璃效果参数。具体要求如下：
>
> 1. 实现基准：完全复用该仓库 Backdrop 2.0.0 版本的 AGSL 着色器、默认参数、渲染逻辑，1:1 复刻液态玻璃效果，不得修改玻璃折射、色散、高光、模糊的核心参数。
> 2. 应用内容：做 iOS 风格 26 个系统标准组件的液态玻璃效果展示，包含但不限于卡片、按钮、开关、滑块、输入框、导航栏、底部标签栏、弹窗、提示框、分段控件、列表项、进度条等，所有组件统一套用仓库的液态玻璃材质。
> 3. 启动动效：应用打开后先展示液态玻璃质感的等待加载动画，随后以 1.2 秒时长、easeOutCubic 插值器丝滑过渡到主组件展示界面，过渡过程保持玻璃质感连贯，无生硬跳转。
> 4. 技术规范：使用 Jetpack Compose 原生安卓开发，minSdk 31、targetSdk 34；所有辅助工具库、UI 组件均从 GitHub 选用星标 1000+ 的高星开源项目，禁止使用小众自定义实现。
> 5. 交付标准：输出完整可直接编译的安卓项目代码结构与核心代码，确保引入对应依赖后可一键构建 Release 版 APK
>
> 然后你还可以问我几个问题

后续追加要求（按时间顺序）：

| # | 用户原话 | 处理结果 |
|---|---|---|
| 2 | 「只把APK文件给我就行了」 | 把 APK 单独复制到工作区根目录并给出下载链接 |
| 3 | 「没有呀，这个工作区我看不到这个还有就是把这个名字改成IOS26」 | 解释「二进制文件不出现在变更视图」；把 `app_name` 改为 **IOS26**、APK 文件名改为 `IOS26.apk`，重新编译 |
| 4 | 「把你做好的所有源码工程解释说明……全部要放在一个压缩包里面……放在这个账号下的一个仓库里面，那个占位仓库」（并提供了 GitHub token） | 生成压缩包 + 本交接仓库 |
| 5 | 「你需要先把这个仓库里面的其他内容删掉，再把这个完整交接文档和源码文件全部放上去」 | 清空 `sitong-zhang/experiments` 工作区内容（旧内容保留在 git 历史中），放入本文档 + 源码 + APK |

> ⚠️ 用户在消息中贴过一次 GitHub Personal Access Token。**出于安全考虑，本仓库任何文件都没有记录该 token。**
> 建议提醒用户尽快到 GitHub Settings → Developer settings → Personal access tokens 中 **吊销/轮换** 该 token。

---

## 2. 关键决策（开工前我问了 4 个问题，用户的选择如下）

| 问题 | 用户选择 |
|---|---|
| `kyant-shapes` 只有 43★（低于 1000★ 门槛），但它是 backdrop 2.0.0 自带的传递运行时依赖、仓库所有示例都用它做 iOS 连续圆角。是否在应用代码中直接使用？ | **使用（与仓库 1:1）** |
| backdrop 的引入方式？ | **Maven 官方构件** `io.github.kyant0:backdrop:2.0.0` |
| AGSL lens 折射需 Android 13+，而要求 minSdk 31（Android 12 上无折射）。如何处理？ | **保持 minSdk 31 + 库原生自动降级** |
| 是否要实际编译出 Release APK？ | **要，实测编译出 APK** |

---

## 3. 调研结论（事实，可复现）

### 3.1 仓库基本事实

- 仓库：`Kyant0/AndroidLiquidGlass`，默认分支 **`kmp`**（Compose Multiplatform 工程），
  **3,935 stars**，Apache-2.0，2026-08-26 仍有提交（活跃维护）。
- 库坐标：`io.github.kyant0:backdrop`，Maven Central 最新 **2.0.1**，历史版本含 **2.0.0**。
- 官方文档：<https://kyant.gitbook.io/backdrop>（有 `llms.txt` 全量索引与 `.md` 版本）。
- 子模块：`backdrop/`（库本体）、`app/`（Catalog 示例组件）、`androidApp/`（可运行 APK 壳）。
- 另一个依赖：`io.github.kyant0:shapes`（**43★**），backdrop 的 `lens()` 用它做 G2 连续圆角。

### 3.2 版本核验（关键结论）

用 `git diff 2.0.0..HEAD -- backdrop/` 比对标签与主干：

```
backdrop/build.gradle.kts | 30 +++++++++++++-----------------
1 file changed, 13 insertions(+), 17 deletions(-)
```

**2.0.0 与主干在 `backdrop/` 目录下的差异只有构建脚本一行**，AGSL 着色器字符串、默认参数、
渲染逻辑 **逐字节一致**。因此锁定 `io.github.kyant0:backdrop:2.0.0` 即等于使用仓库当前实现，可放心 1:1 复刻。

### 3.3 四个 AGSL 着色器（原文见 `backdrop/src/commonMain/kotlin/com/kyant/backdrop/internal/Shaders.kt`）

| 着色器 | 作用 | 关键实现 |
|---|---|---|
| `RoundedRectRefractionShaderString` | 折射 | `circleMap(x)=1-√(1-x²)` 生成凸透镜剖面 → `d = circleMap(1 - (-sd)/refractionHeight) * refractionAmount`，沿 `gradSdRoundedRect` 梯度 +`depthEffect * normalize(centeredCoord)` 偏移采样坐标 |
| `RoundedRectRefractionWithDispersionShaderString` | 折射 + 色散 | 在上面基础上按 `dispersionIntensity = chromaticAberration * (x*y/(halfW*halfH))` 对 7 个通道（红/橙/黄/绿/青/蓝/紫）分别偏移采样后加权累加 |
| `DefaultHighlightShaderString` | 边缘高光（方向性） | `d = dot(grad, float2(cos(angle), sin(angle)))`，`intensity = pow(abs(d), falloff)`，颜色走 `BlendMode.Plus` |
| `AmbientHighlightShaderString` | 环境高光（分正负半区） | 同上，但用 `step(0.0, d)` 区分极性并输出 `half4(t,t,t,1)*intensity` |

配套 SDF：`sdRoundedRect`（圆角矩形有符号距离）与 `gradSdRoundedRect`（其梯度，`gradRadius = min(radius*1.5, min(halfSize.x, halfSize.y))`）。

### 3.4 渲染管线（层级调用逻辑）

`Modifier.drawBackdrop(...)` →
`DrawBackdropNode`（`LayoutModifierNode + DrawModifierNode + GlobalPositionAwareModifierNode + ObserverModifierNode`）：
1. 测量时给内容加 `graphicsLayer(clip=true, shape=形状, compositingStrategy=Offscreen)`；
2. 绘制时 `onDrawBehind → 把 backdrop 图层画到自己的 GraphicsLayer（带 padding）→ onDrawSurface → drawContent → onDrawFront`；
3. `BackdropEffectScope.apply { effects() }` 收敛出 `padding` 与 `renderEffect`（RenderEffect 链），再赋给 `graphicsLayer.renderEffect`；
4. `LayerBackdrop` 通过 `.layerBackdrop(backdrop)` 把某层的内容录进 `GraphicsLayer`，玻璃组件按坐标差取用（玻璃看到的是"背景的拷贝"）。

### 3.5 默认参数（不得修改）

| 项 | 默认值（源码） |
|---|---|
| `Highlight` | `width = 0.5dp`，`blurRadius = width / 2`，`alpha = 1f`，`style = HighlightStyle.Default` |
| `HighlightStyle.Default` | `color = White 50%`，`BlendMode.Plus`，`angle = 45°`，`falloff = 1f` |
| `HighlightStyle.Ambient` | `intensity = 0.38f`（`color = White @ intensity`，默认 BlendMode） |
| `HighlightStyle.Plain` | `color = White 38%`，`BlendMode.Plus`，无着色器 |
| `Shadow.Default` | `radius = 24dp`，`offset = DpOffset(0, radius/6)`，`color = Black 10%`，`alpha = 1f` |
| `InnerShadow.Default` | `radius = 24dp`，`offset = DpOffset(0, radius)`，`color = Black 15%`，`alpha = 1f` |
| `vibrancy()` | `saturation = 1.5` 的 ColorMatrix |
| `lens()` | `refractionAmount` 默认 = `refractionHeight`；`depthEffect=false`、`chromaticAberration=false`；内部传 `refractionAmount = -refractionAmount` |

**效果顺序是库契约（文档明示）：`colorFilter ⇒ blur ⇒ lens`。**任何调换都会失真。

### 3.6 依赖星标核验（用户硬性规则：≥1000★ 且活跃维护）

| 依赖 | 星标 | 结论 |
|---|---|---|
| `Kyant0/AndroidLiquidGlass`（backdrop） | 3,935★ | ✅ 采用（核心） |
| androidx.compose / activity / core-ktx | Google 官方 androidx（活跃） | ✅ 采用 |
| `Kyant0/Shapes`（kyant-shapes 1.2.0） | **43★** | ⚠️ 例外，经用户明确同意后使用（它是 backdrop 2.0.0 的传递运行时依赖，非我新增） |
| `androidx.compose.material:material-icons-extended` | — | ❌ **停更**（版本冻结在 1.7.8，2025-02 起无发布），故弃用，改为**自绘 26 个 ImageVector**（见 `core/ios/GlassIcons.kt`） |

### 3.7 官方文档要点

- 效果顺序、`lens(height, amount)` 取值范围（`height ∈ [0, shape.minCornerRadius]`，`amount ∈ [0, size.minDimension]`）、
  必须 `CornerBasedShape` 才能用 lens。
- 「Glass Bottom Bar」教程：`vibrancy() + blur(4.dp) + lens(16.dp, 32.dp)`，表面 `White 50%`。
- 「Glass Bottom Sheet」教程：`vibrancy() + blur(4.dp) + lens(24.dp, 48.dp, depthEffect=true)`，`RoundedCornerShape(44.dp)`；
  **玻璃叠玻璃必须用 `exportedBackdrop` 参数**，否则（对同一层既 `layerBackdrop` 又 `drawBackdrop`）会在 RenderThread 触发 SIGSEGV 崩溃。
- 「Smoother rounded corners」教程指向 Shapes 库（G2 连续曲率）。

---

## 4. 工程实现

### 4.1 技术栈与版本

| 项 | 值 |
|---|---|
| 构建 | Gradle **9.7.1**（wrapper）、AGP **9.3.2** |
| Kotlin | **2.4.10**（AGP 9 **内置 Kotlin 支持**，见第 6 章坑 1） |
| Compose | androidx.compose **1.12.0**（runtime / ui / ui-graphics / foundation / animation / material-ripple） |
| 其它 | `androidx.activity:activity-compose:1.13.0`、`androidx.core:core-ktx:1.19.0` |
| 玻璃引擎 | `io.github.kyant0:backdrop:2.0.0` |
| 形状 | `io.github.kyant0:shapes:1.2.0`（G2 连续圆角，backdrop 的传递依赖） |
| SDK | compileSdk **37**、minSdk **31**、targetSdk **34**、buildTools 37.0.0、JVM target 17 |
| 包名 | `com.liquidglass.showcase`，应用名 **IOS26** |

### 4.2 目录结构与文件职责

```
LiquidGlassShowcase/
├── settings.gradle.kts / build.gradle.kts / gradle.properties / gradle/libs.versions.toml
├── gradlew, gradlew.bat, gradle/wrapper/*            # Gradle 9.7.1 wrapper
├── keystore/liquidglass-demo.jks                     # demo 签名（口令均为 liquidglass）
├── LICENSE / NOTICE                                  # Apache-2.0 + 上游归属声明
└── app/
    ├── build.gradle.kts                              # minSdk31/targetSdk34/release 签名+R8
    ├── proguard-rules.pro                            # 保留 com.kyant.backdrop.** / shapes.**
    └── src/main/
        ├── AndroidManifest.xml, res/*                # 主题透明系统栏、自适应图标、上游壁纸
        └── java/com/liquidglass/showcase/
            ├── MainActivity.kt                       # 边到边 + 提供 LocalIndication
            ├── LiquidGlassApp.kt                     # 根：启动动画 → 1.2s easeOutCubic 过渡
            ├── core/glass/GlassMaterials.kt           # ★ 玻璃材质唯一真源（逐值对照上游）
            ├── core/glass/GlassScaffold.kt            # 壁纸 → LayerBackdrop（上游脚手架移植）
            ├── core/ios/IosColors.kt                  # 上游 iOS 配色/容器色
            ├── core/ios/GlassIcons.kt                 # 自绘 26 个 ImageVector（零图标依赖）
            ├── core/utils/                            # 上游工具类移植（仅改包名）
            │   ├── DampedDragAnimation.kt             # 弹簧拖拽/按压动画（开关/滑块/标签栏）
            │   ├── InteractiveHighlight.kt            # AGSL 交互高光（点击处涟漪光）
            │   ├── DragGestureInspector.kt            # 自定义拖拽手势
            │   ├── UISensor.kt                        # 加速度计 → 重力角度（控制中心高光）
            │   └── Ripple.kt                          # AOSP ripple（上游 commonMain 同款）
            ├── components/
            │   ├── GlassFoundation.kt                 # GlassIcon / GlassCard
            │   ├── GlassButtons.kt                    # 按钮·图标按钮·标签·头像·角标·步进器
            │   ├── GlassControls.kt                   # 开关·滑块·分段控件·复选·单选·进度条·活动指示器
            │   ├── GlassBars.kt                       # 底部标签栏·导航栏·搜索框·输入框
            │   ├── GlassList.kt                       # 列表组·列表项·分隔线
            │   └── GlassOverlays.kt                   # 弹窗·提示框·操作表·轻提示·浮层菜单·选择器·控制中心
            └── screens/
                ├── SplashScreen.kt                    # 液态玻璃加载动画
                └── CatalogScreen.kt                   # 3 个标签页 + 26 个组件展示 + 浮层联动
```

### 4.3 玻璃材质唯一真源（`core/glass/GlassMaterials.kt`）

所有组件的玻璃参数都从这里取，**每条配方都标注了上游来源文件**，数值逐字照搬：

| 材质 | 参数（原文） | 上游来源 |
|---|---|---|
| `Button` | `vibrancy(); blur(2.dp); lens(12.dp, 24.dp)` | `components/LiquidButton.kt` |
| `BottomBar` | `vibrancy(); blur(8.dp); lens(24.dp, 24.dp)` | `components/LiquidBottomTabs.kt`（栏背景） |
| `selectionIndicator(p)` | `lens(10.dp*p, 14.dp*p, chromaticAberration = true)` | `components/LiquidBottomTabs.kt`（指示器） |
| `toggleKnob(p)` | `blur(8.dp*(1-p)); lens(5.dp*p, 10.dp*p, chromaticAberration = true)` | `components/LiquidToggle.kt` |
| `sliderKnob(p)` | `blur(8.dp*(1-p)); lens(10.dp*p, 14.dp*p, chromaticAberration = true)` | `components/LiquidSlider.kt` |
| `dialog(light)` | `colorControls(brightness = 0.2/0, saturation = 1.5); blur(16.dp/8.dp); lens(24.dp, 48.dp, depthEffect = true)` | `destinations/DialogContent.kt` |
| `NavigationBar` | `vibrancy(); blur(4.dp); lens(16.dp, 32.dp)` | `tutorials/glass-bottom-bar` |
| `BottomSheet` | `vibrancy(); blur(4.dp); lens(24.dp, 48.dp, depthEffect = true)` | `tutorials/glass-bottom-sheet` |
| `Card` | `vibrancy(); lens(16.dp, 32.dp)` | `destinations/LazyScrollContainerContent.kt` |
| `controlCenterItem(p)` | `vibrancy(); lens(24.dp*p, 48.dp*p, depthEffect = true)` | `destinations/ControlCenterContent.kt` |

配套的上游默认值（同样未改）：开关/滑块轨道色 `0xFF787878 @20%`（深色 `0xFF787880 @36%`）、
开关强调色 `0xFF34C759 / 0xFF30D158`、滑块强调色 `0xFF0088FF / 0xFF0091FF`、
弹窗容器 `0xFFFAFAFA @60%` / `0xFF121212 @40%`、遮罩 `0xFF29293A @23%` / `0xFF121212 @56%`、
控制中心表面 `Black @5%` + `HighlightStyle.Default(angle = 重力角, falloff = 2f)`。

> 唯一一处「非逐字」的写法：进度条前导玻璃旋钮使用 `sliderKnob(1f)`（即上游表达式在 `progress = 1` 取值），
> 因为进度条不是拖拽控件、没有 press 过程。代码中已注释说明。

### 4.4 26 个 iOS 系统组件清单

1 卡片 `GlassCard`｜2 按钮 `GlassButton`（透明/表面/着色三态）｜3 图标按钮 `GlassIconButton`｜
4 开关 `GlassToggle`｜5 滑块 `GlassSlider`｜6 步进器 `GlassStepper`｜7 分段控件 `GlassSegmentedControl`｜
8 输入框 `GlassTextField`｜9 搜索框 `GlassSearchBar`｜10 导航栏 `GlassNavBar`｜
11 底部标签栏 `GlassBottomTabBar`｜12 弹窗 `GlassDialog`｜13 提示框 `GlassAlert`｜
14 操作表 `GlassActionSheet`｜15 列表项 `GlassListItem`(+`GlassListGroup`/`GlassListDivider`)｜
16 进度条 `GlassProgressBar`｜17 活动指示器 `GlassActivityIndicator`｜18 复选框 `GlassCheckbox`｜
19 单选按钮 `GlassRadioButton`｜20 标签 `GlassChip`｜21 头像 `GlassAvatar`｜22 角标 `GlassBadge`｜
23 轻提示 `GlassToast`｜24 浮层菜单 `GlassPopoverMenu`｜25 选择器 `GlassPicker`｜26 控制中心 `GlassControlCenter`

其中 10、11 就是应用外壳本身（顶部导航栏 + 底部标签栏），第 3 个标签页负责触发 12/13/14/23/24 浮层。

**交互实现同样照搬上游**：开关/滑块/分段控件的 press 过程由 `DampedDragAnimation`（弹簧 + 速度阻尼 + 形变）
驱动，玻璃从 `blur` 过渡到 `lens(..., chromaticAberration = true)`，并叠加
`Highlight.Ambient`、`Shadow`、`InnerShadow`、白色表面；标签栏的选中指示器还用
上游的「隐形着色副行 + `rememberCombinedBackdrop(backdrop, tabsBackdrop)`」技巧实现强调色玻璃。

### 4.5 启动动效（`LiquidGlassApp.kt`）

```
加载阶段 1.6s：SplashScreen（面板 = dialog 材质 + Highlight.Plain；轨道 = BottomBar 材质；旋钮 = Button 材质）
        ↓ AnimatedVisibility 交叉过渡，时长 1200ms、插值器 EaseOutCubic
主界面：CatalogScreen（淡入 + scaleIn(0.94 → 1.0)），启动页同时 fadeOut + scaleOut(1.0 → 1.12)
```

关键点：**两屏共用同一个 `LayerBackdrop`（壁纸层）**，过渡期间两屏的玻璃都在折射同一层背景，
所以玻璃质感连续、不会跳变。浮层与面板内部的按钮统一用 `exportedBackdrop` 实现「玻璃叠玻璃」，
避免上游文档警告的递归绘制崩溃。

---

## 5. 构建与验证（实测证据，非纸面）

### 5.1 构建命令

```bash
cd LiquidGlassShowcase
ANDROID_HOME=<android-sdk> ./gradlew clean :app:assembleRelease
```

一键构建成功（`BUILD SUCCESSFUL`），产物：`app/build/outputs/apk/release/app-release.apk`。

### 5.2 产物校验

```
package: name='com.liquidglass.showcase' versionCode='1' versionName='1.0.0'
minSdkVersion:'31'          targetSdkVersion:'34'      compileSdkVersion='37'
application-label:'IOS26'
launchable-activity: com.liquidglass.showcase.MainActivity
V2 Signer: certificate DN: CN=Liquid Glass Showcase, OU=Demo, O=LiquidGlass, C=CN   （apksigner verify 通过）
```

### 5.3 着色器完整性核验（证明 1:1 复用）

反编译 APK 的 `classes.dex` 后检索字符串，四个着色器的关键标识全部存在：

```
circleMap 4 · dispersionIntensity 2 · chromaticAberration 5 · refractionHeight 7
RoundedRectRefractionWithDispersion 2 · DefaultHighlightShaderString 1 · AmbientHighlightShaderString 1
```

### 5.4 未能验证的部分（诚实声明）

沙箱内**没有模拟器/真机**，因此只验证到「编译 → R8 → 打包 → 签名 → 着色器入库」，
**未做真机运行时的目视验收**。建议接手方在 Android 13+ 设备上安装 `IOS26.apk` 复核视觉表现。

---

## 6. 环境与踩坑记录（接手方务必知道）

1. **AGP 9 内置 Kotlin 支持**：不能再应用 `org.jetbrains.kotlin.android` 插件，否则报
   `The 'org.jetbrains.kotlin.android' plugin is no longer required for Kotlin support since AGP 9.0`。
   只需 `com.android.application` + `org.jetbrains.kotlin.plugin.compose`；也无需 `compileOptions`/`kotlin{}` 块
   （上游 `androidApp` 同样如此）。
2. **`ImageVector.Builder.path(pathData = ...)` 在 Compose 1.12 已不可用**（只剩 `path(pathBuilder)` 重载），
   用 `addPath(addPathNodes("M..."), stroke = ...)` 替代（本工程 `GlassIcons.kt` 即如此）。
3. **`material-icons-extended` 已停更**（1.7.8 冻结）→ 改为自绘矢量图标，零图标依赖。
4. **沙箱网络需要代理**：Gradle wrapper 下载发行包要用 `GRADLE_OPTS="-Dhttps.proxyHost=... -Dhttps.proxyPort=..."`，
   Gradle 本体的依赖下载用命令行 `-Dhttp(s).proxyHost/Port`。本地正常联网环境**无需**这些参数。
5. **签名**：`keystore/liquidglass-demo.jks` 是**演示用自签名**（口令均为 `liquidglass`），正式发布请替换并妥善保管。
6. **minSdk 31 的降级行为**：AGSL `RuntimeShader` 需 API 33+，库内部 `isRuntimeShaderSupported()` 会自动跳过
   `lens()`，Android 12 上只剩色彩/模糊（blur 需 API 31+，`isRenderEffectSupported()`）。
   这是库的原生行为，不是缺陷。
7. **玻璃叠玻璃禁止递归**：同一层不能既 `layerBackdrop` 又 `drawBackdrop`（上游文档注明会 RenderThread SIGSEGV），
   必须用 `exportedBackdrop`。
8. Kotlin 局部变量会**遮蔽** `GraphicsLayerScope` 同名成员（如 `alpha`），命名时注意。

---

## 7. 参考与归属

- 上游仓库：<https://github.com/Kyant0/AndroidLiquidGlass>（Apache-2.0，Copyright 2025 Kyant）
- 上游文档：<https://kyant.gitbook.io/backdrop>
- 形状库：<https://github.com/Kyant0/Shapes>（Apache-2.0）
- 本工程 `LICENSE`（Apache-2.0）与 `NOTICE`（逐项列明复用来源）已随源码提供；
  壁纸 `wallpaper_light.webp` 取自上游示例资源。

---

## 8. 附录：完整对话原文（供接手 AI 判断用户偏好）

### 8.1 用户消息（按时间顺序，原文）

1. **（首条需求）** "开始开发前必须执行完整调研流程，禁止在未理解实现原理的情况下盲目开发：1. 完整通读指定GitHub仓库的README、官方文档、全部示例代码与AGSL着色器源码，彻底掌握其液态玻璃效果的渲染原理、核心参数定义、层级调用逻辑与默认参数值，完全对齐仓库的实现方案后再启动开发。2. 所有引入的辅助依赖、UI组件库必须先在GitHub检索验证，仅选用星标≥1000且处于活跃维护状态的高星开源项目，禁止使用小众、低星、停止维护的第三方库。3. 全程严格遵循仓库的原生实现逻辑，禁止自行修改玻璃折射强度、色散系数、模糊半径、边缘高光亮度、圆角形变等任何核心视觉参数，禁止自创任何玻璃效果实现方式。基于 GitHub 仓库 https://github.com/Kyant0/AndroidLiquidGlass 开发一款原生安卓 APK……（5 条具体要求见第 1 章）……然后你还可以问我几个问题"

2. **（决策）** 用户在 4 选 1 提问中的回答：使用 kyant-shapes（与仓库 1:1）／Maven 官方构件／保持 minSdk 31 + 原生自动降级／要实测编译出 APK。

3. **（交付形态）** "只把APK文件给我就行了"

4. **（改名）** "没有呀，这个工作区我看不到这个还有就是把这个名字改成IOS26"

5. **（交接）** "好，我知道原因了，你现在把你做好的所有源码工程解释说明，就是这一段时间你做的所有动作和我说的所有动作，你全部要放在一个压缩包里面，我会去给另一个AI,他就能知道了，我的意思就是我要把我和你的这个对话完整的搬到另一个对话框里面，所以请给我一个压缩包……然后放在这个账号下的一个仓库里面，那个占位仓库"
   （消息中附带过一个 GitHub token —— **未记录在本仓库，建议吊销**）

6. **（清空仓库）** "你需要先把这个仓库里面的其他内容删掉，再把这个完整交接文档和源码文件全部放上去，这样好，让我直接复制那个仓库链接给AI, AI就知道了"

### 8.2 助手的关键动作（按时间顺序）

1. 克隆 `Kyant0/AndroidLiquidGlass`（`kmp` 分支）→ 通读库源码、示例组件、`androidApp`、CI 配置、GitBook 全部教程与 API 页；
   用 `git diff 2.0.0 HEAD -- backdrop/` 证明 2.0.0 与主干一致；用 GitHub API 核验星标与活跃度（发现 ShApEs 43★、icons-extended 停更）。
2. 用 `AskUserQuestion` 提 4 个决策问题（见第 2 章）。
3. 搭建单模块 Gradle 工程（AGP 9.3.2 / Kotlin 2.4.10 / Compose 1.12.0 / compileSdk 37），
   移植上游工具类（`DampedDragAnimation`、`InteractiveHighlight`、`DragGestureInspector`、`UISensor`、`Ripple`，仅改包名），
   把全部玻璃配方集中到 `GlassMaterials.kt` 并标注来源，实现 26 个组件 + 启动动效 + 目录页。
4. 沙箱内安装 Android SDK（cmdline-tools + platforms;android-37.0 + build-tools;37.0.0），
   修复 AGP9 内置 Kotlin、ImageVector API、括号笔误等编译问题，跑通 `clean :app:assembleRelease`，
   校验签名 / minSdk / targetSdk / DEX 内着色器字符串。
5. 按用户要求把应用名改为 **IOS26**、重新出包；随后生成本交接文档与仓库内容。

### 8.3 当前状态与建议的下一步

- ✅ 需求 1–5 全部完成并实测通过构建；❌ 唯一缺口是**真机视觉验收**。
- 可选后续：深色壁纸/跟随系统主题、把 `backdrop` 源码内联为本地模块便于逐行调着色器、
  增加更多组件或交互动效、替换正式签名与包名后发布。