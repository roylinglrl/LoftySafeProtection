package net.royling.lsp.lofty_safe_protection.fluid;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerInvWrapper;
import net.royling.lsp.lofty_safe_protection.menu.InventoryItemContainer;

public final class ContainerFluidTransfers {
    public record Plan(ContainerFluidContents contents, ItemStack cursor, ItemStack extra) {}
    private ContainerFluidTransfers() {}

    public static Plan prepare(ItemStack container, ItemStack carried, int tank, boolean deposit) {
        if (container.getCount() != 1 || carried.isEmpty() || tank < 0 || tank >= ContainerFluidHandler.layout(container).slots()) return null;
        ItemStack updated = container.copy();
        var handler = new ContainerFluidHandler(updated).tank(tank);
        FluidActionResult result = deposit
                ? FluidUtil.tryEmptyContainer(carried, handler, Integer.MAX_VALUE, null, true)
                : FluidUtil.tryFillContainer(carried, handler, Integer.MAX_VALUE, null, true);
        if (!result.isSuccess()) return null;
        ItemStack output = result.getResult();
        if (carried.getCount() == 1) return new Plan(ContainerFluidHandler.contents(updated), output, ItemStack.EMPTY);
        ItemStack remaining = carried.copyWithCount(carried.getCount() - 1);
        if (!output.isEmpty() && ItemStack.isSameItemSameComponents(remaining, output)
                && remaining.getCount() + output.getCount() <= remaining.getMaxStackSize()) {
            remaining.grow(output.getCount());
            return new Plan(ContainerFluidHandler.contents(updated), remaining, ItemStack.EMPTY);
        }
        return new Plan(ContainerFluidHandler.contents(updated), remaining, output);
    }

    public static boolean transfer(ServerPlayer player, InventoryItemContainer storage, int tank, boolean deposit) {
        storage.validateBinding();
        if (!storage.isOpen()) return false;
        var menu = player.containerMenu;
        var plan = prepare(storage.sourceStack(), menu.getCarried(), tank, deposit);
        if (plan == null) return false;
        var inventory = new PlayerInvWrapper(player.getInventory());
        if (!plan.extra().isEmpty() && !ItemHandlerHelper.insertItemStacked(inventory, plan.extra(), true).isEmpty()) return false;
        if (!storage.storeFluids(plan.contents())) return false;
        menu.setCarried(plan.cursor());
        if (!plan.extra().isEmpty()) {
            var remainder = ItemHandlerHelper.insertItemStacked(inventory, plan.extra(), false);
            if (!remainder.isEmpty()) player.drop(remainder, false);
        }
        menu.broadcastChanges();
        return true;
    }
}
