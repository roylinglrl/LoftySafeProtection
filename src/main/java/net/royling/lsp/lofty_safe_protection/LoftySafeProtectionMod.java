package net.royling.lsp.lofty_safe_protection;

import net.royling.lsp.lofty_safe_protection.compat.legacy.LegacySafeBoxIds;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.event.SafeBoxEvents;
import net.royling.lsp.lofty_safe_protection.network.SafeBoxNetworking;
import net.royling.lsp.lofty_safe_protection.registry.SafeBoxRegistries;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;

@Mod(SafeBoxManager.MOD)
public final class LoftySafeProtectionMod {
    public LoftySafeProtectionMod() {
        IEventBus bus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        SafeBoxRegistries.register(bus);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(LegacySafeBoxIds::registerAliases);
        SafeBoxNetworking.registerPayloadHandlers();
        SafeBoxEvents.register();
    }
}
