**简体中文** | [English](README.en.md)

# Lofty Safe Protection

**安全箱与背包内容器，为随身物品提供更方便的收纳方式。**

Lofty Safe Protection 为玩家物品栏添加安全箱侧栏，并提供可在物品栏中右键打开的自定义容器。支持死亡保护、经验储存、多窗口管理、内容预览，以及通过数据包和 KubeJS 扩展容器定义。

## 运行环境

| 项目 | 当前支持 |
| --- | --- |
| Minecraft | 1.21.1 |
| 加载器 | NeoForge 21.1.265 及以上的 1.21.1 版本 |
| Java | 21 |
| 模组 ID | `lofty_safe_protection` |
| 可选兼容 | JEI、KubeJS 7 |

多人游戏时，客户端和服务端均需安装本模组。JEI 与 KubeJS 是可选组件。

## 安全箱

玩家物品栏右下侧提供安全箱图标。未装备时显示默认图标，装备后显示对应安全箱的贴图。点击图标会在物品栏右侧展开安全箱侧栏，打开后原按钮隐藏。

- 提供一个安全箱装备槽，根据已装备安全箱生成矩形储存区域。
- 玩家死亡后，已装备的安全箱及其中物品、已储存经验保留。
- 安全箱中仍有物品或经验时，需要先清空才能卸载或更换。
- 默认禁止安全箱互相嵌套，并禁止基岩、命令方块、屏障、调试棒等物品放入安全箱。
- 物品存入规则可通过每种安全箱的定义和物品标签配置。

### 默认安全箱

尺寸按 **行数 × 列数，也就是高 × 宽** 表示。例如，`2×3` 为高 2 格、横向 3 格。

| 安全箱 | 尺寸 | 物品槽位 | 经验上限 | 定义 ID |
| --- | --- | --- | --- | --- |
| 石头安全箱 | 2×2 | 4 | 不启用 | `lofty_safe_protection:case_2x2` |
| 铜安全箱 | 2×3 | 6 | 不启用 | `lofty_safe_protection:case_2x3` |
| 金安全箱 | 2×4 | 8 | 100 | `lofty_safe_protection:case_2x4` |
| 钻石安全箱 | 3×3 | 9 | 300 | `lofty_safe_protection:case_3x3` |
| 绿宝石安全箱 | 3×4 | 12 | 500 | `lofty_safe_protection:case_3x4` |

经验以**经验点数**计量。是否启用经验储存及其上限均由定义配置。

### 经验存取

启用经验储存的安全箱会显示经验条与 `−`、`+` 按钮，分别用于取出和存入经验。

| 操作 | 单次存取量 |
| --- | --- |
| 点击 | 1 点 |
| Shift + 点击 | 10 点 |
| Ctrl + 点击 | 50 点 |
| Shift + Ctrl + 点击 | 尽可能多地存入或取出 |

## 背包内容器

背包内容器是不可堆叠的收纳物品。鼠标未拿取物品时，右键即可在当前物品栏界面打开对应浮层，也支持放在安全箱、箱子、木桶、潜影盒等常规容器中打开。

- 支持同时打开多个容器，每个窗口独立保存内容。
- 新窗口默认出现在原物品右侧，向右下方展开；关闭后重新打开会恢复默认位置。
- 拖动标题可以移动窗口，点击窗口可将其置顶。
- 再次右键原物品，或点击窗口右上角 `×`，只关闭对应窗口；Esc 关闭最上层窗口。
- 打开期间原物品槽位会高亮，并锁定正常拾取与快捷交换操作。
- 关闭物品栏时会关闭所有浮层；物品被移走或绑定失效时关闭对应窗口。

内容保存在容器物品中。背包内容器本身遵循普通物品的死亡掉落规则；放在已装备安全箱中时，随安全箱一起保留。

默认提供 **4×4 地图包**，可收纳空地图和已绘制地图。仓库还提供一个通过 KubeJS 定义的 **5×5 SICC 收纳包示例**，用于收纳金属粒、染料、粉尘、纸、旗帜图案、锻造模板和附魔书。

## 收纳与界面体验

- **内容预览**：按住 Shift 悬停在自定义容器物品上，可查看约 50% 尺寸的槽位预览；已装备安全箱也支持预览。
- **容量状态**：物品提示框显示容量及已用格数，满格时改变数值颜色。
- **一键整理**：合并可堆叠物品并排列，保留物品的附魔、名称和其他数据。
- **存入同类物品**：将主背包中与容器已有物品相同的物品存入，跳过快捷栏和打开中的容器原物品，并遵守过滤规则与剩余容量。
- **紧凑工具栏**：整理与存入同类图标位于关闭按钮左侧。
- **不兼容反馈**：拿着不允许存入的物品悬停对应槽位时，显示红色边框。
- **JEI 避让**：为安全箱按钮、展开侧栏和所有已打开的容器浮层预留区域。
- **像素风界面**：背景使用九宫格拼接，配合展开动画和拼接式经验条。

