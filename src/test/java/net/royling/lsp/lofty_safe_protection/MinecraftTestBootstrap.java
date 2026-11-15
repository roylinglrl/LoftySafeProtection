package net.royling.lsp.lofty_safe_protection;

public final class MinecraftTestBootstrap implements org.junit.jupiter.api.extension.BeforeAllCallback {
    @Override public void beforeAll(org.junit.jupiter.api.extension.ExtensionContext context) {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }
}
