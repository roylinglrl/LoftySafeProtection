**简体中文** | [English](kubejs8.en.md)

# Minecraft 26.1.2 的 KubeJS 8 扩展

在 `kubejs/server_scripts/` 中使用 `SafeEvents.definitions`。该事件在 JSON 数据包定义加载之后执行，可添加新定义、覆盖同 ID 的定义，也可移除数据包提供的定义。

```js
SafeEvents.definitions(event => {
  event.addSafeBox('kubejs:my_safe', 'lofty_safe_protection:textures/safebox/gold_safe_box.png', 4, 3, {
    translation_key: 'item.kubejs.my_safe',
    filter: {
      mode: 'blacklist',
      items: ['minecraft:tnt'],
      tags: ['minecraft:logs']
    },
    experience: {
      enabled: true,
      capacity: 500
    }
  })

  event.addCustomContainer('kubejs:my_pouch', 'lofty_safe_protection:textures/item/map_pouch.png', 4, 4, {
    filter: {
      mode: 'whitelist',
      items: ['minecraft:paper', 'minecraft:enchanted_book'],
      tags: ['c:nuggets']
    }
  })

  event.removeSafeBox('example:old_safe')
  event.removeCustomContainer('example:old_pouch')
})
```

| 方法 | 参数 |
| --- | --- |
| `addSafeBox` | `id, texture, columns, rows, options` |
| `removeSafeBox` | `id` |
| `addCustomContainer` | `id, texture, columns, rows, options` |
| `removeCustomContainer` | `id` |

`options` 可以省略。尺寸参数为**列数、行数**，例如 `4, 3` 表示高 3、宽 4。支持 1～9 列、1～6 行。贴图参数使用包含 `.png` 的完整资源路径。

- `translation_key`：本地化键。省略时由 ID 生成，如 `kubejs:my_pouch` 对应 `item.kubejs.my_pouch`。
- `filter.mode`：`whitelist` 或 `blacklist`，默认为黑名单。`items` 接受物品 ID，`tags` 接受不带 `#` 的物品标签 ID。
- `experience.enabled`、`experience.capacity`：安全箱是否储存经验及经验点数上限；省略时不启用。背包内容器不提供经验储存。

安全箱的全局禁用物品标签仍然生效。脚本使用与 JSON 定义相同的容量和选项校验规则。

## 安装示例

[完整示例](../examples/kubejs8)包含一个 3×4、500 点经验的安全箱和一个 5×5 物资收纳包，贴图使用本模组已有资源，并附有中英文名称。

将 `examples/kubejs8/server_scripts/` 和 `examples/kubejs8/assets/` 中的内容分别复制到实例的同名 `kubejs` 子目录。若已有同名语言文件，合并其中的键值。原有 [SICC 示例](../examples/kubejs)也可继续使用。

```mcfunction
/lofty_safe_protection give kubejs:experience_case
/lofty_safe_protection give kubejs:supply_pouch
```

这些 ID 表示模组的容器定义，使用上述命令获取对应物品。

## 重载与同步

修改脚本后执行 `/reload`。每次重载都先恢复数据包定义，再执行事件；从脚本中删去的新增定义不会残留。定义更新后会同步给客户端，后续打开容器使用新设置。

脚本放在服务端脚本目录；客户端需要本模组，以及提供贴图和本地化文本的资源。新增或修改客户端资源后使用 `F3+T` 重载资源。

本工作区使用 Maven 配置 KubeJS `26.1.2-8.0.8`、Rhino `2101.2.8-build.91` 和 Better Advanced Tooltips `2601.1.0-build.9`，均作为开发依赖，未打入发布 JAR。

验证命令：`./gradlew build verifyServerStartup -PstartupCheck`。隔离服务端会检查添加、覆盖、移除数据包与脚本定义，物品 ID 和标签过滤，经验配置，修改脚本后的实际重载，以及同步数据中的定义内容。
