# 02 · Backdrop 分层架构

> 第 01 章讲了「玻璃要满足哪 6 条光学线索」。
> 这一章讲**更底层的那个问题**：玻璃要折射的**那张图**是从哪来的、什么时候被画的、谁先谁后、
> 以及为什么"分层"错了就一定会出现黑块、上一帧残影、或者干脆什么都不显示。
>
> **一句话结论：`drawBackdrop` 只是"采样器"，`layerBackdrop` 才是"录制器"。没有录制器，采样器采到的是空气。**

---

## 1. 三个核心对象，别混为一谈

Backdrop 2.0.0 里只有三类东西，搞清楚它们的角色，后面就顺了。

| 角色 | 类型 | 怎么造 | 作用 |
|---|---|---|---|
| **只读接口** | `Backdrop` | 由库提供 | `drawBackdrop` 的入参类型。它只有"你能读我"的能力，没有"往我这里写"的能力 |
| **录制实现** | `LayerBackdrop` | `rememberLayerBackdrop()` | 真正持有那块**离屏纹理**。用 `Modifier.layerBackdrop(它)` 挂在某个节点上，**那个节点每帧画什么，纹理里就是什么** |
| **派生 Backdrop** | 也是 `Backdrop` | `rememberCombinedBackdrop(...)` / `rememberBackdrop(...)` / `drawBackdrop(exportedBackdrop = ...)` | 不自己持有纹理，而是"在别人的纹理上做一层组合/变换"后再给别人采样 |

**关键区分：`rememberLayerBackdrop()` 是"新建一块画布"；`Modifier.layerBackdrop()` 是"把画笔接到这块画布上"。**
很多人只写了第一个，于是画布永远是空的 —— 这就是"玻璃里什么都没有"的第一大原因。

```kotlin
val backdrop = rememberLayerBackdrop()          // ① 新建画布（空的）

Image(
    modifier = Modifier
        .layerBackdrop(backdrop)                // ② 把这块节点接上画布 → 从此它画的内容被录进去
        .fillMaxSize()
)

GlassCard(backdrop = backdrop)                  // ③ 别人来采样
```

---

## 2. 录制：`Modifier.layerBackdrop` 到底录了什么

### 2.1 语义

`Modifier.layerBackdrop(backdrop)` 是一个 **draw modifier**：

1. 它给宿主节点开一块**离屏图层**（offscreen layer）；
2. 宿主节点**自己 + 它的全部子节点**画进这块图层；
3. 图层画完后，**整张图被写进 `backdrop` 持有的那块纹理**。

所以「录什么」取决于**你把 modifier 挂在哪个节点上**：

```kotlin
// ✅ 正确：挂在"真正画背景"的那个节点上 → 纹理里是壁纸
Image(painter = wallpaper, modifier = Modifier.fillMaxSize().layerBackdrop(backdrop))

// ❌ 错误：挂在一个空 Box 上 → 纹理里是透明
Box(Modifier.layerBackdrop(backdrop))

// ❌ 错误：挂在玻璃自己的节点上 → 玻璃录自己、又折射自己 = 反馈环
Box(Modifier.layerBackdrop(backdrop).drawBackdrop(backdrop))
```

### 2.2 坐标空间：屏幕坐标系，不是局部坐标系

图层记录的是**根坐标系（root / 屏幕坐标）下的画面**，`drawBackdrop` 采样时也用同一套坐标。
所以你**不需要**做任何坐标换算——把玻璃放在屏幕任意位置，它自动采到自己头上那块背景。

这也是为什么本仓库的 `IosWallpaper`、`IosHomeScreen` 都直接 `fillMaxSize()`：
全屏录、局部采，天然对齐。

### 2.3 成本：这是整套方案唯一的大开销

录一帧 = **一次全屏离屏渲染**。它的代价与屏幕像素数成正比，和"录的内容复杂度"关系不大。

