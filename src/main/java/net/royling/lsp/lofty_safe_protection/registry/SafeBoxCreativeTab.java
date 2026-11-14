package net.royling.lsp.lofty_safe_protection.registry;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerManager;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
public final class SafeBoxCreativeTab {
    public static CreativeModeTab TAB;
    public static Runnable refresh=()->{};
    public static CreativeModeTab create(CreativeModeTab.Builder builder) {
        return builder.title(Component.translatable("itemGroup.lofty_safe_protection"))
                .icon(() -> SafeBoxManager.stack(SafeBoxManager.id("case_2x2")))
                .displayItems((parameters, output) -> {
                    SafeBoxManager.definitions.keySet().stream().sorted().forEach(id -> output.accept(SafeBoxManager.stack(id)));
                    InventoryContainerManager.definitions.keySet().stream().sorted().forEach(id -> output.accept(InventoryContainerManager.stack(id)));
                }).build();
    }
}
