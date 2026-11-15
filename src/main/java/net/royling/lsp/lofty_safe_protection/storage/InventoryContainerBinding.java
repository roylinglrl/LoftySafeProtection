package net.royling.lsp.lofty_safe_protection.storage;

import java.util.List;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.royling.lsp.lofty_safe_protection.storage.legacy.ItemContainerContents;

public final class InventoryContainerBinding {
    private final Slot source;
    private ItemStack snapshot;

    public InventoryContainerBinding(Slot source) {
        this.source = source;
        snapshot = source.getItem().copy();
    }

    public boolean matches() {
        ItemStack current = source.getItem();
        return current.getCount() == 1 && ItemStack.isSameItemSameTags(current, snapshot);
    }

    public boolean write(List<ItemStack> items, int columns, int rows) {
        if (!matches()) return false;
        ItemStack updated = source.getItem().copy();
        ItemContainerContents.fromItems(items).writeTo(updated);
        ItemStorageData.update(updated, tag -> {
            tag.putInt("ContainerColumns", columns);
            tag.putInt("ContainerRows", rows);
        });
        snapshot = updated.copy();
        source.set(updated);
        source.setChanged();
        return matches();
    }
}