由此得到本仓库最重要的一条工程规则（见 [IosShell.kt](../LiquidGlassShowcase/app/src/main/java/com/liquidglass/showcase/ios/IosShell.kt#L77-L99)）：

```kotlin
// 只有"上面真的有东西要读它"的时候，才挂 layerBackdrop
val surfaceIsRead = state.isLocked || state.controlCenter || state.notificationCenter

Box(
    Modifier
        .fillMaxSize()
        .then(if (surfaceIsRead) Modifier.layerBackdrop(surfaceBackdrop) else Modifier)
) { /* 主屏 + 打开的 App */ }
```

- 用户在主屏上正常滑动手势时，**没有任何浮层在读它** → 不录 → 零额外开销；
- 下拉控制中心 / 锁屏出现时 → 才开录。
- 静态页面挂着 `layerBackdrop` 不动，纯粹白烧 GPU。

> 上游（Kyant0）的 Demo 页少、内容简单，可以无脑常开；
> 做系统级壳子**必须**做这种条件录制，否则一直有一层全屏离屏 pass 在跑。

### 2.4 时机：录制节点必须"先画完，再被采样"

绘制顺序 = 组合顺序（`Box` 的子节点按书写顺序画）。所以**内容层必须写在浮层之前**：

```kotlin
Box {
    // ① 先画：内容层（被录进 surfaceBackdrop）
    Box(Modifier.layerBackdrop(surfaceBackdrop)) {
        IosHomeScreen(...)
        IosOpenAppWindow(...)
    }

    // ② 后画：浮层（去采样上一步录好的纹理）
    IosControlCenter(backdrop = overlayBackdrop, ...)
}
```

如果写反了，浮层会采到**上一帧**（甚至空白）的内容 —— 表现为"打开控制中心时，玻璃里是主屏的残影/黑块"。

---

## 3. 组合：`rememberCombinedBackdrop` —— "浮层要看到下层 + 下下层"

### 3.1 问题

本仓库有两块录制层（见 [IosScaffold.kt](../LiquidGlassShowcase/app/src/main/java/com/liquidglass/showcase/ios/IosScaffold.kt#L120-L148)）：

| 层 | 录制内容 | 谁采样它 |
|---|---|---|
| `wallpaperBackdrop` | 壁纸 | 主屏里的玻璃（Dock 卡片等）、打开 App 后的玻璃 |
| `surfaceBackdrop` | 主屏 / 打开的 App（浮层出现时才录） | 没有直接采样者，它就是给组合用的 |
| `overlayBackdrop` = 前两者的组合 | 壁纸 **+** 主屏/App | 锁屏、控制中心、通知中心 |

为什么浮层不能只采 `surfaceBackdrop`？
因为 `surfaceBackdrop` 只录了"主屏/App"这一层的内容，**不含壁纸**；而主屏绝大部分是透明的，
只采它 → 玻璃后面一片空。为什么不能只采 `wallpaperBackdrop`？
因为那样玻璃里就看不到主屏上的 App 图标了 —— 而真实 iOS 下拉控制中心时，**主屏图标是被模糊、被扭曲的**。

### 3.2 解法

```kotlin
val overlayBackdrop = rememberCombinedBackdrop(wallpaperBackdrop, surfaceBackdrop)
```

语义是**层的叠加（painter's algorithm）**：从下往上依次 `wallpaperBackdrop` → `surfaceBackdrop`，
把结果当作一块新的虚拟纹理交给采样者。于是浮层玻璃里同时有壁纸和主屏图标，且整块一起被折射。

> `rememberCombinedBackdrop` 是变长参数：两块走 `Combined2Backdrops`，三块走 `Combined3Backdrops`，
> 再多走 `CombinedBackdrops`。有三层以上的世界观时（壁纸 / 内容 / 上一级浮层）可以继续往上叠。

### 3.3 本仓库的分层全景图

```
IosShell                                    z 轴（从下到上）
├─ IosWallpaper ──layerBackdrop──▶ wallpaperBackdrop        ← 层 0：壁纸
│
├─ Box ──layerBackdrop(条件)────▶ surfaceBackdrop          ← 层 1：屏幕内容
│   ├─ IosHomeScreen（Dock 采 wallpaperBackdrop）
│   └─ IosOpenAppWindow（App 内玻璃采 appBackdrop）
│
├─ IosStatusBar                （不录，浮在最上面，保证覆盖 App 时仍可读）
├─ IosLockScreen               ← 采 overlayBackdrop
├─ IosAppSwitcher
├─ IosControlCenter            ← 采 overlayBackdrop
├─ IosNotificationCenter       ← 采 overlayBackdrop
├─ IosPullDownZones / IosHomeGesture
└─ IosDynamicIsland            （永不玻璃化，就是一块纯黑硬件挖孔）
```

对应的代码：`IosShell` 里 `overlayBackdrop` 在 [IosShell.kt L75](../LiquidGlassShowcase/app/src/main/java/com/liquidglass/showcase/ios/IosShell.kt#L75) 建，
内容层在 [L83-L99](../LiquidGlassShowcase/app/src/main/java/com/liquidglass/showcase/ios/IosShell.kt#L83-L99)，
浮层在 [L102-L141](../LiquidGlassShowcase/app/src/main/java/com/liquidglass/showcase/ios/IosShell.kt#L102-L141)。

### 3.4 为什么 App 内部换了一块 backdrop

打开 App 后，窗口是**不透明的系统底色**（`Color(0xFFF2F2F7)`）。此时：

- 窗口**内部**的玻璃如果继续采 `wallpaperBackdrop`，就会折射"藏在 App 后面的壁纸"——
  物理上说不通（光穿不过不透明底），视觉上会发飘；
- 正确做法是给窗口**自己**录一层：[IosAppWindow.kt L109](../LiquidGlassShowcase/app/src/main/java/com/liquidglass/showcase/ios/IosAppWindow.kt#L109) 的 `appBackdrop`，
  记录"系统底色 + App 顶部渐变色"，App 内所有玻璃采它（[L171-L188](../LiquidGlassShowcase/app/src/main/java/com/liquidglass/showcase/ios/IosAppWindow.kt#L171-L188)）。

**规则：玻璃一定要折射"自己在视觉上真正压在什么上面"，而不是"最先录的那一层"。**

---

## 4. 派生：`rememberBackdrop` —— 把一块纹理"变形"后再用

`rememberBackdrop(source) { drawBackdrop -> ... }` 造的是一块**派生纹理**：
lambda 里调用 `drawBackdrop()` 就等于"把 `source` 画出来"，你可以在外面包任意 `DrawScope` 变换。

本仓库用它解决一个很具体的问题：**开关 / 滑块的"丸粒"看起来应该是一滴从轨道里被吸起来的水滴**，
而 `drawBackdrop` 只能采样"原始的、未变形的"背景。

做法（[GlassControls.kt L184-L194](../LiquidGlassShowcase/app/src/main/java/com/liquidglass/showcase/components/GlassControls.kt#L184-L194)）：

```kotlin
val trackBackdrop = rememberLayerBackdrop()   // 记录"轨道"这一小块

// 轨道自己：透明地录进去（能被上方丸粒采到）
Box(Modifier.layerBackdrop(trackBackdrop).clip(Capsule()).size(64.dp, 28.dp))

// 丸粒：采样 = 背景 + (被压扁 2/3 的轨道)
drawBackdrop(
    backdrop = rememberCombinedBackdrop(
        backdrop,                                   // 真实背景
        rememberBackdrop(trackBackdrop) { drawBackdrop ->
            val scaleX = lerp(2f / 3f, 0.75f, progress)   // press 时轨道"被抽走"
            val scaleY = lerp(0f, 0.75f, progress)
            scale(scaleX, scaleY) { drawBackdrop() }      // ← 变换后画出来
        }
    ),
    shape = { Capsule() },
    effects = GlassMaterials.toggleKnob(...)
)
```

效果：静止时丸粒里能看到一整条轨道；按下时轨道被横向抽走 1/3、纵向压扁 → 看起来就像轨道里的液体被吸进了丸粒。

> 这是全仓库最"炫技"的一段代码。**先把它读三遍。** 读懂了，`Backdrop` 的抽象层次你就算通了。
> 滑块的写法完全一样，见 [GlassControls.kt L350-L360](../LiquidGlassShowcase/app/src/main/java/com/liquidglass/showcase/components/GlassControls.kt#L350-L360)。

---

## 5. `exportedBackdrop` —— 玻璃上再叠玻璃

### 5.1 问题

需要在**一块玻璃之上**再放一块玻璃：比如对话框里的两个按钮（按钮坐在对话框玻璃上），
底部动作表的每一行（行坐在面板玻璃上），启动页的进度滑块（滑块坐在面板玻璃上）。

天真写法是"父节点录一层、子节点采它"，但父节点自己就是 `drawBackdrop` 出来的，
它的绘制结果包含"已经折射过的背景"，而这份结果**需要一个录制器才能被子节点采到**——
于是很容易写成"父子都挂 `layerBackdrop`"，然后互相反馈。

### 5.2 解法：`drawBackdrop(exportedBackdrop = ...)`

```kotlin
val panelBackdrop = rememberLayerBackdrop()

Column(
    Modifier.drawBackdrop(
        backdrop = backdrop,          // 采真实背景
        shape = { panelShape },
        effects = GlassMaterials.dialog(isLightTheme),
        exportedBackdrop = panelBackdrop   // ← 我"画完之后"的样子导出成一块新纹理
    )
) {
    // 子按钮采样 panelBackdrop → 折射的是"父玻璃"，不是背景
    DialogActionButton(backdrop = panelBackdrop, ...)
}
```

`exportedBackdrop` 的语义是：**这个节点自己（含 effects 结果）作为一块新的 Backdrop 暴露出去。**
它是库官方推荐的"玻璃叠玻璃"写法，比手搓双层 `layerBackdrop` 安全，因为它由节点自身生命周期管理，
不会出现"录一帧、采一帧"的竞态。

本仓库三处用到：

| 位置 | 代码 |
|---|---|
| 对话框两个按钮 | [GlassOverlays.kt L137](../LiquidGlassShowcase/app/src/main/java/com/liquidglass/showcase/components/GlassOverlays.kt#L137)（`panelBackdrop`） |
| 底部动作表每一行 | [GlassOverlays.kt L357](../LiquidGlassShowcase/app/src/main/java/com/liquidglass/showcase/components/GlassOverlays.kt#L357)（`sheetBackdrop`） |
| 启动页面板 + 进度条 | [SplashScreen.kt L88](../LiquidGlassShowcase/app/src/main/java/com/liquidglass/showcase/screens/SplashScreen.kt#L88)（`panelBackdrop`） |

---

## 6. 反馈环：唯一一个"看起来该那么写、但绝对不能那么写"的地方

### 6.1 什么是反馈环

```
节点 A：Modifier.layerBackdrop(bd)   ← 把 A 画进 bd
节点 A：Modifier.drawBackdrop(bd)    ← 又去读 bd
```

A 读的东西里含 A 自己 → 每帧自激 → 闪烁 / 糊成一坨黑。

**一条铁律：同一个 `LayerBackdrop`，录制节点和采样节点必须是两个不同的节点。**

### 6.2 上游的"影子记录节点"技巧

底栏的"选中指示器"需要**折射底栏本身**（指示器是底栏上的一块玻璃，要能糊掉底栏的文字/图标）。

做法（[GlassBars.kt L253-L291](../LiquidGlassShowcase/app/src/main/java/com/liquidglass/showcase/components/GlassBars.kt#L253-L291)）：

```kotlin
val tabsBackdrop = rememberLayerBackdrop()

// ① 影子节点：alpha(0f) → 用户看不见，但它每帧照常被画 → 把"底栏上的图标文字"录进 tabsBackdrop
Row(
    Modifier
        .alpha(0f)                     // ← 关键：完全透明
        .layerBackdrop(tabsBackdrop)
        .drawBackdrop(backdrop = backdrop, shape = { Capsule() }, effects = { ... })
) { tabContent }

// ② 真正的指示器：采样 combined(背景, 影子层) → 于是它折射到了"底栏上的图标"
Box(
    Modifier.drawBackdrop(
        backdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop),
        shape = { Capsule() },
        effects = GlassMaterials.selectionIndicator(progress)
    )
)
```

为什么能成立：`alpha(0f)` 只影响**最终合成时的可见度**，**不影响它被录进图层**。
所以影子节点"存在但不可见"，既提供了录制内容，又不会被用户看到两份图标。

> 这是一个非常"反直觉但极其有用"的技巧，值得单独立一条笔记：
> **要折射某块内容，但不想重复显示它 → 复制一份、设 `alpha(0f)`、挂 `layerBackdrop`。**

---

## 7. 什么时候该录、录哪一层？决策树

按顺序问自己 4 个问题：

```
Q1  这块玻璃"视觉上"压在什么上面？
      ├─ 壁纸/渐变          → 采 wallpaperBackdrop
      ├─ 不透明的 App 底色  → 新建 appBackdrop，采它
      └─ 另一块玻璃         → 用 exportedBackdrop

Q2  那个"被压的东西"有没有被录？
      ├─ 有 → 直接用
      └─ 没有 → 造 rememberLayerBackdrop()，挂到"画那个东西的节点"上

Q3  需要"同时看到两层"吗？（浮层既要看到内容层，又要看到壁纸）
      └─ 要 → rememberCombinedBackdrop(下层, 上层)

Q4  需要"看到的内容被变形"吗？（水滴吸走轨道）
      └─ 要 → rememberBackdrop(源) { drawBackdrop -> transform { drawBackdrop() } }
```

---

## 8. 坐标、变换与"为什么玻璃会跟着动"

- **录制**发生在根坐标；**采样**也用根坐标。所以玻璃**平移**时，采到的是它新区块下面的背景 → 视觉上"玻璃滑过背景"，正确。
- 如果给玻璃加了 `graphicsLayer { translationX = ... }`（如底栏指示器跟着手指滑），
  它采样的**位置**也跟着变 —— 这正是"液态"感的来源：玻璃在移动中持续折射不同内容。
- **不要**在 `layerBackdrop` 节点上加 `scale/rotation` 来"修画面"。要变换采样的内容，请用 §4 的 `rememberBackdrop`。
- `shadow` / `innerShadow` / `highlight` 都画在**玻璃自身图层**里，不参与背景折射，也不影响 backdrop 内容。

---

## 9. 性能清单（照抄即可）

| 规则 | 原因 |
|---|---|
| 录制节点**条件挂载**（`state.isLocked \|\| ...`） | 每帧全屏离屏 pass 是唯一大开销 |
| 每块 backdrop **只挂一个**录制节点 | 后挂的会覆盖前一个 |
| 录制节点尽量**小**（滑块只录轨道那 64×28） | 离屏图层面积 ∝ 成本；本仓库的 `trackBackdrop` 就是范例 |
| 采样节点可以很多 | 采样只是着色器读取，比录制便宜得多 |
| 避免在 `effects` 里做逐帧随机/IO | `effects` 每帧执行 |
| `lens()` 越强 → `padding` 越大 → 采样范围越大 | 库会自动扩 padding（`getPadding/setPadding`），别手动再叠一层 clip 把它裁掉 |

---

## 10. 本章错误对照表

| 症状 | 根因 | 修法 |
|---|---|---|
| 玻璃里全黑 / 全透明 | 只 `rememberLayerBackdrop()` 没挂 `layerBackdrop` | 把 modifier 挂到真正画背景的节点 |
| 玻璃里是"上一帧"的内容 | 采样节点画在录制节点之前 | 内容层写在浮层前面 |
| 画面闪烁 / 糊成黑块 | 同一节点既录又采（反馈环） | 拆成两个节点，或改用 `alpha(0f)` 影子节点 |
| 浮层玻璃里只有壁纸、看不到主屏图标 | 只采了 `wallpaperBackdrop` | 用 `rememberCombinedBackdrop(wallpaper, surface)` |
| 浮层玻璃里只有空白 | 采了 `surfaceBackdrop` 但录制被条件关掉了 | 检查条件录制开关是否覆盖了当前状态 |
| 玻璃叠玻璃变成实心色块 | 子节点采了背景而不是父玻璃 | 父加 `exportedBackdrop`，子采它 |
| 打开 App 后玻璃显得"飘" | App 内玻璃还在采壁纸 | 给 App 的不透明底新建 `appBackdrop` |
| 滑动时卡顿 | 无条件录制 | 改条件录制 |

---

## 11. 小结

```
rememberLayerBackdrop()  →  一块空画布
Modifier.layerBackdrop() →  把某个节点接上画布（每帧录一帧）
Modifier.drawBackdrop()  →  采样画布（可以在别处、别层、别的时间）
rememberCombinedBackdrop →  把两块画布叠成一块（解决"浮层要看到内容层"）
rememberBackdrop         →  把一块画布变形后再用（解决"水滴吸走轨道"）
exportedBackdrop         →  把"玻璃自己"变成画布（解决"玻璃叠玻璃"）
alpha(0f) + layerBackdrop→  隐形录制（解决"要折射但不想重复显示"）
```

**下一步**：读 `03-API-完全参考.md`，把 `drawBackdrop` 的每一个参数逐个过一遍。
