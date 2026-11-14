package net.royling.lsp.lofty_safe_protection.storage;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;


import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

public final class SafeBoxSavedData extends SavedData {
    private final Map<UUID, PlayerStorage> players = new HashMap<>();
    public static SafeBoxSavedData get(ServerPlayer player) {
        return player.level().getServer().overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(SafeBoxSavedData::new, SafeBoxSavedData::load, null), "safebox_players");
    }
    public PlayerStorage forPlayer(UUID uuid) { return players.computeIfAbsent(uuid, key -> new PlayerStorage(this::setDirty)); }
    public static final class PlayerStorage extends SimpleContainer {
        public int columns, rows;
        private int experience;
        private final Runnable dirty;
        public PlayerStorage(Runnable dirty) { super(55); this.dirty = dirty; }
        public int experience() { return experience; }
        public void setExperience(int value) {
            if (value < 0) throw new IllegalArgumentException("Negative safe box experience");
            if (experience != value) { experience = value; setChanged(); }
        }
        public boolean contentsEmpty() { if (experience > 0) return false; for (int i=1;i<55;i++) if (!getItem(i).isEmpty()) return false; return true; }
        public void refreshSize() {
            if (!contentsEmpty()) return;
            var def = SafeBoxManager.definition(getItem(0));
            columns = def == null ? 0 : def.columns(); rows = def == null ? 0 : def.rows();
        }
        @Override public void setChanged() { super.setChanged(); if (dirty != null) dirty.run(); }
    }
    private static SafeBoxSavedData load(CompoundTag root, net.minecraft.core.HolderLookup.Provider registries) {
        SafeBoxSavedData store = new SafeBoxSavedData();
        for (String key : root.getAllKeys()) {
            CompoundTag tag = root.getCompound(key);
            PlayerStorage state = store.forPlayer(UUID.fromString(key));
            state.setExperience(tag.getInt("Experience"));
            state.columns = tag.getInt("Columns"); state.rows = tag.getInt("Rows");
            if (state.columns < 0 || state.columns > 9 || state.rows < 0 || state.rows > 6)
                throw new IllegalStateException("Invalid saved safe box dimensions for " + key);
            ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
            for (int i=0;i<items.size();i++) { CompoundTag item = items.getCompound(i); int slot = item.getInt("Slot");
                if (slot < 0 || slot >= 55) throw new IllegalStateException("Invalid saved safe box slot");
                state.setItem(slot, ItemStack.parseOptional(registries,item));
            }
        }
        return store;
    }
    @Override public CompoundTag save(CompoundTag root, net.minecraft.core.HolderLookup.Provider registries) {
        players.forEach((uuid, state) -> {
            CompoundTag tag = new CompoundTag(); tag.putInt("Columns", state.columns); tag.putInt("Rows", state.rows);
            tag.putInt("Experience", state.experience());
            ListTag items = new ListTag();
            for (int i=0;i<55;i++) if (!state.getItem(i).isEmpty()) { CompoundTag item = (CompoundTag) state.getItem(i).save(registries,new CompoundTag()); item.putInt("Slot", i); items.add(item); }
            tag.put("Items", items); root.put(uuid.toString(), tag);
        });
        return root;
    }
}
