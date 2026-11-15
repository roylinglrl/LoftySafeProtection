package net.royling.lsp.lofty_safe_protection;

import net.royling.lsp.lofty_safe_protection.compat.legacy.LegacySafeBoxIds;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.event.SafeBoxEvents;
import net.royling.lsp.lofty_safe_protection.network.SafeBoxNetworking;
import net.royling.lsp.lofty_safe_protection.registry.SafeBoxRegistries;
import net.royling.lsp.lofty_safe_protection.registry.SafeBoxDataComponents;
import net.royling.lsp.lofty_safe_protection.fluid.ContainerFluidHandler;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;

@Mod(SafeBoxManager.MOD)
public final class LoftySafeProtectionMod {
    public LoftySafeProtectionMod(IEventBus bus) {
        net.neoforged.neoforge.common.NeoForgeMod.enableMilkFluid();
        SafeBoxRegistries.register(bus);
        SafeBoxDataComponents.register(bus);
        bus.addListener(ContainerFluidHandler::registerCapabilities);
        bus.addListener(LegacySafeBoxIds::registerAliases);
        bus.addListener(SafeBoxNetworking::registerPayloadHandlers);
        SafeBoxEvents.register();
    }
}
