package net.royling.lsp.lofty_safe_protection.item;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;


import net.minecraft.core.component.DataComponents;
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

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        super.verifyComponentsAfterLoad(stack);
        var data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains("SafeBoxId")) {
            return;
        }
        if (isLegacyLabel(stack.get(DataComponents.CUSTOM_NAME))) {
            stack.remove(DataComponents.CUSTOM_NAME);
        }
        if (isLegacyLabel(stack.get(DataComponents.ITEM_NAME))) {
            stack.remove(DataComponents.ITEM_NAME);
        }
    }

    private static boolean isLegacyLabel(Component name) {
        return name != null && name.getContents() instanceof TranslatableContents text
            && (text.getKey().equals("item.safebox.safe_box") || text.getKey().equals("item.safebox.sized")
                || text.getKey().equals("item.lofty_safe_protection.safe_box") || text.getKey().equals("item.lofty_safe_protection.sized"));
    }
}
