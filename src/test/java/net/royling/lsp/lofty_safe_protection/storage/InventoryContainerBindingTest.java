package net.royling.lsp.lofty_safe_protection.storage;

import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryContainerBindingTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        net.royling.lsp.lofty_safe_protection.TestRegistries.initialize();
    }

    @Test
    void savesThroughTheActualSlotAndMarksItsContainerDirty() {
        var parent = new CopyingContainer();
        parent.setItem(0, new ItemStack(Items.BUNDLE));
        int before = parent.changes;
        var binding = new InventoryContainerBinding(new Slot(parent, 0, 0, 0));
        assertTrue(binding.write(List.of(new ItemStack(Items.MAP, 3)), 4, 4));
        assertEquals(3, parent.getItem(0).get(DataComponents.CONTAINER).getTemplateInSlot(0).orElseThrow().create().getCount());
        assertEquals(4, parent.getItem(0).get(DataComponents.CUSTOM_DATA).copyTag().getIntOr("ContainerColumns", 0));
        assertTrue(parent.changes > before);
        assertTrue(binding.matches());
    }

    @Test
    void aSecondReaderCannotOverwriteContentsChangedByTheFirst() {
        var parent = new CopyingContainer();
        parent.setItem(0, new ItemStack(Items.BUNDLE));
        Slot source = new Slot(parent, 0, 0, 0);
        var first = new InventoryContainerBinding(source);
        var second = new InventoryContainerBinding(source);
        assertTrue(first.write(List.of(new ItemStack(Items.MAP, 5)), 4, 4));
        assertFalse(second.matches());
        assertFalse(second.write(List.of(), 4, 4));
        assertEquals(5, parent.getItem(0).get(DataComponents.CONTAINER).getTemplateInSlot(0).orElseThrow().create().getCount());
    }

    @Test
    void movingOrReplacingTheSourceInvalidatesTheOldWindow() {
        var parent = new CopyingContainer();
        parent.setItem(0, new ItemStack(Items.BUNDLE));
        var binding = new InventoryContainerBinding(new Slot(parent, 0, 0, 0));
        assertTrue(binding.write(List.of(new ItemStack(Items.MAP, 2)), 4, 4));
        ItemStack moved = parent.removeItemNoUpdate(0);
        assertFalse(binding.matches());
        parent.setItem(0, new ItemStack(Items.STONE));
        assertFalse(binding.write(List.of(), 4, 4));
        assertTrue(parent.getItem(0).is(Items.STONE));
        assertEquals(2, moved.get(DataComponents.CONTAINER).getTemplateInSlot(0).orElseThrow().create().getCount());
    }

    private static final class CopyingContainer extends SimpleContainer {
        private int changes;
        private CopyingContainer() { super(1); }
        @Override public ItemStack getItem(int slot) { return super.getItem(slot).copy(); }
        @Override public void setChanged() { super.setChanged(); changes++; }
    }
}
