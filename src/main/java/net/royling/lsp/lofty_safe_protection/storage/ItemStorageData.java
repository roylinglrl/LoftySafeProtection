package net.royling.lsp.lofty_safe_protection.storage;

import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class ItemStorageData {
    private ItemStorageData() {}
    public static void update(ItemStack stack, Consumer<CompoundTag> change) {
        CompoundTag tag = stack.getOrCreateTag().copy();
        change.accept(tag);
        stack.setTag(tag);
    }
}
