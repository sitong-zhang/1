# 03 · API 完全参考

> 本章逐个拆解 Backdrop 2.0.0 暴露给使用者的全部 API。
> 参数名、预设名、着色器名都是从 **`io.github.kyant0:backdrop:2.0.0` 的构件本身**核对过的
> （`backdrop-api.jar` / `backdrop-runtime.jar` 的类常量池），不是凭印象写的。
>
> 阅读方式：**先看 §1 的骨架，再用 §2–§7 查细节，最后看 §8 的完整签名汇总。**

---

## 1. 骨架：一个 `drawBackdrop` 调用里到底发生了什么

```kotlin
Modifier.drawBackdrop(
    backdrop        = 背景纹理,        // 必须：采谁
    shape           = { 形状 },        // 轮廓：决定折射带的位置 + 裁剪
    effects         = { … },          // 着色管线：颜色 → 模糊 → 折射（顺序固定）
    highlight       = { … },          // 边缘高光（口红效应/菲涅尔）
    shadow          = { … },          // 外投影
    innerShadow     = { … },          // 内暗边（厚度）
    layerBlock      = { … },          // 给"玻璃这一层"套 graphicsLayer 变换
    onDrawBehind    = { … },          // 在采样结果"之下"画
    onDrawSurface   = { … },          // 在采样结果"之上"画（玻璃自身的染色）
    onDrawFront     = { … },          // 在所有东西"最上面"画（描边、箭头等）
    onDrawBackdrop  = { … },          // 完全接管采样结果的绘制（高级）
    exportedBackdrop= 子层纹理,        // 把我自己导出成一块纹理给子节点采样
)
```

执行顺序（**这是理解一切的关键**）：

```
① 按 shape 生成 rounded-rect SDF
② 用 shape 的 padding 扩大采样范围（折射会"向外抓"像素）
③ 采样 backdrop 纹理
④ 对采样结果依次套 effects：  colorFilter/vibrancy/colorControls  →  blur  →  lens
⑤ 把结果裁成 shape
⑥ onDrawBehind → [④的结果] → onDrawSurface
⑦ 依次叠：shadow / innerShadow / highlight
⑧ onDrawFront
⑨ 整层套 layerBlock 的 graphicsLayer
⑩ 若给了 exportedBackdrop，把"本节点最终画面"写进它
```

> 记住 **④ 的顺序由 `effects` 代码块的书写顺序决定**。库不会帮你排序。

---

## 2. `effects` —— 着色管线（`BackdropEffectScope`）

`effects` 的 receiver 是 `BackdropEffectScope`。它同时是：

- 一个 **`Density`**（所以里面能直接 `12f.dp.toPx()`）；
- 一个可以读 **`size` / `shape` / `layoutDirection`** 的作用域；
- 一个**可追加的 RenderEffect 链**（每次调用往链尾 `chain(...)` 追加一段）。

### 2.1 `vibrancy()`

```kotlin
vibrancy()
```

无参。基于 `colorMatrix` 构造一个"提饱和 + 轻微提亮"的滤镜（库里叫 `VibrantColorFilter`）。
**作用：抵消模糊带来的发灰。** 配 `blur()` 使用，几乎无例外。

- 用在哪：几乎所有「浅色玻璃」配方。
- 不需要的时候（比如大面积深色弹窗），改用 `colorControls` 显式控制。

### 2.2 `colorControls(brightness, contrast, saturation)`

```kotlin
colorControls(
    brightness = 0.2f,     // 加减亮度（浅色主题常用 0.2；深色通常 0）
    contrast   = 1f,       // 对比度（上游默认不改）
    saturation = 1.5f      // 饱和度（上游弹窗固定 1.5）
)
```

比 `vibrancy()` 可控。**上游弹窗（dialog）用的就是它**，因为要按明暗主题给不同 brightness。

### 2.3 `colorFilter(colorFilter)` / `opacity(alpha)`

```kotlin
colorFilter(ColorFilter.tint(...))
opacity(0.8f)
```

`colorFilter` 是通用的 Compose `ColorFilter` 入口（灰度、染色、矩阵都行）；
`opacity` 是纯 alpha 乘法。两者与 `vibrancy/colorControls` 同属管路第一阶段（颜色阶段）。

> 注意区分：`onDrawSurface` 里的 `drawRect(color)` 是**画在玻璃上的涂层**（会影响"玻璃是什么颜色"），
> 而 `opacity()` 是**对整个采样结果的透明度**（会连背景一起变淡）。别混用。

### 2.4 `blur(radius, edgeTreatment)`

```kotlin
blur(16f.dp.toPx())
```

