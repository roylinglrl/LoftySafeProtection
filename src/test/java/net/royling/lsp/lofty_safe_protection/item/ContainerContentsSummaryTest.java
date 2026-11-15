package net.royling.lsp.lofty_safe_protection.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerDefinition;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContainerContentsSummaryTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        net.royling.lsp.lofty_safe_protection.TestRegistries.initialize(); }

    @Test
    void previewAndUsageRetainStoredDimensionsAndCountOccupiedSlotsInsteadOfItemAmounts() {
        var oldItem = InventoryContainerManager.ITEM;
        var oldDefinitions = InventoryContainerManager.definitions;
        try {
            var id = Identifier.parse("test:pouch");
            InventoryContainerManager.ITEM = Items.BUNDLE;
            InventoryContainerManager.definitions = Map.of(id, new InventoryContainerDefinition(id, "test", id, 2, 2, false, Set.of(), Set.of()));
            var pouch = InventoryContainerManager.stack(id);
            CustomData.update(DataComponents.CUSTOM_DATA, pouch, tag -> { tag.putInt("ContainerColumns", 5); tag.putInt("ContainerRows", 5); });
            List<ItemStack> contents = new ArrayList<>();
            for (int cell = 0; cell < 54; cell++) contents.add(ItemStack.EMPTY);
            contents.set(0, new ItemStack(Items.MAP, 64));
            contents.set(40, new ItemStack(Items.PAPER, 12));
            pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
            var summary = ContainerContentsSummary.fromItem(pouch);
            assertEquals(5, summary.columns());
            assertEquals(5, summary.rows());
            assertEquals(2, summary.used());
            assertEquals(25, summary.capacity());
            assertEquals(12, summary.items().get(40).getCount());
            pouch.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            assertEquals(4, ContainerContentsSummary.fromItem(pouch).capacity());
        } finally {
            InventoryContainerManager.ITEM = oldItem;
            InventoryContainerManager.definitions = oldDefinitions;
        }
    }
}
