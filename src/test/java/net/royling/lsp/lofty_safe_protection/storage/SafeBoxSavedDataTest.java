package net.royling.lsp.lofty_safe_protection.storage;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SafeBoxSavedDataTest {
    private static DynamicOps<JsonElement> ops;
    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        net.royling.lsp.lofty_safe_protection.TestRegistries.initialize();
        ops = RegistryOps.create(JsonOps.INSTANCE, net.minecraft.data.registries.VanillaRegistries.createLookup());
    }

    @Test void savesEquipmentNestedContentsExperienceAndSeparatePlayers() {
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        var store = new SafeBoxSavedData();
        var state = store.forPlayer(first);
        state.columns = 4;
        state.rows = 3;
        state.setItem(0, new ItemStack(Items.GOLD_BLOCK));
        ItemStack maps = new ItemStack(Items.MAP, 7);
        maps.set(DataComponents.CUSTOM_NAME, Component.literal("Saved maps"));
        ItemStack pouch = new ItemStack(Items.BUNDLE);
        pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(maps)));
        state.setItem(12, pouch);
        state.setExperience(299);
        store.forPlayer(second).setExperience(1);
        JsonElement json = SafeBoxSavedData.CODEC.encodeStart(ops, store).getOrThrow();
        var restored = SafeBoxSavedData.CODEC.parse(ops, json).getOrThrow();
        var loaded = restored.forPlayer(first);
        assertEquals(4, loaded.columns);
        assertEquals(3, loaded.rows);
        assertEquals(299, loaded.experience());
        assertTrue(loaded.getItem(0).is(Items.GOLD_BLOCK));
        assertTrue(ItemStack.isSameItemSameComponents(pouch, loaded.getItem(12)));
        assertFalse(loaded.contentsEmpty());
        assertEquals(1, restored.forPlayer(second).experience());
        restored.setDirty(false);
        loaded.setExperience(298);
        assertTrue(restored.isDirty());
        restored.setDirty(false);
        loaded.setItem(54, new ItemStack(Items.PAPER, 3));
        assertTrue(restored.isDirty());
    }

    @Test void oldSavesDefaultToZeroExperienceAndInvalidStorageIsRejected() {
        UUID id = UUID.randomUUID();
        var store = new SafeBoxSavedData();
        store.forPlayer(id);
        var json = SafeBoxSavedData.CODEC.encodeStart(ops, store).getOrThrow().getAsJsonObject();
        var data = json.getAsJsonObject(id.toString());
        data.remove("Experience");
        assertEquals(0, SafeBoxSavedData.CODEC.parse(ops, json).getOrThrow().forPlayer(id).experience());
        data.addProperty("Columns", 10);
        assertTrue(SafeBoxSavedData.CODEC.parse(ops, json).error().isPresent());
        data.addProperty("Columns", 0);
        data.getAsJsonArray("Items").remove(0);
        assertTrue(SafeBoxSavedData.CODEC.parse(ops, json).error().isPresent());
    }
}
