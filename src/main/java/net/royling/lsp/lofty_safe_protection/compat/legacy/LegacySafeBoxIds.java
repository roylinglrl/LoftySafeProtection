package net.royling.lsp.lofty_safe_protection.compat.legacy;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;


import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class LegacySafeBoxIds {
    private static final String OLD_NAMESPACE = "safebox";
    private static final Set<String> BUILTIN_DEFINITIONS = Set.of(
            "case_2x2", "case_2x3", "case_2x4", "case_3x3", "case_3x4");

    private LegacySafeBoxIds() {}

    public static ResourceLocation definitionId(ResourceLocation original) {
        if (original != null && original.getNamespace().equals(OLD_NAMESPACE)
                && BUILTIN_DEFINITIONS.contains(original.getPath())) return SafeBoxManager.id(original.getPath());
        return original;
    }

    public static void registerAliases(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ITEM) || event.getRegistryKey().equals(Registries.MENU)) {
            event.getRegistry().addAlias(ResourceLocation.fromNamespaceAndPath(OLD_NAMESPACE, "safe_box"), SafeBoxManager.id("safe_box"));
        } else if (event.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)) {
            event.getRegistry().addAlias(ResourceLocation.fromNamespaceAndPath(OLD_NAMESPACE, "safeboxes"), SafeBoxManager.id("safeboxes"));
        }
    }
}
