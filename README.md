# 苹果液态玻璃（Apple Liquid Glass）实现总纲

> **这份文档是写给 AI 助手看的。**
> 目标只有一个：**让任何一个 AI，只读这个仓库，就能在原生安卓上做出苹果 iOS 26 那种"液态玻璃"效果**，
> 并且做对——不是"半透明 + 高斯模糊"那种廉价仿制品，而是真正有**折射、色散、边缘高光、重力响应**的玻璃。
>
> 全部结论来自一次真实的、跑通并编译出 APK 的完整开发过程（Jetpack Compose + Backdrop 2.0.0）。
> 文档里的每一个参数、每一行代码、每一个坑，都是实际验证过的，不是推测。
>
> 最后更新：2026-10-01

---

## 0. 先读这一段：30 秒建立正确心智模型

**如果你只记住一件事，请记住这个：**

> 液态玻璃不是一种"材质属性"，而是**对玻璃背后已经渲染完成的画面做一次实时的几何折射**。

这句话决定了整个架构：

| 错误的理解 | 正确的理解 |
|---|---|
| 给一个 Box 加 `alpha = 0.3f` + `Modifier.blur()` | 先要有"背后那层画面"的**离屏纹理**，再对它做采样变形 |
| 玻璃组件自己画自己 | 玻璃组件**只负责采样别人的纹理** |
| `Modifier.blur()` 模糊自身内容 | 模糊的是**背景**，自身内容保持锐利（这正是玻璃感的关键） |

所以整个技术栈只有三步：

```
① 把背景"录"进一块离屏纹理   →   rememberLayerBackdrop() + Modifier.layerBackdrop()
② 玻璃组件去"采样"这块纹理   →   Modifier.drawBackdrop(backdrop = ...)
③ 采样时按光学公式做变形     →   effects = { vibrancy(); blur(..); lens(..) }
```

**没有第 ① 步，第 ② 步什么也画不出来。** 这是 90% 的失败案例的根因。

---

## 1. 十条铁律（开发前必读，全是我踩出来的）

1. **没有 backdrop 就没有玻璃。**
   `drawBackdrop` 的第一参数是一个 `Backdrop` 对象。你必须先用 `rememberLayerBackdrop()` 造一个，
   再用 `Modifier.layerBackdrop(它)` 挂到"背景节点"上，它才有内容可采样。

2. **渲染管线顺序是固定的：`colorFilter → blur → lens`。**
   所以写 `effects` 代码块时，`vibrancy()` / `colorControls()` 永远第一行，`blur()` 第二行，`lens()` 最后一行。
   把 `lens()` 写在前面 = 在模糊前的画面上折射，边缘会糊掉。

3. **`lens()` 的两个参数是 dp，不是像素。** 必须 `12f.dp.toPx()`。
   第一个参数是**折射高度**（透镜"凸起"多高 → 决定折射的作用范围），
   第二个是**折射量**（偏移多强）。两者都变大 = 玻璃更厚、更像一颗水滴。

4. **色散（`chromaticAberration`）只在"动态"时开。**
   静止的玻璃开色散 → 边缘出现红蓝彩边 → 一眼假。
   上游的做法是：拖动/按下时 `progress → 1`，色散随 progress 打开。

5. **`Highlight` 不是描边，是"沿边缘按角度渐隐的高光带"。**
   想要苹果那种"边缘一道亮光"，靠 `Highlight`，不要自己 `border(1.dp, Color.White)`——
   后者是硬边、均匀的，看起来像塑料框。

6. **`onDrawSurface` 是玻璃的"染色"，不是背景色。必须极端克制。**
   控制中心 5% 黑、Dock 22%~34% 白、通知卡 14% 白。
   一旦超过 50%，玻璃就变成一块实心圆角矩形，折射全被盖住。

7. **分层：内容层只能折射它的"下层"；上层浮层要折射"下层的下层"，必须合并 backdrop。**
   用 `rememberCombinedBackdrop(a, b)`。这是"控制中心打开时能把主屏图标糊掉"的唯一正确做法。

8. **永远不要制造"自己折射自己"的环。**
   一个节点不能既 `layerBackdrop(自己)` 又 `drawBackdrop(自己)`，也不能在同一个 `LayerBackdrop` 上
   挂两个记录节点（后一个会覆盖前一个）。上游的做法是：用一个 `alpha(0f)` 的**影子节点**去记录，
   真正绘制玻璃的是另一个节点。

9. **记录层 = 每帧一次全屏离屏渲染，是这套方案唯一的性能开销。**
   所以要做**条件记录**：只有当上层浮层真的要读它的时候，才挂 `layerBackdrop`。
   静态页面挂着不动，白烧 GPU。

