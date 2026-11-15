SafeEvents.definitions(event => {
  event.addCustomContainer('kubejs:sicc_pouch', 'kubejs:textures/item/sicc_pouch.png', 5, 5, {
    translation_key: 'item.kubejs.sicc_pouch',
    filter: {
      mode: 'whitelist',
      tags: ['forge:nuggets', 'forge:dyes', 'forge:dusts', 'forge:gunpowder', 'minecraft:trim_templates'],
      items: [
        'minecraft:paper',
        'minecraft:enchanted_book',
        'minecraft:netherite_upgrade_smithing_template',
        'minecraft:gunpowder',
        'minecraft:blaze_powder',
        'minecraft:bone_meal',
        'minecraft:sugar',
        'minecraft:flower_banner_pattern',
        'minecraft:creeper_banner_pattern',
        'minecraft:skull_banner_pattern',
        'minecraft:mojang_banner_pattern',
        'minecraft:globe_banner_pattern',
        'minecraft:piglin_banner_pattern',
      ]
    }
  })
})
