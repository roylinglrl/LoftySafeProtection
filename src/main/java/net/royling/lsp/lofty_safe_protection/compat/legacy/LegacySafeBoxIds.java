package net.royling.lsp.lofty_safe_protection.compat.legacy;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;


import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegisterEvent;

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

    public static void registerAliases(net.minecraftforge.registries.MissingMappingsEvent event) {
        if (event.getKey().equals(Registries.ITEM)) {
            event.getMappings(net.minecraftforge.registries.ForgeRegistries.Keys.ITEMS, OLD_NAMESPACE).forEach(mapping -> {
                if (mapping.getKey().getPath().equals("safe_box")) mapping.remap(SafeBoxManager.ITEM);
            });
        } else if (event.getKey().equals(Registries.MENU)) {
            event.getMappings(net.minecraftforge.registries.ForgeRegistries.Keys.MENU_TYPES, OLD_NAMESPACE).forEach(mapping -> {
                if (mapping.getKey().getPath().equals("safe_box")) mapping.remap(SafeBoxManager.MENU);
            });
        }
    }
}