10. **`RoundedRectangle` 用 `com.kyant.shapes`，不是 `androidx.compose.foundation.shape`。**
    前者是苹果的 **G2 连续曲率**（超椭圆/squircle），后者是普通圆角。
    苹果所有图标的圆角都是 G2 连续的，用错了形状，光看轮廓就知道是假的。

---

## 2. 仓库地图

```
experiments/                      ← 你在这里
├── README.md                     ← 本文档（总纲，先读这个）
├── docs/
│   ├── 01-液态玻璃的光学模型.md      ← 原理：折射/色散/模糊/高光/阴影到底在算什么
│   ├── 02-Backdrop-分层架构.md      ← 架构：LayerBackdrop / Combined / 坐标空间 / 防反馈
│   ├── 03-API-完全参考.md          ← API：drawBackdrop 全部参数 + effects + Highlight/Shadow 逐个解释
│   ├── 04-材质配方表.md            ← 配方：12 个已验证材质的精确参数 + 如何自己推导新配方
│   ├── 05-苹果系统外壳实战.md       ← 实战：主屏/锁屏/控制中心/转场 怎么用这套东西拼出来
│   ├── 06-图标与资产管线.md         ← 资产：ui-icons-hub 怎么用（含"苹果应用图标分区"的真实清单）
│   ├── 07-构建与排错.md            ← 工程：环境、编译命令、全部报错与修复
│   └── 08-上一轮交接原文.md         ← 历史：上一轮（26 个组件展示）的完整对话交接，保留备查
└── LiquidGlassShowcase/          ← 完整可编译工程（Jetpack Compose）
```

> ⚠️ **本仓库不包含 APK。** APK 是构建产物，不是源码。
> 要验证效果，请照 [07-构建与排错.md](docs/07-构建与排错.md) 自己编一个：
> `local.properties` 写 `sdk.dir` → `JAVA_HOME` 指向 JDK 17 → `./gradlew assembleRelease`。
> **能编出 1.25 MB 的 `app-release.apk`，就说明整套液态玻璃环境已经复刻成功。**

**源码里最该读的 5 个文件**（按重要性排序）：

| 文件 | 为什么重要 |
|---|---|
| `core/glass/GlassMaterials.kt` | **全部材质的唯一真相来源**：12 个配方的精确参数，每个都标注了上游出处 |
| `core/glass/GlassScaffold.kt` | 最小可用示例：怎么把壁纸录成 LayerBackdrop，18 行 |
| `components/GlassFoundation.kt` | 怎么把 `drawBackdrop` 封装成可复用的 `GlassCard` |
| `components/GlassBars.kt` | **最复杂的范例**：combined backdrop + 影子记录节点 + 动态色散，看会这个就全懂了 |
| `ios/IosShell.kt` | 分层架构的实战：整个苹果系统外壳的 z-order 与 backdrop 分配 |

---

## 3. 五分钟上手：最小可运行示例

这是把"液态玻璃"跑起来所需的**全部代码**。抄进去就能看到效果。

### 3.1 依赖

```kotlin
// gradle/libs.versions.toml
[versions]
backdrop = "2.0.0"       // io.github.kyant0:backdrop —— 液态玻璃引擎
kyantShapes = "1.2.0"    // io.github.kyant0:shapes  —— 苹果 G2 连续曲率形状（backdrop 的传递依赖）

[libraries]
backdrop = { group = "io.github.kyant0", name = "backdrop", version.ref = "backdrop" }
kyant-shapes = { group = "io.github.kyant0", name = "shapes", version.ref = "kyantShapes" }
```

```kotlin
// app/build.gradle.kts
android {
    minSdk = 31          // 硬性要求：AGSL 需要 API 31+
    compileSdk = 37
}
dependencies {
    implementation(libs.backdrop)
    implementation(libs.kyant.shapes)
}
```

> ⚠️ **`minSdk` 必须 ≥ 31。** 这套效果底层是 **AGSL（Android Graphics Shading Language）**
> 运行时着色器，API 31 以下没有 `RuntimeShader`，直接 ClassNotFound。

### 3.2 第一步：把背景录成 backdrop

```kotlin
@Composable
fun WallpaperScaffold(content: @Composable BoxScope.(backdrop: Backdrop) -> Unit) {
    Box(Modifier.fillMaxSize()) {
        // ① 造一块离屏纹理
        val backdrop = rememberLayerBackdrop()

        Image(
            painter = painterResource(R.drawable.wallpaper),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)   // ② 把这个节点每帧的画面录进纹理
        )

        content(backdrop)                  // ③ 把纹理交给上层所有玻璃组件
    }
}
```

**这两行就是整套方案的钥匙。** `layerBackdrop` 修饰的节点，它画了什么，backdrop 里就有什么。

