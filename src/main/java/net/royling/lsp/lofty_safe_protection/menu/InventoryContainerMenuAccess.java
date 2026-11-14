package net.royling.lsp.lofty_safe_protection.menu;

import net.minecraft.world.entity.player.Player;
import java.util.List;

public interface InventoryContainerMenuAccess {
    List<InventoryItemContainer> lsp$inventoryContainers();
    InventoryItemContainer lsp$addInventoryContainer(Player player);
    InventoryItemContainer lsp$focusedInventoryContainer();
    void lsp$focusInventoryContainer(int sourceSlot);
    void lsp$attachInventoryContainer(Player player);

    default InventoryItemContainer lsp$inventoryContainer() {
        return lsp$inventoryContainers().isEmpty() ? null : lsp$inventoryContainers().getFirst();
    }

    default int lsp$hostSlotCount() {
        var storage = lsp$inventoryContainer();
        return storage == null ? ((net.minecraft.world.inventory.AbstractContainerMenu) this).slots.size() : storage.firstSlot();
    }

    default boolean lsp$isInventoryContainerSource(int slot) {
        return lsp$inventoryContainers().stream().anyMatch(storage -> storage.isSource(slot));
    }

    default void lsp$closeInventoryContainers() { lsp$inventoryContainers().forEach(InventoryItemContainer::close); }
}
