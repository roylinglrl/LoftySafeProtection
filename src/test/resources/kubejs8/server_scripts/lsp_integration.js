SafeEvents.definitions(event => {
  event.removeSafeBox('lsp_kubejs8_test:removed_safe')
  event.removeCustomContainer('lsp_kubejs8_test:removed_pouch')

  event.addSafeBox('lsp_kubejs8_test:replaced_safe', 'lofty_safe_protection:textures/safebox/gold_safe_box.png', 3, 2, {
    filter: {mode: 'blacklist', items: ['minecraft:diamond'], tags: ['minecraft:logs']},
    experience: {enabled: true, capacity: 321}
  })
  event.addCustomContainer('lsp_kubejs8_test:replaced_pouch', 'lofty_safe_protection:textures/item/map_pouch.png', 4, 3, {
    filter: {mode: 'whitelist', items: ['minecraft:paper'], tags: ['minecraft:planks']}
  })

  event.addSafeBox('lsp_kubejs8_test:retired_safe', 'lofty_safe_protection:textures/safebox/stone_safe_box.png', 2, 2)
  event.addSafeBox('lsp_kubejs8_test:temporary_safe', 'lofty_safe_protection:textures/safebox/stone_safe_box.png', 2, 2)
  event.addCustomContainer('lsp_kubejs8_test:temporary_pouch', 'lofty_safe_protection:textures/item/map_pouch.png', 2, 2)
  event.removeSafeBox('lsp_kubejs8_test:temporary_safe')
  event.removeCustomContainer('lsp_kubejs8_test:temporary_pouch')
})
