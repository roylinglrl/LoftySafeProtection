[简体中文](kubejs8.md) | **English**

# KubeJS 8 Integration for Minecraft 26.1.2

Use `SafeEvents.definitions` in `kubejs/server_scripts/`. The event runs after JSON data pack definitions load. Scripts can add definitions, replace definitions with the same ID, and remove definitions supplied by data packs.

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

| Method | Arguments |
| --- | --- |
| `addSafeBox` | `id, texture, columns, rows, options` |
| `removeSafeBox` | `id` |
| `addCustomContainer` | `id, texture, columns, rows, options` |
| `removeCustomContainer` | `id` |

`options` is optional. Dimensions are **columns, then rows**: `4, 3` means three rows and four columns. Supported sizes are 1–9 columns and 1–6 rows. Textures use complete resource paths including `.png`.

- `translation_key`: the localization key. If omitted, it is generated from the ID: `kubejs:my_pouch` becomes `item.kubejs.my_pouch`.
- `filter.mode`: `whitelist` or `blacklist`, defaulting to blacklist. `items` takes item IDs; `tags` takes item tag IDs without `#`.
- `experience.enabled`, `experience.capacity`: whether a safe box stores XP and its capacity in XP points. Omit to disable XP storage. Portable containers do not store XP.

The global safe box blacklist still applies. Scripts use the same size and options validation as JSON definitions.

## Installing the Example

The [complete example](../examples/kubejs8) defines a three-row, four-column safe box with 500 XP capacity and a 5×5 Supply Pouch. It uses bundled mod textures and includes English and Chinese names.

Copy the contents of `examples/kubejs8/server_scripts/` and `examples/kubejs8/assets/` into the corresponding instance `kubejs` subdirectories. Merge translation entries if those language files already exist. The existing [SICC example](../examples/kubejs) also works with KubeJS 8.

```mcfunction
/lofty_safe_protection give kubejs:experience_case
/lofty_safe_protection give kubejs:supply_pouch
```

These IDs identify storage definitions. Use the commands above to obtain their items.

## Reloading and Synchronization

Run `/reload` after editing scripts. Each reload restores data pack definitions before running the event, so definitions deleted from scripts do not remain registered. Updated definitions are synchronized to clients and used when containers are subsequently opened.

Scripts belong in the server script directory. Clients need this mod and the resources that supply textures and translations. Use `F3+T` after adding or changing client resources.

This workspace configures KubeJS `26.1.2-8.0.8`, Rhino `2101.2.8-build.91`, and Better Advanced Tooltips `2601.1.0-build.9` through Maven. They are development dependencies and are not bundled in the release JAR.

Verification: `./gradlew build verifyServerStartup -PstartupCheck`. The isolated server checks additions, replacements, removals of data pack and script definitions, item ID and tag filters, XP configuration, live script reloads, and the contents of synchronized definitions.
