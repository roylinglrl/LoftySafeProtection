package net.royling.lsp.lofty_safe_protection.menu;

import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerDefinition;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryContainerTransfersTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void attachesAfterTheHostSlotsWithoutChangingTheirIndices() {
        for (int size : new int[]{63, 90}) {
            var menu = new HostMenu(new SimpleContainer(size));
            Slot original = menu.slots.get(size - 1);
            var access = (InventoryContainerMenuAccess) menu;
            access.lsp$attachInventoryContainer(null);
            access.lsp$attachInventoryContainer(null);
            assertEquals(size, access.lsp$inventoryContainer().firstSlot());
            assertEquals(size + 54, menu.slots.size());
            assertSame(original, menu.slots.get(size - 1));
            assertEquals(size - 1, original.index);
        }
    }

    @Test
    void nativeTransfersCannotMergeIntoHiddenOverlaySlots() {
        var parent = new SimpleContainer(2);
        parent.setItem(0, new ItemStack(Items.STONE, 64));
        parent.setItem(1, new ItemStack(Items.STONE, 64));
        var menu = new HostMenu(parent);
        var access = (InventoryContainerMenuAccess) menu;
        access.lsp$attachInventoryContainer(null);
        var storage = access.lsp$inventoryContainer();
        storage.setItem(0, new ItemStack(Items.MAP, 1));
        var stack = new ItemStack(Items.MAP, 5);
        assertFalse(menu.nativeMove(stack));
        assertEquals(5, stack.getCount());
        assertEquals(1, storage.getItem(0).getCount());
    }

    @Test
    void mergingIntoExistingStacksStillHonorsTheTargetFilter() {
        var parent = new SimpleContainer(1);
        parent.setItem(0, new ItemStack(Items.STONE, 1));
        var slot = new Slot(parent, 0, 0, 0) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(Items.MAP); }
        };
        var stack = new ItemStack(Items.STONE, 5);
        InventoryContainerTransfers.move(stack, List.of(slot));
        assertEquals(5, stack.getCount());
        assertEquals(1, parent.getItem(0).getCount());
    }

    @Test
    void partialQuickMoveWritesBackToCopyReturningSourceSlots() {
        var oldItem = InventoryContainerManager.ITEM;
        var oldDefinitions = InventoryContainerManager.definitions;
        try {
            var id = ResourceLocation.parse("example:maps");
            InventoryContainerManager.ITEM = Items.BUNDLE;
            InventoryContainerManager.definitions = Map.of(id, new InventoryContainerDefinition(
                    id, "item.example.maps", id, 1, 1, true, Set.of(ResourceLocation.parse("minecraft:map")), Set.of()));
            var parent = new SimpleContainer(2) {
                @Override public ItemStack getItem(int slot) { return super.getItem(slot).copy(); }
            };
            parent.setItem(0, InventoryContainerManager.stack(id));
            parent.setItem(1, new ItemStack(Items.MAP, 64));
            var menu = new HostMenu(parent);
            var access = (InventoryContainerMenuAccess) menu;
            access.lsp$attachInventoryContainer(null);
            var storage = access.lsp$inventoryContainer();
            storage.syncSourceSlot(0);
            storage.syncColumns(1);
            storage.syncRows(1);
            storage.setItem(0, new ItemStack(Items.MAP, 60));
            assertNotNull(storage.definition());
            assertTrue(storage.canInsert(parent.getItem(1)));
            assertTrue(menu.slots.get(storage.firstSlot()).mayPlace(parent.getItem(1)));
            assertEquals(64, menu.slots.get(storage.firstSlot()).getMaxStackSize(parent.getItem(1)));
            assertTrue(InventoryContainerTransfers.quickMove(menu, storage, null, 1));
            assertEquals(60, parent.getItem(1).getCount());
            assertEquals(64, storage.getItem(0).getCount());
        } finally {
            InventoryContainerManager.ITEM = oldItem;
            InventoryContainerManager.definitions = oldDefinitions;
        }
    }

    private static class HostMenu extends AbstractContainerMenu {
        private HostMenu(SimpleContainer container) {
            super(null, 1);
            for (int slot = 0; slot < container.getContainerSize(); slot++) addSlot(new Slot(container, slot, 0, 0));
        }
        @Override public boolean stillValid(Player player) { return true; }
        @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
        private boolean nativeMove(ItemStack stack) { return moveItemStackTo(stack, 0, slots.size(), false); }
    }
}