## 数据包扩展

两类容器都通过 JSON 定义，支持自定义 ID、贴图、本地化键、矩形容量和物品过滤。

| 内容 | 数据包路径 |
| --- | --- |
| 安全箱定义 | `data/<命名空间>/safeboxes/<名称>.json` |
| 背包内容器定义 | `data/<命名空间>/inventory_containers/<名称>.json` |
| 安全箱默认禁用物品标签 | `data/lofty_safe_protection/tags/item/blacklisted_items.json` |

定义的 `id` 应与命名空间及文件路径对应。容量支持 1～9 列、1～6 行；JSON 中的 `columns` 表示列数，`rows` 表示行数。

安全箱定义示例：

```json
{
  "id": "example:custom_safe",
  "translation_key": "item.example.custom_safe",
  "texture": "example:textures/item/custom_safe.png",
  "capacity": {
    "columns": 4,
    "rows": 3
  },
  "filter": {
    "mode": "blacklist",
    "items": ["minecraft:bedrock"],
    "tags": ["example:forbidden_items"]
  },
  "experience": {
    "enabled": true,
    "capacity": 300
  }
}
```

将其保存为 `data/example/safeboxes/custom_safe.json`。贴图和本地化文本通过资源包提供。

`filter.mode` 可使用 `whitelist` 或 `blacklist`，`items` 为物品 ID 列表，`tags` 为物品标签 ID 列表，标签 ID 不加 `#`。背包内容器使用相同的容量和过滤结构，需填写 `translation_key`，并不使用安全箱专属的 `experience` 设置。

## KubeJS 7 扩展

将脚本放入 `kubejs/server_scripts/`，通过 `SafeEvents.definitions` 添加、覆盖或移除定义。事件在数据包定义加载后执行，因此可移除来自其他数据包的安全箱或背包内容器。修改后使用 `/reload` 重载。

```js
SafeEvents.definitions(event => {
  event.addSafeBox('example:custom_safe', 'example:textures/item/custom_safe.png', 4, 3, {
    translation_key: 'item.example.custom_safe',
    filter: {
      mode: 'blacklist',
      items: ['minecraft:bedrock'],
      tags: []
    },
    experience: {
      enabled: true,
      capacity: 300
    }
  })

  event.addCustomContainer('example:map_bag', 'example:textures/item/map_bag.png', 4, 4, {
    translation_key: 'item.example.map_bag',
    filter: {
      mode: 'whitelist',
      items: ['minecraft:map', 'minecraft:filled_map'],
      tags: []
    }
  })

  event.removeSafeBox('example:unwanted_safe')
  event.removeCustomContainer('example:unwanted_bag')
})
```

添加方法的尺寸参数依次为**列数、行数**。最后的选项对象可以省略；未指定本地化键时，会按 ID 生成，例如 `example:map_bag` 对应 `item.example.map_bag`。

定义写法可参考仓库中的 [安全箱 JSON](src/main/resources/data/lofty_safe_protection/safeboxes/case_2x4.json)、[地图包 JSON](src/main/resources/data/lofty_safe_protection/inventory_containers/map_pouch.json) 和 [SICC KubeJS 脚本](examples/kubejs/server_scripts/sicc_pouch.js)。完整 SICC 示例与配套贴图、本地化文件位于 [examples/kubejs](examples/kubejs)。

## 获取物品

默认安全箱和地图包位于模组独立的创造模式物品栏中。管理员也可使用模组命令，根据定义 ID 获取物品：

```mcfunction
/lofty_safe_protection give lofty_safe_protection:case_2x4
/lofty_safe_protection give lofty_safe_protection:map_pouch
```

加载 SICC 示例后，可使用：

```mcfunction
/lofty_safe_protection give kubejs:sicc_pouch
```

## 开发与构建

使用 Java 21，在项目根目录执行：

```powershell
.\gradlew.bat build
```

构建产物位于 `build/libs/`。开发客户端通过以下命令启动：

```powershell
.\gradlew.bat runClient
```

开发运行环境通过 Maven 加载 JEI、KubeJS 和 Rhino，并将仓库内的 KubeJS 示例复制到开发客户端目录。

## 许可证

模组代码采用 [MIT 许可证](LICENSE)。Gradle 启动文件相关的许可证与声明位于 [LICENSES](LICENSES)。
