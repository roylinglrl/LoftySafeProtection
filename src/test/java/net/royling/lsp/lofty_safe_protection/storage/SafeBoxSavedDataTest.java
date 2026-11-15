package net.royling.lsp.lofty_safe_protection.storage;

import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.royling.lsp.lofty_safe_protection.storage.legacy.ItemContainerContents;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SafeBoxSavedDataTest {
    private SafeBoxSavedData load(CompoundTag tag) throws Exception {
        var method = SafeBoxSavedData.class.getDeclaredMethod("load", CompoundTag.class);
        method.setAccessible(true);
        return (SafeBoxSavedData) method.invoke(null, tag);
    }

    @Test void savesEquipmentNestedContentsExperienceAndSeparatePlayers() throws Exception {
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        var store = new SafeBoxSavedData();
        var state = store.forPlayer(first);
        state.columns = 4;
        state.rows = 3;
        state.setItem(0, new ItemStack(Items.GOLD_BLOCK));
        ItemStack pouch = new ItemStack(Items.BUNDLE);
        ItemContainerContents.fromItems(List.of(new ItemStack(Items.MAP, 7))).writeTo(pouch);
        state.setItem(12, pouch);
        state.setExperience(299);
        store.forPlayer(second).setExperience(1);
        var restored = load(store.save(new CompoundTag()));
        var loaded = restored.forPlayer(first);
        assertEquals(4, loaded.columns);
        assertEquals(3, loaded.rows);
        assertEquals(299, loaded.experience());
        assertTrue(loaded.getItem(0).is(Items.GOLD_BLOCK));
        assertTrue(ItemStack.isSameItemSameTags(pouch, loaded.getItem(12)));
        assertFalse(loaded.contentsEmpty());
        assertEquals(1, restored.forPlayer(second).experience());
        restored.setDirty(false);
        loaded.setExperience(298);
        assertTrue(restored.isDirty());
        restored.setDirty(false);
        loaded.setItem(54, new ItemStack(Items.PAPER, 3));
        assertTrue(restored.isDirty());
    }

    @Test void oldSavesDefaultToZeroExperienceAndInvalidStorageIsRejected() throws Exception {
        UUID id = UUID.randomUUID();
        var store = new SafeBoxSavedData();
        store.forPlayer(id);
        var tag = store.save(new CompoundTag());
        var data = tag.getCompound(id.toString());
        data.remove("Experience");
        assertEquals(0, load(tag).forPlayer(id).experience());
        data.putInt("Columns", 10);
        assertThrows(Exception.class, () -> load(tag));
        data.putInt("Columns", 0);
        data.putInt("Experience", -1);
        assertThrows(Exception.class, () -> load(tag));
    }
}
