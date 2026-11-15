package net.royling.lsp.lofty_safe_protection.item;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;


import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class SafeBoxItem extends Item {
    public SafeBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        var definition = SafeBoxManager.definition(stack);
        return definition == null ? super.getName(stack) : Component.translatable(definition.translationKey());
    }

    private static boolean isLegacyLabel(Component name) {
        return name != null && name.getContents() instanceof TranslatableContents text
            && (text.getKey().equals("item.safebox.safe_box") || text.getKey().equals("item.safebox.sized")
                || text.getKey().equals("item.lofty_safe_protection.safe_box") || text.getKey().equals("item.lofty_safe_protection.sized"));
    }
    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(net.royling.lsp.lofty_safe_protection.client.LoftySafeProtectionClient.itemExtension());
    }
}