### 3.3 第二步：画一块玻璃

```kotlin
@Composable
fun GlassCard(backdrop: Backdrop, modifier: Modifier = Modifier) {
    Box(
        modifier.drawBackdrop(
            backdrop = backdrop,                       // 采样哪块纹理
            shape = { RoundedRectangle(32.dp) },       // G2 连续圆角（苹果的 squircle）
            effects = {
                vibrancy()                             // ① 提饱和度，让玻璃"透亮"而不是"发灰"
                lens(16f.dp.toPx(), 32f.dp.toPx())     // ② 折射：玻璃厚度的全部秘密
            }
        )
    )
}
```

### 3.4 第三步：怎么调到"像苹果"

只有三个旋钮在起作用，记住它们的方向：

| 想要的效果 | 改什么 | 方向 |
|---|---|---|
| 玻璃更"厚"、更像一颗水滴 | `lens()` 两个参数一起加 | `16→24`、`32→48` |
| 背景更糊、更"雾" | `blur()` 加 | `2→8→16` |
| 玻璃更"实"、更挡视线 | `onDrawSurface = { drawRect(色) }` 加深 | 白 0.05 → 0.34 |
| 边缘那道亮光更强 | `highlight = { Highlight.Default.copy(alpha = 1f) }` | `0.5→1.0` |

**只有这三个方向。** 其余全是细节。苹果官网那套演示里，控制中心用的是"大 lens + 强高光"，
Dock 用的是"小 lens + 中等染色"，弹窗用的是"大 lens + 大 blur + depthEffect"。
具体数值见 `docs/04-材质配方表.md`。

---

## 4. 一句话回答常见问题

| 问题 | 答案 |
|---|---|
| 为什么我的玻璃是灰的？ | 少了 `vibrancy()`。模糊会让颜色变灰，`vibrancy()` 负责把饱和度提回来 |
| 为什么我的玻璃什么都看不到？ | `onDrawSurface` 颜色太深，把折射盖住了。降到 5%~35% |
| 为什么边缘有红蓝彩边？ | `chromaticAberration = true` 常开了。它只该在按下/拖动时打开 |
| 为什么玻璃边缘是硬边？ | 用了 `border()`。应该用 `highlight = { Highlight.Default }` |
| 为什么圆角看起来不对？ | 用了 `RoundedCornerShape`。应该用 `com.kyant.shapes.RoundedRectangle` |
| 为什么玻璃里是空的/黑的？ | backdrop 没录上内容。检查 `layerBackdrop` 是否挂在了真正画背景的节点上 |
| 为什么打开面板时玻璃里是上一帧的画面？ | 记录节点和绘制节点顺序反了。内容层必须先于浮层绘制 |
| 为什么卡？ | 无条件挂了 `layerBackdrop`。改成"只有需要时才记录" |
| 为什么在低版本手机上闪退？ | `minSdk < 31`，没有 AGSL |
| 参数能自己改吗？ | 能，但要**成比例地改**，见 `docs/04-材质配方表.md` 的"推导新配方"一节 |

---

## 5. 阅读顺序建议

- **完全没接触过** → 读 `01` 光学模型 → `02` 架构 → `03` API → 抄 `03.2/03.3` 的代码
- **只想抄参数** → 直接看 `04-材质配方表.md`
- **要做完整 App** → 看 `05-苹果系统外壳实战.md`，那里面有整个 iOS 外壳的分层图
- **要用图标** → `06-图标与资产管线.md`（含 ui-icons-hub 苹果图标的真实盘点）
- **编译不过** → `07-构建与排错.md`

---

## 6. 上游与版权

- 液态玻璃引擎：[**Kyant0/AndroidLiquidGlass**](https://github.com/Kyant0/AndroidLiquidGlass) → 发布为 Maven 构件 `io.github.kyant0:backdrop:2.0.0`
- 形状库：[**Kyant0/AndroidShapes**](https://github.com/Kyant0/AndroidShapes) → `io.github.kyant0:shapes:1.2.0`（Apache-2.0）
- 本仓库中的 `core/utils/*`、`components/*` 中的若干文件是上游示例的 1:1 移植，文件头保留了原始版权声明
  （`Copyright 2025 Kyant`，Apache-2.0），**请勿删除这些声明**。
- 本仓库新增的代码（`ios/` 目录、`GlassMaterials.kt` 的整理等）为 `Copyright 2026 The Liquid Glass Showcase authors`。
- **所有折射强度、色散系数、模糊半径、高光亮度等核心视觉参数，一律照抄上游，没有自行调参。**
  如果你要改，请在 `docs/04` 里记录你改了什么、为什么。
