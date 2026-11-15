[简体中文](README.md) | **English**

# Lofty Safe Protection

**Protect your valuables and organize portable storage directly in your inventory.**

Lofty Safe Protection adds a safe box side panel to the player inventory, alongside configurable portable containers that open with a right-click. Features include death protection, experience storage, multiple container windows, content previews, and definitions supplied through data packs or KubeJS.

## Requirements

| Component | Current support |
| --- | --- |
| Minecraft | 1.20.1 |
| Mod loader | Forge 47.4.22 or newer for Minecraft 1.20.1 |
| Java | 17 |
| Mod ID | `lofty_safe_protection` |
| Optional integrations | JEI, KubeJS 6 |

Install the mod on both the client and server for multiplayer. JEI and KubeJS are optional.

## Safe Boxes

A safe box icon appears beside the lower-right area of the player inventory. It uses a default texture when no box is equipped and the equipped box's texture otherwise. Clicking it expands the side panel and hides the original button while the panel is open.

- Equip one safe box to access its rectangular storage grid.
- The equipped safe box, its contents, and stored experience are retained when the player dies.
- Empty its items and experience before removing or replacing it.
- Safe boxes cannot be placed inside other safe boxes. Bedrock, command blocks, barriers, debug sticks, and other configured restricted items are blocked by default.
- Each definition can specify allowed or blocked items, with additional restrictions controlled through the safe box blacklist tag.

### Included Safe Boxes

Sizes are written as **rows × columns: height × width**. A `2×3` box has two rows and three columns.

| Safe box | Size | Item slots | XP capacity | Definition ID |
| --- | --- | --- | --- | --- |
| Stone Safe Box | 2×2 | 4 | Disabled | `lofty_safe_protection:case_2x2` |
| Copper Safe Box | 2×3 | 6 | Disabled | `lofty_safe_protection:case_2x3` |
| Gold Safe Box | 2×4 | 8 | 100 | `lofty_safe_protection:case_2x4` |
| Diamond Safe Box | 3×3 | 9 | 300 | `lofty_safe_protection:case_3x3` |
| Emerald Safe Box | 3×4 | 12 | 500 | `lofty_safe_protection:case_3x4` |

Experience is measured in **XP points**, rather than levels. Experience storage and its capacity are configured per definition.

### Experience Transfers

Boxes with experience storage enabled show an experience bar and `−` / `+` buttons for withdrawing and depositing XP.

| Input | Amount per transfer |
| --- | --- |
| Click | 1 point |
| Shift + click | 10 points |
| Ctrl + click | 50 points |
| Shift + Ctrl + click | Transfer as much as possible |

## Portable Inventory Containers

Portable containers are non-stackable storage items. Right-click one with an empty cursor to open a floating window within the current inventory screen. This also works when the item is in a safe box, chest, barrel, shulker box, or another supported standard container interface.

- Open multiple containers at once, each with its own contents.
- Windows initially expand down and right beside their source item. Closing and reopening restores the default position.
- Drag the title to move a window, and click a window to bring it to the front.
- Right-click the source item again or use its window's `×` button to close that window. Esc closes the topmost window.
- Source slots are highlighted and protected against normal pickup and hotbar swaps while open.
- Closing the parent inventory closes all windows. Moving a source item or invalidating its binding closes the corresponding window.

Contents are stored in the container item. Portable containers follow normal item death-drop behavior; when stored in an equipped safe box, they are retained with the safe box.

The mod includes a **4×4 Map Pouch** for empty and filled maps. This repository also includes a **5×5 SICC Pouch KubeJS example** for nuggets, dyes, dusts, paper, banner patterns, smithing templates, and enchanted books.

## Inventory Convenience

- **Content previews:** hold Shift while hovering over a portable container to view its grid at approximately 50% scale. Equipped safe boxes also support previews.
- **Capacity information:** tooltips show capacity and occupied slots, with a different count color when full.
- **One-click sorting:** merge compatible stacks and arrange contents while preserving names, enchantments, and other item data.
- **Deposit matching items:** move matching items from the main inventory into storage, respecting filters and remaining space. The hotbar and source items of open containers are skipped.
- **Compact controls:** sort and deposit-matching icons sit immediately left of the close button.
- **Compatibility feedback:** hovering over a slot while carrying an incompatible item shows a red border.
- **JEI exclusion areas:** space is reserved for the safe box button, expanded panel, and all open container windows.
- **Pixel-style interface:** nine-slice backgrounds, opening animations, and a segmented experience bar.

## Data Pack Definitions

Both storage types use JSON definitions with an ID, texture, translation key, rectangular capacity, and item filters.

| Content | Data pack path |
| --- | --- |
| Safe boxes | `data/<namespace>/safeboxes/<name>.json` |
| Portable containers | `data/<namespace>/inventory_containers/<name>.json` |
| Default safe box blacklist tag | `data/lofty_safe_protection/tags/items/blacklisted_items.json` |

The definition ID must match its namespace and file path. Capacities support 1–9 columns and 1–6 rows. In JSON, `columns` is the width and `rows` is the height.

Example safe box definition:

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

Save this as `data/example/safeboxes/custom_safe.json`. Supply textures and localized text through a resource pack.

Set `filter.mode` to `whitelist` or `blacklist`. `items` contains item IDs, and `tags` contains item tag IDs without a leading `#`. Portable containers use the same capacity and filter structure, require a `translation_key`, and do not use the safe-box-only `experience` setting.

## KubeJS 6 Integration

Place scripts in `kubejs/server_scripts/` and use `SafeEvents.definitions` to add, replace, or remove definitions. The event runs after data pack definitions load, allowing scripts to remove safe boxes or portable containers supplied by other data packs. Use `/reload` after changing scripts.

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

Dimension arguments are **columns, then rows**. The final options object is optional. When no translation key is specified, one is generated from the ID: `example:map_bag` becomes `item.example.map_bag`.

For working definitions, see the [safe box JSON](src/main/resources/data/lofty_safe_protection/safeboxes/case_2x4.json), [Map Pouch JSON](src/main/resources/data/lofty_safe_protection/inventory_containers/map_pouch.json), and [SICC KubeJS script](examples/kubejs/server_scripts/sicc_pouch.js). The complete SICC example, including its texture and localization, is in [examples/kubejs](examples/kubejs).

## Obtaining Items

Default safe boxes and the Map Pouch are available in the mod's creative tab. Administrators can also obtain items by definition ID:

```mcfunction
/lofty_safe_protection give lofty_safe_protection:case_2x4
/lofty_safe_protection give lofty_safe_protection:map_pouch
```

With the SICC example loaded:

```mcfunction
/lofty_safe_protection give kubejs:sicc_pouch
```

## Development and Building

With Java 17, run the following from the project root on Windows:

```powershell
.\gradlew.bat build
```

Build outputs are written to `build/libs/`. Start the development client with:

```powershell
.\gradlew.bat runClient
```

The development runtime loads JEI, KubeJS, and Rhino through Maven, and copies the repository's KubeJS example into the development client directory.

`build` runs regression tests inside an isolated Forge server, including the KubeJS 6 example. Test worlds and reports are written under `build/`.

## License

Mod code is available under the [MIT License](LICENSE). Gradle launcher licenses and notices are in [LICENSES](LICENSES).
