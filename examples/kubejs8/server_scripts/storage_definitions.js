SafeEvents.definitions(event => {
  event.addSafeBox('kubejs:experience_case', 'lofty_safe_protection:textures/safebox/gold_safe_box.png', 4, 3, {
    translation_key: 'item.kubejs.experience_case',
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

  event.addCustomContainer('kubejs:supply_pouch', 'lofty_safe_protection:textures/item/map_pouch.png', 5, 5, {
    translation_key: 'item.kubejs.supply_pouch',
    filter: {
      mode: 'whitelist',
      items: ['minecraft:paper', 'minecraft:enchanted_book'],
      tags: ['c:nuggets', 'c:dyes', 'c:dusts']
    }
  })

  event.removeSafeBox('example:obsolete_case')
  event.removeCustomContainer('example:obsolete_pouch')
})
