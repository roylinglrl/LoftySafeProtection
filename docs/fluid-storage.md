**简体中文** | [English](fluid-storage.en.md)

# 流体储存

Minecraft 1.21.1 NeoForge 的背包内容器支持独立流体槽。每个槽只储存一种流体，不同数据组件的流体不会混合；多个槽可以储存同一种流体。

默认提供 `lofty_safe_protection:fluid_container`，拥有两个槽，每槽 4000 mB（4 桶），合计 8 桶，可容纳最多两种流体。物品位于模组的创造模式物品栏中，也可使用命令获取：

```mcfunction
/lofty_safe_protection give lofty_safe_protection:fluid_container
```

## 定义

在 `data/<命名空间>/inventory_containers/<名称>.json` 中添加 `fluids`：

```json
{
  "id": "example:fluid_container",
  "translation_key": "item.example.fluid_container",
  "texture": "minecraft:textures/item/bucket.png",
  "fluids": {
    "slots": 4,
    "capacity": 4000
  }
}
```

`slots` 表示槽数，支持 1～9。`capacity` 表示**每槽容量**，单位为 mB，1000 mB 为一桶。上述示例可储存 16 桶、最多四种流体。未配置 `fluids` 的旧容器保持原有行为。

流体专用容器可省略 `capacity`，或将物品容量设为 `{"columns":0,"rows":0}`。若需要同时储存物品，再添加普通物品容量，例如 `"capacity":{"columns":4,"rows":3}`。已有物品过滤规则只影响物品槽。

每个流体槽横向显示，固定为 64×16 像素，多个槽优先上下排列，屏幕高度不足时自动分列；流体从左向右填充，保留贴图底部的红色刻度。贴图资源路径为 `lofty_safe_protection:textures/gui/fluid_slot.png`。物品格数量变化时不会拉伸流体槽；面板会补足容纳流体槽所需的宽度。流体区域位于物品格下方，专用流体容器只显示流体区域。

## 存取

右键容器物品打开窗口后，将桶或储液物品拿到鼠标上：

- 左键流体槽，将物品中的流体存入该槽。
- 右键流体槽，从该槽取液到鼠标拿着的物品。
- 每次操作处理一个物品；支持储罐的部分液量转移，桶按完整一桶转移。
- 堆叠空桶取液时，装满的桶会放进背包；背包没有空间时保持原状。

支持水桶、熔岩桶和牛奶桶，并通过 NeoForge 的 `IFluidHandlerItem` 接口兼容其他储液物品。本模组的液体容器也会向其他模组提供该流体接口。

流体保存在容器物品的数据组件中，移动物品、关闭窗口、保存和读取世界均保留。将液体容器放进已装备的安全箱后，其内容会随安全箱保留。减少槽数、容量或移除流体配置时，已有流体不会被截断，旧内容仍可取出。

## KubeJS 7

`addCustomContainer` 的最后一个选项对象支持相同的 `fluids` 设置：

```js
SafeEvents.definitions(event => {
  event.addCustomContainer('kubejs:fluid_container', 'minecraft:textures/item/bucket.png', 0, 0, {
    fluids: { slots: 4, capacity: 4000 }
  })
})
```

修改数据包或服务端脚本后使用 `/reload`。本地化键省略时按定义 ID 生成。

## 验证

`./gradlew build` 运行回归测试。`./gradlew verifyFluidServer -PfluidCheck` 使用隔离服务端，验证原版菜单中的桶存取、满背包处理及安全箱内液体容器的存档保留。测试文件与世界均位于 `src/test` 和 `build`，不进入发布 JAR。