| 参数 | 含义 |
|---|---|
| `radius` | 高斯/盒式模糊半径。API 31+ 走 `RenderEffect.createBlurEffect`（`isRenderEffectSupported()`） |
| `edgeTreatment` | 边缘处理策略（防止越界采样出黑边/透明边）。一般**不用改**，库有默认值 |

**`blur` 越大越需要 `vibrancy()` 兜底**，见 §2.1。

### 2.5 `lens(refractionHeight, refractionAmount, depthEffect, chromaticAberration)`

整套效果的心脏。**只有它有"几何折射"能力**，其余都只是颜色/模糊。

```kotlin
lens(
    refractionHeight    = 24f.dp.toPx(),   // 必须 toPx()
    refractionAmount    = 48f.dp.toPx(),   // 必须 toPx()
    depthEffect         = false,
    chromaticAberration = false
)
```

| 参数 | 类型 | 作用 | 何时开 |
|---|---|---|---|
| `refractionHeight` | `Float`（px） | 透镜"隆起高度"→ 折射带宽度 | 一直要 |
| `refractionAmount` | `Float`（px） | 最大像素位移量 → 折射强度 | 一直要 |
| `depthEffect` | `Boolean` | 球面纵深映射，玻璃中间更"鼓" | 只在大面积强暗示材质：**弹窗、控制中心** |
| `chromaticAberration` | `Boolean` | 打开色散（多波段采样） | **只在按下/拖动时**，随 progress 一起归零 |

**色散的实现细节（已从着色器源码核实）**：
库里有两个 AGSL 着色器 —— `RoundedRectRefractionShaderString` 和
`RoundedRectRefractionWithDispersionShaderString`。
后者把背景采样了 **7 次**，位移量分别为带色散偏移的
`+1, +2/3, +1/3, 0, -1/3, -2/3, -1` 倍，对应
**红 / 橙 / 黄 / 绿 / 青 / 蓝 / 紫** 七个谱段，再逐通道合成。

所以"色散"不是简单的 RGB 三通道，而是**七段光谱错位** —— 这就是它动起来像真玻璃的原因。
同时也解释了为什么静止时不能常开：七个谱带常驻会出现肉眼可辨的彩虹边。

**两个着色器共用的 uniform**（等价于告诉你"折射是由什么决定的"）：

```
uniform shader content;              // ← 被录制的背景纹理，以 shader 形式传入
uniform float2 size;                 // 玻璃尺寸
uniform float2 offset;               // 玻璃在根坐标中的位置
uniform float4 cornerRadii;          // 四个角的半径（G2 连续曲率）
uniform float refractionHeight;
uniform float refractionAmount;
uniform float depthEffect;
uniform float chromaticAberration;
```

> 注意第一行 `uniform shader content` —— **你的背景纹理是作为"子着色器"被求值的**（`content.eval(coord)`）。
> 这就是"液态玻璃必须有一块真实背景纹理"的底层原因：它不是贴图叠加，是**几何重采样**。

### 2.6 `renderEffect { … }` / `runtimeShaderEffect(…)`

```kotlin
runtimeShaderEffect(
    shaderString = "half4 main(float2 coord) { … }",   // AGSL
    uniformShaderName = "shader",
    block = { setFloatUniform("amount", 0.5f) }
)
```

逃生舱：自定义 AGSL。`chain` 会把自定义 effect 接在现有链尾。
本仓库**没有使用**它——凡是能用上游配方的，一律用配方。

### 2.7 `effects` 里能读到的上下文

| 属性 | 说明 |
|---|---|
| `size` | 玻璃当前尺寸（px，`Size`） |
| `shape` | 当前 `Shape` |
| `layoutDirection` | LTR / RTL |
| `padding` | 采样外扩量。**`lens()` 会自动把它撑大**（避免折射采到纹理外），一般别动 |
| `renderEffect` | 当前的 effect 链，可读写 |

---

## 3. `shape` —— 必须是 G2 连续曲率

```kotlin
import com.kyant.shapes.RoundedRectangle
import com.kyant.shapes.Capsule

shape = { RoundedRectangle(24f.dp) }   // 苹果 squircle（超椭圆/G2）
shape = { Capsule() }                  // 完全圆头（按钮/底栏/搜索框）
```

**不要**用 `androidx.compose.foundation.shape.RoundedCornerShape`。
两者的区别在**曲率连续性**：

| | 普通圆角（RoundedCornerShape） | G2 连续曲率（kyant shapes） |
|---|---|---|
| 直边与圆角的连接 | 曲率突变（一阶连续） | 曲率连续过渡（二阶连续） |
| 观感 | 一眼"安卓卡片" | 苹果图标那种"饱满"的圆角 |
| 折射带分布 | 圆角处折射会突然中断 | 折射沿轮廓平滑过渡 |

