package net.royling.lsp.lofty_safe_protection.item;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.royling.lsp.lofty_safe_protection.storage.legacy.ItemContainerContents;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerManager;

public record ContainerContentsSummary(int columns, int rows, List<ItemStack> items) {
    public int used() { return (int) items.stream().filter(stack -> !stack.isEmpty()).count(); }
    public int capacity() { return columns * rows; }

    public static ContainerContentsSummary fromItem(ItemStack stack) {
        var definition = InventoryContainerManager.definition(stack);
        if (definition == null) return null;
        var contents = ItemContainerContents.fromStack(stack);
        var tag = stack.getOrCreateTag().copy();
        int columns = definition.columns(), rows = definition.rows();
        if (contents.nonEmptyItems().iterator().hasNext()) {
            if (tag.getInt("ContainerColumns") >= 1 && tag.getInt("ContainerColumns") <= 9) columns = tag.getInt("ContainerColumns");
            if (tag.getInt("ContainerRows") >= 1 && tag.getInt("ContainerRows") <= 6) rows = tag.getInt("ContainerRows");
        }
        List<ItemStack> items = new ArrayList<>();
        for (int cell = 0; cell < 54; cell++) {
            ItemStack item = cell < contents.getSlots() ? contents.getStackInSlot(cell) : ItemStack.EMPTY;
            items.add(item);
            if (!item.isEmpty()) { columns = Math.max(columns, cell % 9 + 1); rows = Math.max(rows, cell / 9 + 1); }
        }
        return new ContainerContentsSummary(columns, rows, List.copyOf(items));
    }
}
