package net.royling.lsp.lofty_safe_protection.menu;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContainerActionsTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    @Test
    void sortingMergesCompatibleStacksAndPreservesComponentsAndOutsideSlots() {
        var container = new SimpleContainer(8);
        container.setItem(0, new ItemStack(Items.MAP, 50));
        container.setItem(1, new ItemStack(Items.MAP, 20));
        var named = new ItemStack(Items.MAP, 4);
        named.setHoverName(Component.literal("A"));
        container.setItem(2, named);
        container.setItem(3, new ItemStack(Items.PAPER, 3));
        container.setItem(7, new ItemStack(Items.DIAMOND, 7));
        var targets = slots(container, 0, 6);
        ContainerActions.sort(targets);
        assertEquals(74, targets.stream().filter(slot -> slot.getItem().is(Items.MAP)).mapToInt(slot -> slot.getItem().getCount()).sum());
        assertEquals(4, targets.stream().filter(slot -> Component.literal("A").equals(slot.getItem().getHoverName())).mapToInt(slot -> slot.getItem().getCount()).sum());
        assertEquals(4, targets.stream().filter(Slot::hasItem).count());
        assertEquals(7, container.getItem(7).getCount());
        for (Slot slot : targets) if (slot.hasItem()) assertTrue(slot.getItem().getCount() <= slot.getItem().getMaxStackSize());
    }

    @Test
    void depositUsesMatchingMainInventoryItemsAndHonorsFiltersAndLockedSource() {
        var inventory = new SimpleContainer(41);
        inventory.setItem(0, new ItemStack(Items.MAP, 10));
        inventory.setItem(9, new ItemStack(Items.MAP, 20));
        inventory.setItem(10, new ItemStack(Items.PAPER, 8));
        inventory.setItem(11, new ItemStack(Items.STONE, 6));
        inventory.setItem(12, new ItemStack(Items.MAP, 5));
        inventory.setItem(40, new ItemStack(Items.MAP, 9));
        var target = new SimpleContainer(4);
        target.setItem(0, new ItemStack(Items.MAP, 60));
        target.setItem(1, new ItemStack(Items.STONE, 1));
        var targets = new ArrayList<Slot>();
        for (int index = 0; index < 4; index++) targets.add(new Slot(target, index, 0, 0) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(Items.MAP); }
        });
        var sources = slots(inventory, 0, 41);
        ContainerActions.depositMatching(sources, targets, null, 12);
        assertEquals(10, inventory.getItem(0).getCount());
        assertTrue(inventory.getItem(9).isEmpty());
        assertEquals(8, inventory.getItem(10).getCount());
        assertEquals(6, inventory.getItem(11).getCount());
        assertEquals(5, inventory.getItem(12).getCount());
        assertEquals(9, inventory.getItem(40).getCount());
        assertEquals(80, targets.stream().filter(slot -> slot.getItem().is(Items.MAP)).mapToInt(slot -> slot.getItem().getCount()).sum());
    }

    @Test
    void depositStopsAtCapacityAndDoesNotSeedAnEmptyContainer() {
        var inventory = new SimpleContainer(36);
        inventory.setItem(9, new ItemStack(Items.MAP, 12));
        var sources = slots(inventory, 0, 36);
        var target = new SimpleContainer(1);
        var targets = slots(target, 0, 1);
        ContainerActions.depositMatching(sources, targets, null, -1);
        assertEquals(12, inventory.getItem(9).getCount());
        target.setItem(0, new ItemStack(Items.MAP, 63));
        ContainerActions.depositMatching(sources, targets, null, -1);
        assertEquals(11, inventory.getItem(9).getCount());
        assertEquals(64, target.getItem(0).getCount());
    }

    private static List<Slot> slots(SimpleContainer container, int start, int end) {
        List<Slot> slots = new ArrayList<>();
        for (int cell = start; cell < end; cell++) {
            var slot = new Slot(container, cell, 0, 0);
            slot.index = cell;
            slots.add(slot);
        }
        return slots;
    }
}
