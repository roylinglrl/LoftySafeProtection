# Lofty Safe Protection

**保护重要物品，让随身收纳更方便。**

Lofty Safe Protection 为玩家物品栏带来安全箱侧栏和可右键打开的背包内容器。随身保护贵重物品、分类整理小物件，并通过可拖动的多窗口直接管理储存内容。

## 安全箱：死亡后保留重要物品

点击物品栏右下侧的图标，即可展开安全箱侧栏。装备一个安全箱后，玩家死亡时会保留安全箱、箱内物品和已经储存的经验。箱内仍有物品或经验时，需要先清空才能卸载或更换。

默认提供五种安全箱，尺寸按**高 × 宽**表示：

- **石头安全箱**：2×2，4 格。
- **铜安全箱**：2×3，6 格。
- **金安全箱**：2×4，8 格，可储存 100 点经验。
- **钻石安全箱**：3×3，9 格，可储存 300 点经验。
- **绿宝石安全箱**：3×4，12 格，可储存 500 点经验。

经验以点数计量。使用 `−` / `+` 按钮取出或存入经验，配合 Shift、Ctrl 可分别操作 10 点、50 点，同时按住两者则尽可能多地存取。

## 背包内容器：在物品栏中直接打开

右键不可堆叠的容器物品，即可打开对应收纳浮层。支持从背包、安全箱，以及箱子、木桶、潜影盒等常规容器界面中打开。

- 同时打开多个容器，各自独立操作与关闭。
- 拖动标题调整位置，点击窗口置顶。
- 再次右键原物品关闭对应窗口，Esc 关闭最上层窗口。
- 默认内置 **4×4 地图包**，用于收纳空地图和已绘制地图。

背包内容器的内容随物品保存，并遵循普通物品的死亡掉落规则；放在已装备安全箱中时，随安全箱保留。

## 更顺手的收纳体验

- **Shift 悬停预览**：以约半尺寸查看容器内部槽位。
- **一键整理**：合并可堆叠物品，保留名称、附魔等物品数据。
- **存入同类**：快速收纳主背包中的同类物品，跳过快捷栏并遵守容量与过滤规则。
- **容量状态**：显示已用格数，满格时改变数值颜色。
- **清晰的操作反馈**：原槽位高亮、不兼容物品的红色边框、展开动画。
- **JEI 避让**：为安全箱侧栏及多个收纳窗口预留空间。

## 为整合包作者提供扩展

使用 JSON 数据包定义安全箱和背包内容器，自由配置 ID、贴图、本地化键、矩形容量，以及基于物品 ID 或物品标签的白名单、黑名单。安全箱是否支持经验储存、经验上限也可单独设置。

可选安装 **KubeJS 7**，通过 `SafeEvents.definitions` 添加、覆盖或移除定义，包括移除其他数据包提供的安全箱与容器。

**可以前往 GitHub 查看示例的定义写法：**

- [安全箱 JSON 定义示例](https://github.com/roylinglrl/LoftySafeProtection/blob/main/src/main/resources/data/lofty_safe_protection/safeboxes/case_2x4.json)
- [地图包 JSON 定义示例](https://github.com/roylinglrl/LoftySafeProtection/blob/main/src/main/resources/data/lofty_safe_protection/inventory_containers/map_pouch.json)
- [5×5 SICC 收纳包 KubeJS 示例](https://github.com/roylinglrl/LoftySafeProtection/tree/main/examples/kubejs)
- [完整配置与使用说明](https://github.com/roylinglrl/LoftySafeProtection#readme)

SICC 收纳包是仓库中的可选脚本示例，展示如何为金属粒、染料、粉尘、纸、旗帜图案、锻造模板和附魔书配置专用收纳规则。

## 运行环境

**Minecraft 1.21.1 · NeoForge 21.1.265 及以上的 1.21.1 版本 · Java 21**

多人游戏需要客户端与服务端均安装本模组。JEI 和 KubeJS 为可选组件。

[GitHub 源码与说明](https://github.com/roylinglrl/LoftySafeProtection) · [问题反馈](https://github.com/roylinglrl/LoftySafeProtection/issues)
