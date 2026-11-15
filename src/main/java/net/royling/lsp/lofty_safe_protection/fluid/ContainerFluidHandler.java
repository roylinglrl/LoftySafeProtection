package net.royling.lsp.lofty_safe_protection.fluid;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerManager;
import net.royling.lsp.lofty_safe_protection.registry.SafeBoxDataComponents;
import net.royling.lsp.lofty_safe_protection.registry.SafeBoxRegistries;

public final class ContainerFluidHandler implements IFluidHandlerItem {
    private final ItemStack container;

    public ContainerFluidHandler(ItemStack container) { this.container = container; }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> stack.getCount() == 1 && layout(stack).enabled() ? new ContainerFluidHandler(stack) : null,
                SafeBoxRegistries.INVENTORY_CONTAINER.get());
    }

    public static ContainerFluidContents contents(ItemStack stack) {
        return stack.getOrDefault(SafeBoxDataComponents.FLUID_CONTENTS.get(), ContainerFluidContents.EMPTY);
    }

    public static FluidStorageSettings configured(ItemStack stack) {
        var definition = InventoryContainerManager.definition(stack);
        return definition == null ? FluidStorageSettings.DISABLED : definition.fluids();
    }

    public static FluidStorageSettings layout(ItemStack stack) {
        var settings = configured(stack);
        var saved = contents(stack);
        if (saved.isEmpty()) return settings;
        int capacity = Math.max(settings.capacity(), saved.capacity());
        for (var fluid : saved.tanks()) capacity = Math.max(capacity, fluid.getAmount());
        return new FluidStorageSettings(Math.max(settings.slots(), saved.tanks().size()), capacity);
    }

    @Override public ItemStack getContainer() { return container; }
    @Override public int getTanks() { return layout(container).slots(); }
    @Override public FluidStack getFluidInTank(int tank) { check(tank); return contents(container).fluid(tank); }
    @Override public int getTankCapacity(int tank) { check(tank); return layout(container).capacity(); }
    @Override public boolean isFluidValid(int tank, FluidStack fluid) {
        check(tank);
        return container.getCount() == 1 && !fluid.isEmpty() && tank < configured(container).slots();
    }

    @Override public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return 0;
        int transferred = 0;
        for (int pass = 0; pass < 2; pass++) for (int tank = 0; tank < getTanks() && transferred < resource.getAmount(); tank++) {
            FluidStack current = getFluidInTank(tank);
            if ((pass == 0) == current.isEmpty()) continue;
            transferred += fill(tank, resource.copyWithAmount(resource.getAmount() - transferred), action);
        }
        return transferred;
    }

    public int fill(int tank, FluidStack resource, FluidAction action) {
        if (!isFluidValid(tank, resource)) return 0;
        FluidStack current = getFluidInTank(tank);
        if (!current.isEmpty() && !FluidStack.isSameFluidSameComponents(current, resource)) return 0;
        int amount = Math.min(resource.getAmount(), Math.max(0, configured(container).capacity() - current.getAmount()));
        if (amount > 0 && action.execute()) update(tank, resource.copyWithAmount(current.getAmount() + amount));
        return amount;
    }

    @Override public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return FluidStack.EMPTY;
        int transferred = 0;
        for (int tank = 0; tank < getTanks() && transferred < resource.getAmount(); tank++) {
            if (FluidStack.isSameFluidSameComponents(getFluidInTank(tank), resource))
                transferred += drain(tank, resource.getAmount() - transferred, action).getAmount();
        }
        return transferred == 0 ? FluidStack.EMPTY : resource.copyWithAmount(transferred);
    }

    @Override public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0) return FluidStack.EMPTY;
        for (int tank = 0; tank < getTanks(); tank++) {
            var current = getFluidInTank(tank);
            if (!current.isEmpty()) return drain(current.copyWithAmount(maxDrain), action);
        }
        return FluidStack.EMPTY;
    }

    public FluidStack drain(int tank, int maxDrain, FluidAction action) {
        if (container.getCount() != 1) return FluidStack.EMPTY;
        var current = getFluidInTank(tank);
        if (current.isEmpty() || maxDrain <= 0) return FluidStack.EMPTY;
        int amount = Math.min(current.getAmount(), maxDrain);
        var result = current.copyWithAmount(amount);
        if (action.execute()) update(tank, current.copyWithAmount(current.getAmount() - amount));
        return result;
    }

    public IFluidHandler tank(int tank) {
        check(tank);
        return new IFluidHandler() {
            @Override public int getTanks() { return 1; }
            @Override public FluidStack getFluidInTank(int index) { return ContainerFluidHandler.this.getFluidInTank(tank); }
            @Override public int getTankCapacity(int index) { return ContainerFluidHandler.this.getTankCapacity(tank); }
            @Override public boolean isFluidValid(int index, FluidStack fluid) { return ContainerFluidHandler.this.isFluidValid(tank, fluid); }
            @Override public int fill(FluidStack fluid, FluidAction action) { return ContainerFluidHandler.this.fill(tank, fluid, action); }
            @Override public FluidStack drain(FluidStack fluid, FluidAction action) {
                return FluidStack.isSameFluidSameComponents(getFluidInTank(0), fluid) ? drain(fluid.getAmount(), action) : FluidStack.EMPTY;
            }
            @Override public FluidStack drain(int amount, FluidAction action) { return ContainerFluidHandler.this.drain(tank, amount, action); }
        };
    }

    private void update(int tank, FluidStack fluid) {
        var updated = contents(container).with(tank, fluid, layout(container));
        if (updated.isEmpty()) container.remove(SafeBoxDataComponents.FLUID_CONTENTS.get());
        else container.set(SafeBoxDataComponents.FLUID_CONTENTS.get(), updated);
    }

    private void check(int tank) { java.util.Objects.checkIndex(tank, getTanks()); }
}
