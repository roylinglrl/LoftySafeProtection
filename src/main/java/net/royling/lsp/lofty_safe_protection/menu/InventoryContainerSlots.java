package net.royling.lsp.lofty_safe_protection.menu;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class InventoryContainerSlots {
    private InventoryContainerSlots() {}

    public static List<Slot> slots(InventoryItemContainer storage) {
        List<Slot> slots = new ArrayList<>();
        for (int cell = 0; cell < 54; cell++) {
            final int cellIndex = cell;
            slots.add(new Slot(storage, cell, 0, 0) {
                @Override public boolean isActive() { return storage.activeCell(cellIndex); }
                @Override public boolean mayPlace(ItemStack stack) { return isActive() && storage.canInsert(stack); }
                @Override public boolean mayPickup(Player player) { return isActive(); }
            });
        }
        return slots;
    }

    public static List<DataSlot> data(InventoryItemContainer container) {
        return List.of(
                new DataSlot() {
                    public int get() { return container.columns(); }
                    public void set(int value) { container.syncColumns(value); }
                },
                new DataSlot() {
                    public int get() { return container.rows(); }
                    public void set(int value) { container.syncRows(value); }
                },
                new DataSlot() {
                    public int get() { return container.sourceSlot(); }
                    public void set(int value) { container.syncSourceSlot(value); }
                });
    }

}
