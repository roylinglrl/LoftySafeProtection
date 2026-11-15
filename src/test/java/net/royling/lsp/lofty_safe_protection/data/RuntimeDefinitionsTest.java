package net.royling.lsp.lofty_safe_protection.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RuntimeDefinitionsTest {
    @Test void loadsDataPacksLiveBlacklistTagsAndKubeJs6Definitions() {
        assertEquals(5, SafeBoxManager.definitions.size());
        var gold = SafeBoxManager.definitions.get(SafeBoxManager.id("case_2x4"));
        assertNotNull(gold);
        assertEquals(4, gold.columns());
        assertEquals(2, gold.rows());
        assertEquals(100, gold.experience().capacity());
        assertFalse(gold.accepts(new ItemStack(Items.BEDROCK)));
        assertTrue(gold.accepts(new ItemStack(Items.PAPER)));
        assertSame(gold, SafeBoxManager.definition(SafeBoxManager.stack(gold.id())));
        var map = InventoryContainerManager.definitions.get(SafeBoxManager.id("map_pouch"));
        assertNotNull(map);
        assertTrue(map.accepts(new ItemStack(Items.MAP)));
        assertFalse(map.accepts(new ItemStack(Items.DIAMOND)));
        var sicc = InventoryContainerManager.definitions.get(new ResourceLocation("kubejs:sicc_pouch"));
        assertNotNull(sicc);
        assertEquals(5, sicc.columns());
        assertEquals(5, sicc.rows());
        assertTrue(sicc.accepts(new ItemStack(Items.ENCHANTED_BOOK)));
    }
}
