package net.royling.lsp.lofty_safe_protection.menu;

import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryContainerWindowsTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    @Test
    void windowsOwnSeparateSlotsAndClosingOneKeepsTheOtherOpen() {
        var menu = new HostMenu();
        var access = (InventoryContainerMenuAccess) menu;
        var first = access.lsp$addInventoryContainer(null);
        var second = access.lsp$addInventoryContainer(null);
        open(first, 0);
        open(second, 1);
        assertEquals(4, access.lsp$hostSlotCount());
        assertEquals(4, first.firstSlot());
        assertEquals(58, second.firstSlot());
        first.setItem(0, new ItemStack(Items.MAP, 4));
        first.setItem(1, new ItemStack(Items.MAP, 7));
        second.setItem(0, new ItemStack(Items.PAPER, 5));
        ContainerActions.sort(List.of(menu.slots.get(first.firstSlot()), menu.slots.get(first.firstSlot() + 1)));
        assertEquals(11, first.getItem(0).getCount());
        assertEquals(5, second.getItem(0).getCount());
        assertTrue(access.lsp$isInventoryContainerSource(0));
        assertTrue(access.lsp$isInventoryContainerSource(1));
        menu.clicked(0, 0, ClickType.PICKUP, null);
        menu.clicked(1, 0, ClickType.PICKUP, null);
        assertTrue(menu.getCarried().isEmpty());
        first.close();
        assertTrue(second.isOpen());
        assertFalse(access.lsp$isInventoryContainerSource(0));
        assertTrue(access.lsp$isInventoryContainerSource(1));
        assertEquals(5, second.getItem(0).getCount());
        access.lsp$closeInventoryContainers();
        assertFalse(second.isOpen());
    }

    @Test
    void focusFallsBackToTheMostRecentlyFocusedOpenWindow() {
        var menu = new HostMenu();
        var access = (InventoryContainerMenuAccess) menu;
        var first = access.lsp$addInventoryContainer(null);
        var second = access.lsp$addInventoryContainer(null);
        var third = access.lsp$addInventoryContainer(null);
        open(first, 0); open(second, 1); open(third, 2);
        access.lsp$focusInventoryContainer(1);
        access.lsp$focusInventoryContainer(0);
        access.lsp$focusInventoryContainer(2);
        assertSame(third, access.lsp$focusedInventoryContainer());
        third.close();
        assertSame(first, access.lsp$focusedInventoryContainer());
        first.close();
        assertSame(second, access.lsp$focusedInventoryContainer());
    }

    @Test
    void nativeTransfersCannotReachAnyAppendedWindow() {
        var menu = new HostMenu();
        for (int slot = 0; slot < 4; slot++) menu.slots.get(slot).set(new ItemStack(Items.STONE, 64));
        var access = (InventoryContainerMenuAccess) menu;
        access.lsp$addInventoryContainer(null);
        var second = access.lsp$addInventoryContainer(null);
        second.setItem(0, new ItemStack(Items.MAP, 1));
        var stack = new ItemStack(Items.MAP, 5);
        assertFalse(menu.nativeMove(stack));
        assertEquals(5, stack.getCount());
        assertEquals(1, second.getItem(0).getCount());
    }

    private static void open(InventoryItemContainer storage, int source) {
        storage.syncSourceSlot(source);
        storage.syncColumns(2);
        storage.syncRows(2);
    }

    private static class HostMenu extends AbstractContainerMenu {
        private HostMenu() {
            super(null, 1);
            var container = new SimpleContainer(4);
            for (int slot = 0; slot < 4; slot++) {
                container.setItem(slot, new ItemStack(Items.BUNDLE));
                addSlot(new Slot(container, slot, 0, 0));
            }
        }
        @Override public boolean stillValid(Player player) { return true; }
        @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
        private boolean nativeMove(ItemStack stack) { return moveItemStackTo(stack, 0, slots.size(), false); }
    }
}
