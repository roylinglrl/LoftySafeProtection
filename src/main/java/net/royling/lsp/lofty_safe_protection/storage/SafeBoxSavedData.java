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
    public static final com.mojang.serialization.Codec<SafeBoxSavedData> CODEC = com.mojang.serialization.Codec.unboundedMap(net.minecraft.core.UUIDUtil.STRING_CODEC, PlayerStorage.CODEC).xmap(SafeBoxSavedData::new, store -> Map.copyOf(store.players));
    public static final net.minecraft.world.level.saveddata.SavedDataType<SafeBoxSavedData> TYPE = new net.minecraft.world.level.saveddata.SavedDataType<>(SafeBoxManager.id("players"), SafeBoxSavedData::new, CODEC, null);
    public SafeBoxSavedData() {}
    private SafeBoxSavedData(Map<UUID, PlayerStorage> loaded) {
        players.putAll(loaded);
        players.values().forEach(state -> state.dirty = this::setDirty);
    }
    public static SafeBoxSavedData get(ServerPlayer player) {
        return player.level().getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }
    public PlayerStorage forPlayer(UUID uuid) { return players.computeIfAbsent(uuid, key -> new PlayerStorage(this::setDirty)); }
    public static final class PlayerStorage extends SimpleContainer {
        private static final com.mojang.serialization.Codec<List<ItemStack>> ITEMS = ItemStack.OPTIONAL_CODEC.listOf().validate(items -> items.size() == 55 ? com.mojang.serialization.DataResult.success(items) : com.mojang.serialization.DataResult.error(() -> "Expected 55 safe box slots"));
        public static final com.mojang.serialization.Codec<PlayerStorage> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.create(instance -> instance.group(
                com.mojang.serialization.Codec.intRange(0, 9).fieldOf("Columns").forGetter(state -> state.columns),
                com.mojang.serialization.Codec.intRange(0, 6).fieldOf("Rows").forGetter(state -> state.rows),
                com.mojang.serialization.Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("Experience", 0).forGetter(PlayerStorage::experience),
                ITEMS.fieldOf("Items").forGetter(PlayerStorage::snapshot)
        ).apply(instance, PlayerStorage::new));
        private PlayerStorage(int columns, int rows, int experience, List<ItemStack> items) {
            this(() -> {});
            this.columns = columns;
            this.rows = rows;
            this.experience = experience;
            for (int slot = 0; slot < 55; slot++) setItem(slot, items.get(slot));
        }
        private List<ItemStack> snapshot() {
            List<ItemStack> items = new ArrayList<>(55);
            for (int slot = 0; slot < 55; slot++) items.add(getItem(slot).copy());
            return items;
        }
        public int columns, rows;
        private int experience;
        private Runnable dirty;
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
}