`lens()` 的着色器接收的是 **`cornerRadii: float4`**，说明它按"圆角矩形 SDF"算折射带 ——
形状越接近苹果的超椭圆，折射带的宽度分布就越自然。

> `Capsule` 也来自 `com.kyant.shapes`，等价于"短边一半的圆角矩形"，不是 `CircleShape`。

---

## 4. `highlight` —— 边缘高光

### 4.1 数据结构

```kotlin
data class Highlight(
    val style: HighlightStyle = …,
    val width: Dp,
    val blurRadius: Dp,
    val alpha: Float
)
```

### 4.2 三个预设

| 预设 | 实现 | 特征 | 用在哪 |
|---|---|---|---|
| `Highlight.Default` | `DefaultHighlightShaderString` | **有方向**的斜向高光（`uniform angle` / `uniform falloff`），并在迎着光的一侧带 `layout(color) uniform half4 color` | 绝大多数组件（省略时库自动用） |
| `Highlight.Ambient` | `AmbientHighlightShaderString` | **无彩色**、更均匀的环境光（着色器直接返回 `half4(t,t,t,1)*intensity`） | 大面积平板；按下时的高光 |
| `Highlight.Plain` | 极简 | 几乎不可见 | 弹窗等"不想有方向性亮边"的大玻璃 |

### 4.3 常见写法

```kotlin
highlight = { Highlight.Default }                                  // 常亮默认高光
highlight = { Highlight.Default.copy(alpha = progress) }           // 按下才亮
highlight = { Highlight.Plain }                                    // 弹窗
highlight = {
    Highlight.Ambient.copy(                                          // 更细更柔的环境光
        width = Highlight.Ambient.width / 1.5f,
        blurRadius = Highlight.Ambient.blurRadius / 1.5f,
        alpha = progress
    )
}
```

### 4.4 重力高光（苹果味的关键）

```kotlin
val uiSensor = rememberUISensor()

highlight = {
    Highlight(
        style = HighlightStyle.Default(
            angle = uiSensor.gravityAngle,   // 高光方向跟随手机倾角
            falloff = 2f                     // 衰减曲线，越大收得越快
        )
    )
}
```

`gravityAngle` 来自加速度计的 `atan2(y, x)`，用 `alpha = 0.5f` 一阶低通平滑，初值 `45f`。
实现见 [UISensor.kt](../LiquidGlassShowcase/app/src/main/java/com/liquidglass/showcase/core/utils/UISensor.kt)。
**倾斜手机时高光会"滑"过去** —— 这是最容易被忽略、但最能骗过眼睛的细节。

### 4.5 绝不要用 `border()`

```kotlin
// ❌ 均匀硬边 = 塑料框
Modifier.border(1.dp, Color.White.copy(0.3f), RoundedCornerShape(24.dp))

// ✅ 迎光侧有、背光侧无，且向内渐隐
highlight = { Highlight.Default }
```

---

## 5. `shadow` / `innerShadow`

```kotlin
data class Shadow(val radius: Dp, val offset: DpOffset, val color: Color, val alpha: Float)
data class InnerShadow(val radius: Dp, val offset: DpOffset, val color: Color, val alpha: Float)
```

| 用法 | 代码 | 效果 |
|---|---|---|
| 常亮柔和外投影 | `shadow = { Shadow.Default }` | 玻璃"浮起来" |
| 按下时浮起 | `shadow = { Shadow(alpha = progress) }` | 按压反馈 |
| 明确的小投影 | `shadow = { Shadow(radius = 4f.dp, color = Color.Black.copy(alpha = 0.05f)) }` | 开关丸粒、滑块丸粒 |
| 内暗边（厚度） | `innerShadow = { InnerShadow(radius = 8f.dp * progress, alpha = progress) }` | **亮边 + 内暗边 = 有厚度的实体** |
| 关掉 | `shadow = null` | 控制中心用（避免叠影） |

`innerShadow` 上游**只在按下时**加（`progress` 从 0→1），静止时保持干净。

---

## 6. 其余参数

### 6.1 `layerBlock: GraphicsLayerScope.() -> Unit`

给"玻璃这一层"套 `graphicsLayer`。**这是施加速度感/形变的标准位置**：

```kotlin
layerBlock = {
    val p = pressProgress
    // 按压时轻微横向拉伸，像水被挤压
    val scale = lerp(1f, 1f + 16f.dp.toPx() / size.width, p)
    scaleX = scale
    scaleY = scale
}
```

