package net.royling.lsp.lofty_safe_protection.registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.fluid.ContainerFluidContents;

public final class SafeBoxDataComponents {
    private static final DeferredRegister<DataComponentType<?>> TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, SafeBoxManager.MOD);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ContainerFluidContents>> FLUID_CONTENTS = TYPES.register(
            "container_fluids", () -> DataComponentType.<ContainerFluidContents>builder()
                    .persistent(ContainerFluidContents.CODEC).networkSynchronized(ContainerFluidContents.STREAM_CODEC).build());

    private SafeBoxDataComponents() {}
    public static void register(IEventBus bus) { TYPES.register(bus); }
}
