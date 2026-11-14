package net.royling.lsp.lofty_safe_protection.storage;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;

public final class InventoryContainerBinding {
    private final Slot source;
    private ItemStack snapshot;

    public InventoryContainerBinding(Slot source) {
        this.source = source;
        snapshot = source.getItem().copy();
    }

    public boolean matches() {
        ItemStack current = source.getItem();
        return current.getCount() == 1 && ItemStack.isSameItemSameComponents(current, snapshot);
    }

    public boolean write(List<ItemStack> items, int columns, int rows) {
        if (!matches()) return false;
        ItemStack updated = source.getItem().copy();
        updated.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        CustomData.update(DataComponents.CUSTOM_DATA, updated, tag -> {
            tag.putInt("ContainerColumns", columns);
            tag.putInt("ContainerRows", rows);
        });
        snapshot = updated.copy();
        source.set(updated);
        source.setChanged();
        return matches();
    }
}
