package net.royling.lsp.lofty_safe_protection.registry;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerManager;
import net.royling.lsp.lofty_safe_protection.item.InventoryContainerItem;
import net.royling.lsp.lofty_safe_protection.item.SafeBoxItem;
import net.royling.lsp.lofty_safe_protection.menu.SafeBoxMenu;


import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SafeBoxRegistries {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, SafeBoxManager.MOD);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, SafeBoxManager.MOD);
    public static final DeferredHolder<Item, Item> BOX = ITEMS.register("safe_box",
            () -> SafeBoxManager.ITEM = new SafeBoxItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(Registries.ITEM, SafeBoxManager.id("safe_box"))).stacksTo(1).fireResistant()));
    public static final DeferredHolder<Item, Item> INVENTORY_CONTAINER = ITEMS.register("inventory_container",
            () -> InventoryContainerManager.ITEM = new InventoryContainerItem(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(Registries.ITEM, SafeBoxManager.id("inventory_container")))));
    public static final DeferredHolder<MenuType<?>, MenuType<SafeBoxMenu>> MENU = MENUS.register("safe_box",
            () -> SafeBoxManager.MENU = new MenuType<>(SafeBoxMenu::new, FeatureFlags.DEFAULT_FLAGS));
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SafeBoxManager.MOD);

    static {
        TABS.register("safeboxes", () -> SafeBoxCreativeTab.TAB = SafeBoxCreativeTab.create(CreativeModeTab.builder()));
    }

    private SafeBoxRegistries() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        MENUS.register(bus);
        TABS.register(bus);
    }
}