注意作用域里能读到 `size`（这一层的尺寸）和 `toPx()`（`GraphicsLayerScope` 是 `Density`）。

按钮/底栏还在这里做"速度形变"（velocity squash & stretch）：

```kotlin
val velocity = dampedDragAnimation.velocity / 10f
scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
```

### 6.2 `onDrawBehind` / `onDrawSurface` / `onDrawFront`

三者都是 `DrawScope.() -> Unit`，区别只在**绘制层级**：

```
        ┌─────────────── onDrawFront        ← 最上面（描边、角标）
        ├─────────────── highlight / shadow
        ├─────────────── onDrawSurface      ← 玻璃自身的染色（"这块玻璃什么颜色"）
        ├─────────────── onDrawBehind       ← 玻璃"底下"的垫层
        └─────────────── 折射后的背景
```

**`onDrawSurface` 是玻璃的染色，不是背景色，必须极端克制：**

| 场景 | 值（本仓库实际用的） |
|---|---|
| 控制中心小方块 | `Color.Black @ 5%` |
| Dock / 底栏 | `Color.White @ 22% ~ 34%` |
| 通知卡（锁屏上，直接压壁纸） | `Color.White @ 14%` |
| 通知卡（通知中心里） | `Color.White @ 22%` |
| 导航栏 / 搜索框（浅色） | `Color.White @ 35% ~ 50%` |
| 弹窗 | `dialogContainer`（浅色 `#FAFAFA @ 60%` / 深色 `#121212 @ 40%`） |
| 分段控件 / 底栏指示器 | `Black @ 10%`（浅色）/ `White @ 10%`（深色），且随 press 淡出 |

> **一旦超过 ~50%，折射就全被盖住，玻璃退化成一块实心圆角矩形。**

`onDrawSurface` 里也可以做**叠加绘制**（比如按钮的染色 + 覆盖）：

```kotlin
onDrawSurface = {
    drawRect(tint, blendMode = BlendMode.Hue)   // 先按色相染色
    drawRect(tint.copy(alpha = 0.75f))          // 再叠一层
}
```

### 6.3 `onDrawBackdrop`

完全接管"折射结果"的绘制。默认实现是 `DefaultOnDrawBackdrop`（把上面那条链跑完并画出来）。
除非你要做完全自定义的采样合成，否则**不要传**。

### 6.4 `exportedBackdrop: LayerBackdrop?`

把本节点最终画面导出成一块可采样纹理，供子节点"玻璃叠玻璃"。见 `02-Backdrop-分层架构.md §5`。

---

## 7. 与 `drawBackdrop` 配套的独立 API

### 7.1 `rememberLayerBackdrop()`

```kotlin
@Composable fun rememberLayerBackdrop(): LayerBackdrop
```

新建一块空录制纹理。配合 `Modifier.layerBackdrop(它)` 使用。

### 7.2 `Modifier.layerBackdrop(backdrop: LayerBackdrop)`

把宿主节点（含子树）每帧的画面录进 `backdrop`。**一个 backdrop 只挂一个录制节点。**

### 7.3 `rememberCombinedBackdrop(vararg backdrop: Backdrop)`

```kotlin
rememberCombinedBackdrop(wallpaperBackdrop, surfaceBackdrop)   // 2 块 → Combined2Backdrops
rememberCombinedBackdrop(a, b, c)                              // 3 块 → Combined3Backdrops
```

按 painter's algorithm 从下往上叠。见 `02` 章 §3。

### 7.4 `rememberBackdrop(backdrop) { drawBackdrop -> … }`

```kotlin
rememberBackdrop(source) { drawBackdrop ->
    scale(sx, sy) { drawBackdrop() }   // 把 source 变形后作为新纹理
}
```

见 `02` 章 §4。

### 7.5 `CanvasBackdrop` / `EmptyBackdrop`

- `EmptyBackdrop`：什么都不画的占位（`Backdrop.Empty`）。调试用——把 `backdrop` 换成它，
  玻璃应该"什么都没有但形状正确"，可以快速区分"是 backdrop 没录上"还是"是 effects 写错了"。
- `CanvasBackdrop`：直接给一块 `DrawScope` 自定义绘制的 backdrop。想用代码画背景（而不是录某个节点）时用。

### 7.6 RuntimeShader 工具

```kotlin
isRuntimeShaderSupported()          // API 31+ 才 true
RuntimeShader(agslString)           // 编译一段 AGSL
shader.asComposeShader()            // 转成 Compose 的 Shader
shader.setFloatUniform(name, x, y)
shader.setColorUniform(name, color)
```

