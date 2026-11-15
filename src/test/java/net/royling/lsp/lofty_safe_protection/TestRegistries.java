package net.royling.lsp.lofty_safe_protection;

public final class TestRegistries {
    private static boolean initialized;
    private TestRegistries() {}
    public static synchronized void initialize() {
        if (initialized) return;
        var lookup = net.minecraft.data.registries.VanillaRegistries.createReloadableLookup(net.minecraft.data.registries.VanillaRegistries.createWorldLookup());
        boolean development = net.minecraft.SharedConstants.IS_RUNNING_IN_IDE;
        try {
            net.minecraft.SharedConstants.IS_RUNNING_IN_IDE = false;
            net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(lookup).forEach(net.minecraft.core.component.DataComponentInitializers.PendingComponents::apply);
        } finally { net.minecraft.SharedConstants.IS_RUNNING_IN_IDE = development; }
        initialized = true;
    }
}
