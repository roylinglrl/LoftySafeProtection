package net.royling.lsp.lofty_safe_protection.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ContainerActions {
    public static final int SORT = 0;
    public static final int DEPOSIT_MATCHING = 1;
    public static final int FOCUS = 2;

    private ContainerActions() {}

    public static void sort(List<Slot> slots) {
        List<ItemStack> merged = new ArrayList<>();
        for (Slot slot : slots) {
            ItemStack remaining = slot.getItem().copy();
            if (remaining.isEmpty()) continue;
            for (ItemStack target : merged) {
                if (!ItemStack.isSameItemSameTags(remaining, target)) continue;
                int count = Math.min(remaining.getCount(), Math.min(target.getMaxStackSize(), slot.getMaxStackSize(target)) - target.getCount());
                if (count > 0) { target.grow(count); remaining.shrink(count); }
                if (remaining.isEmpty()) break;
            }
            if (!remaining.isEmpty()) merged.add(remaining);
        }
        merged.sort(Comparator.comparing((ItemStack stack) -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
                .thenComparingInt(stack -> java.util.Objects.hashCode(stack.getTag())).thenComparing(Comparator.comparingInt(ItemStack::getCount).reversed()));
        for (int i = 0; i < slots.size(); i++) slots.get(i).set(i < merged.size() ? merged.get(i) : ItemStack.EMPTY);
    }

    public static void depositMatching(AbstractContainerMenu menu, Player player, List<Slot> targets, int lockedSlot) {
        depositMatching(menu.slots.stream().filter(slot -> slot.container == player.getInventory()
                && !(menu instanceof InventoryContainerMenuAccess access && access.lsp$isInventoryContainerSource(slot.index))).toList(), targets, player, lockedSlot);
    }

    static void depositMatching(List<Slot> sources, List<Slot> targets, Player player, int lockedSlot) {
        List<ItemStack> kinds = targets.stream().filter(Slot::hasItem).map(slot -> slot.getItem().copy()).toList();
        var visited = new HashSet<Integer>();
        for (Slot source : sources) {
            int inventorySlot = source.getContainerSlot();
            if (inventorySlot < 9 || inventorySlot >= 36
                    || source.index == lockedSlot || !source.isActive() || !source.mayPickup(player) || !visited.add(inventorySlot)) continue;
            ItemStack stack = source.getItem().copy();
            if (stack.isEmpty() || kinds.stream().noneMatch(kind -> ItemStack.isSameItemSameTags(kind, stack))) continue;
            int count = stack.getCount();
            InventoryContainerTransfers.move(stack, targets);
            if (stack.getCount() != count) {
                source.setByPlayer(stack.isEmpty() ? ItemStack.EMPTY : stack);
                source.onTake(player, stack);
            }
        }
    }
}
