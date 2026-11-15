[简体中文](fluid-storage.md) | **English**

# Fluid Storage

Portable containers on Minecraft 1.21.1 NeoForge support separate fluid tanks. Each tank holds one fluid and keeps fluids with different data components separate. Multiple tanks may hold the same fluid.

The included `lofty_safe_protection:fluid_container` has two tanks, each holding 4000 mB (four buckets): eight buckets total and up to two fluid types. Find it in the mod's creative tab or use:

```mcfunction
/lofty_safe_protection give lofty_safe_protection:fluid_container
```

## Definitions

Add `fluids` to `data/<namespace>/inventory_containers/<name>.json`:

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

`slots` is the number of tanks, from 1 to 9. `capacity` is the capacity **of each tank**, measured in mB; 1000 mB equals one bucket. This example holds 16 buckets and up to four fluid types. Existing definitions without `fluids` retain their behavior.

Fluid-only containers may omit item `capacity` or use `{"columns":0,"rows":0}`. To store both items and fluids, add a normal item grid, such as `"capacity":{"columns":4,"rows":3}`. Item filters apply to item slots.

Each tank is a fixed horizontal 64×16 bar. Multiple tanks stack vertically and wrap into columns when screen height is insufficient, with left-to-right fluid filling and the supplied red scale markings. The background is at `lofty_safe_protection:textures/gui/fluid_slot.png`. Changing the item grid does not stretch tanks; the panel widens to fit them. Tanks appear below item slots, or by themselves for fluid-only containers.

## Transfers

Open the container with a right-click, then pick up a bucket or fluid container with the cursor:

- Left-click a tank to deposit fluid from the held item.
- Right-click a tank to withdraw into the held item.
- Each click processes one item. Fluid containers may transfer partial amounts; buckets transfer complete buckets.
- Filled buckets from a stack of empty buckets go into the player inventory. If no space is available, the operation leaves everything unchanged.

Water, lava, and milk buckets are supported. Other items integrate through NeoForge's `IFluidHandlerItem` capability. This mod's fluid containers also expose that capability for other mods.

Contents persist in the container item's data components through item movement and world saves. Containers placed in an equipped safe box retain their fluid with the safe box. Reducing tank counts or capacity, or removing fluid settings, does not discard existing fluid; it remains available for withdrawal.

## KubeJS 7

The final `addCustomContainer` options object accepts the same `fluids` settings:

```js
SafeEvents.definitions(event => {
  event.addCustomContainer('kubejs:fluid_container', 'minecraft:textures/item/bucket.png', 0, 0, {
    fluids: { slots: 4, capacity: 4000 }
  })
})
```

Use `/reload` after editing data packs or server scripts. Omitted translation keys are generated from definition IDs.

## Verification

`./gradlew build` runs regression tests. `./gradlew verifyFluidServer -PfluidCheck` uses an isolated server to verify native menu transfers, full-inventory handling, and protected storage persistence. Tests and test worlds stay under `src/test` and `build` and are excluded from release JARs.
