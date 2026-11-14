package net.royling.lsp.lofty_safe_protection.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class InventoryContainerTransfers {
    private InventoryContainerTransfers() {}

    public static boolean quickMove(AbstractContainerMenu menu, InventoryItemContainer storage, Player player, int index) {
        if (index < 0 || index >= menu.slots.size()) return false;
        Slot source = menu.slots.get(index);
        boolean fromOverlay = storage.ownsSlot(index);
        if (!source.isActive() || !source.hasItem() || !source.mayPickup(player)) return fromOverlay;
        ItemStack stack = source.getItem();
        if (!fromOverlay && (!source.mayPlace(stack) || !storage.canInsert(stack))) return false;
        List<Slot> targets = new ArrayList<>();
        if (fromOverlay) {
            int limit = menu instanceof InventoryContainerMenuAccess access ? access.lsp$hostSlotCount() : storage.firstSlot();
            for (int i = 0; i < limit; i++) {
                Slot slot = menu.slots.get(i);
                if (slot.container == player.getInventory() && slot.getContainerSlot() >= 0 && slot.getContainerSlot() < 36
                        && !(menu instanceof InventoryContainerMenuAccess access && access.lsp$isInventoryContainerSource(i))) targets.add(slot);
            }
            Collections.reverse(targets);
        } else targets.addAll(menu.slots.subList(storage.firstSlot(), storage.endSlot()));
        int count = stack.getCount();
        move(stack, targets);
        if (stack.getCount() == count) return fromOverlay;
        source.setByPlayer(stack.isEmpty() ? ItemStack.EMPTY : stack);
        source.onTake(player, stack);
        return true;
    }

    public static boolean moveWithinHost(AbstractContainerMenu menu, InventoryItemContainer storage, ItemStack stack,
                                         int start, int end, boolean reverse) {
        List<Slot> targets = new ArrayList<>();
        for (int index = Math.max(0, start); index < Math.min(end, storage.firstSlot()); index++) {
            if (!(menu instanceof InventoryContainerMenuAccess access && access.lsp$isInventoryContainerSource(index))) targets.add(menu.slots.get(index));
        }
        if (reverse) Collections.reverse(targets);
        int count = stack.getCount();
        move(stack, targets);
        return stack.getCount() != count;
    }

    public static void move(ItemStack stack, List<Slot> targets) {
        for (Slot slot : targets) {
            if (stack.isEmpty()) return;
            ItemStack existing = slot.getItem();
            if (!slot.isActive() || !slot.mayPlace(stack) || existing.isEmpty()
                    || !ItemStack.isSameItemSameComponents(stack, existing)) continue;
            int count = Math.min(stack.getCount(), slot.getMaxStackSize(stack) - existing.getCount());
            if (count <= 0) continue;
            existing = existing.copyWithCount(existing.getCount() + count);
            stack.shrink(count);
            slot.setByPlayer(existing);
        }
        for (Slot slot : targets) {
            if (stack.isEmpty()) return;
            if (!slot.isActive() || !slot.mayPlace(stack) || slot.hasItem()) continue;
            int count = Math.min(stack.getCount(), slot.getMaxStackSize(stack));
            if (count > 0) slot.setByPlayer(stack.split(count));
        }
    }
}
