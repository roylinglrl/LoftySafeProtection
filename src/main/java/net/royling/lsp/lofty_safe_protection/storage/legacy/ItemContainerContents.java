package net.royling.lsp.lofty_safe_protection.storage.legacy;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class ItemContainerContents {
    public static final ItemContainerContents EMPTY = new ItemContainerContents(List.of());
    private static final String KEY = "InventoryContainerItems";
    private final List<ItemStack> items;
    private ItemContainerContents(List<ItemStack> items) { this.items = items; }
    public static ItemContainerContents fromItems(List<ItemStack> source) {
        return new ItemContainerContents(source.stream().map(ItemStack::copy).toList());
    }
    public static ItemContainerContents fromStack(ItemStack stack) {
        if (!stack.hasTag()) return EMPTY;
        ListTag saved = stack.getTag().getList(KEY, Tag.TAG_COMPOUND);
        List<ItemStack> items = new ArrayList<>();
        for (int index = 0; index < saved.size(); index++) {
            CompoundTag entry = saved.getCompound(index);
            int slot = entry.getInt("Slot");
            if (slot < 0 || slot > 255) throw new IllegalArgumentException("Invalid portable container slot");
            while (items.size() <= slot) items.add(ItemStack.EMPTY);
            items.set(slot, ItemStack.of(entry));
        }
        return new ItemContainerContents(List.copyOf(items));
    }
    public void writeTo(ItemStack stack) {
        ListTag saved = new ListTag();
        for (int slot = 0; slot < items.size(); slot++) if (!items.get(slot).isEmpty()) {
            CompoundTag entry = items.get(slot).save(new CompoundTag());
            entry.putInt("Slot", slot);
            saved.add(entry);
        }
        stack.getOrCreateTag().put(KEY, saved);
    }
    public int getSlots() { return items.size(); }
    public ItemStack getStackInSlot(int slot) { return slot < items.size() ? items.get(slot).copy() : ItemStack.EMPTY; }
    public Iterable<ItemStack> nonEmptyItems() { return items.stream().filter(item -> !item.isEmpty()).map(ItemStack::copy).toList(); }
    public void copyInto(List<ItemStack> destination) {
        for (int slot = 0; slot < destination.size(); slot++) destination.set(slot, getStackInSlot(slot));
    }
}