`InteractiveHighlight` 就是用这套自己写了一段"圆形径向高光" AGSL，实现"手指按住的位置局部发光"
（[InteractiveHighlight.kt L63-L115](../LiquidGlassShowcase/app/src/main/java/com/liquidglass/showcase/core/utils/InteractiveHighlight.kt#L63-L115)）。

> 低版本兼容：库内部所有 AGSL 调用前都会 `isRuntimeShaderSupported()` 判断，**Android 12 上自动降级**
> （折射/色散消失，模糊仍在）。所以 `minSdk 31` 是安全的，只是 Android 12 看不到折射。

---

## 8. 签名速查（全部来自构件核实）

```kotlin
// ── 主入口 ───────────────────────────────────────────────────────────────
fun Modifier.drawBackdrop(
    backdrop: Backdrop,
    shape: (Density.() -> Shape)? = null,
    effects: (BackdropEffectScope.() -> Unit)? = null,
    highlight: (Density.() -> Highlight)? = null,
    shadow: (Density.() -> Shadow)? = null,
    innerShadow: (Density.() -> InnerShadow)? = null,
    layerBlock: (GraphicsLayerScope.() -> Unit)? = null,
    onDrawBehind: (DrawScope.() -> Unit)? = null,
    onDrawSurface: (DrawScope.() -> Unit)? = null,
    onDrawFront: (DrawScope.() -> Unit)? = null,
    onDrawBackdrop: (DrawScope.() -> Unit)? = DefaultOnDrawBackdrop,
    exportedBackdrop: LayerBackdrop? = null,
): Modifier

// ── effects ─────────────────────────────────────────────────────────────
fun BackdropEffectScope.vibrancy()
fun BackdropEffectScope.colorControls(brightness: Float = 0f, contrast: Float = 1f, saturation: Float = 1f)
fun BackdropEffectScope.colorFilter(colorFilter: ColorFilter)
fun BackdropEffectScope.opacity(alpha: Float)
fun BackdropEffectScope.blur(radius: Float, edgeTreatment: … = default)
fun BackdropEffectScope.lens(
    refractionHeight: Float,
    refractionAmount: Float,
    depthEffect: Boolean = false,
    chromaticAberration: Boolean = false,
)
fun BackdropEffectScope.runtimeShaderEffect(shaderString: String, uniformShaderName: String, block: …)

// ── backdrop 来源 ───────────────────────────────────────────────────────
@Composable fun rememberLayerBackdrop(): LayerBackdrop
fun Modifier.layerBackdrop(backdrop: LayerBackdrop): Modifier
@Composable fun rememberCombinedBackdrop(vararg backdrop: Backdrop): Backdrop
@Composable fun rememberBackdrop(backdrop: Backdrop, onDraw: DrawScope.(drawBackdrop: () -> Unit) -> Unit): Backdrop
val Backdrop.Companion.Empty: Backdrop

// ── 附属值对象 ──────────────────────────────────────────────────────────
data class Highlight(style: HighlightStyle, width: Dp, blurRadius: Dp, alpha: Float) {
    companion object { val Default; val Ambient; val Plain }
}
data class HighlightStyle(…) {
    companion object { fun Default(angle: Float, falloff: Float): HighlightStyle }
}
data class Shadow(radius: Dp, offset: DpOffset, color: Color, alpha: Float) { companion object { val Default } }
data class InnerShadow(radius: Dp, offset: DpOffset, color: Color, alpha: Float) { companion object { val Default } }
```

> ⚠️ **始终使用具名参数调用 `drawBackdrop`。** 参数顺序在不同版本间可能调整，具名调用永远安全。

---

## 9. 本章最容易踩的 7 个 API 误用

| ❌ | 后果 | ✅ |
|---|---|---|
| `lens(24f, 48f)` 漏 `toPx()` | 3x 屏上折射只有 1/3 | `lens(24f.dp.toPx(), 48f.dp.toPx())` |
| `lens` 写在 `blur` 前面 | 折射后越界像素被模糊抹开 → 脏边 | `lens` 永远最后一行 |
| `effects` 里漏 `vibrancy()` | 玻璃发灰 | 配 `blur` 就配 `vibrancy` |
| `chromaticAberration = true` 常开 | 静止时有红蓝彩边 | 绑 `progress`，静止归零 |
| `border()` 代替 `highlight` | 塑料框 | `highlight = { Highlight.Default }` |
| `RoundedCornerShape` | 圆角不像苹果 | `com.kyant.shapes.RoundedRectangle` |
| `onDrawSurface` 用 60% 白 | 折射全被盖住 | 压到 5%~35% |

---

**下一步**：`04-材质配方表.md` —— 12 个已经调好的配方，直接抄。
