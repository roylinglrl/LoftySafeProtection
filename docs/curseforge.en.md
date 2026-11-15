# Lofty Safe Protection

**Keep your valuables safe and your portable storage organized.**

Lofty Safe Protection adds a safe box side panel and right-clickable portable containers to your inventory. Protect valuable items, organize small supplies, and manage storage through draggable windows without leaving the inventory screen.

## Safe Boxes: Keep Important Items Through Death

Click the icon beside the lower-right area of your inventory to expand the safe box panel. Your equipped safe box, its contents, and stored experience are retained when you die. Empty its items and experience before removing or replacing it.

Five safe boxes are included. Sizes are written as **height × width**:

- **Stone Safe Box:** 2×2, 4 slots.
- **Copper Safe Box:** 2×3, 6 slots.
- **Gold Safe Box:** 2×4, 8 slots, up to 100 XP points.
- **Diamond Safe Box:** 3×3, 9 slots, up to 300 XP points.
- **Emerald Safe Box:** 3×4, 12 slots, up to 500 XP points.

Use the `−` / `+` buttons to withdraw or deposit XP. Click to transfer 1 point, hold Shift for 10, Ctrl for 50, or both to transfer as much as possible. Capacities are measured in XP points, rather than levels.

## Portable Containers: Open Storage Inside Your Inventory

Right-click a non-stackable container item with an empty cursor to open its storage window. This also works from safe boxes and supported standard container screens, including chests, barrels, and shulker boxes.

- Open multiple containers at once and manage each independently.
- Drag a window by its title and click it to bring it to the front.
- Right-click the source item again to close its window, or press Esc to close the topmost window.
- Includes a **4×4 Map Pouch** for empty and filled maps.

Portable container contents are saved in the item and follow normal item death-drop behavior. Containers stored inside an equipped safe box are retained with that safe box.

## Convenient Inventory Management

- **Shift-hover previews:** view a container's grid at approximately half size.
- **One-click sorting:** merge compatible stacks while preserving names, enchantments, and other item data.
- **Deposit matching items:** move matching items from your main inventory into storage, skipping the hotbar and respecting filters and available space.
- **Capacity information:** see occupied slots, with a different count color when full.
- **Clear feedback:** highlighted source slots, red borders for incompatible items, and opening animations.
- **JEI exclusion areas:** space is reserved for the safe box panel and multiple storage windows.

## Built for Modpack Customization

Define safe boxes and portable containers through JSON data packs. Configure IDs, textures, translation keys, rectangular capacities, and item ID or item tag whitelists and blacklists. Experience storage and capacity are configurable for each safe box.

Optionally install **KubeJS 6** and use `SafeEvents.definitions` to add, replace, or remove definitions, including those supplied by other data packs.

**Visit GitHub for working definition examples:**

- [Safe box JSON example](https://github.com/roylinglrl/LoftySafeProtection/blob/forge/1.20.1/src/main/resources/data/lofty_safe_protection/safeboxes/case_2x4.json)
- [Map Pouch JSON example](https://github.com/roylinglrl/LoftySafeProtection/blob/forge/1.20.1/src/main/resources/data/lofty_safe_protection/inventory_containers/map_pouch.json)
- [5×5 SICC Pouch KubeJS example](https://github.com/roylinglrl/LoftySafeProtection/tree/main/examples/kubejs)
- [Full configuration and usage documentation](https://github.com/roylinglrl/LoftySafeProtection/blob/forge/1.20.1/README.en.md)

The SICC Pouch is an optional repository example demonstrating dedicated storage rules for nuggets, dyes, dusts, paper, banner patterns, smithing templates, and enchanted books.

## Requirements

**Minecraft 1.20.1 · Forge 47.4.22 or newer for Minecraft 1.20.1 · Java 17**

Install the mod on both the client and server for multiplayer. JEI and KubeJS are optional.

[Source and documentation](https://github.com/roylinglrl/LoftySafeProtection) · [Report an issue](https://github.com/roylinglrl/LoftySafeProtection/issues)
