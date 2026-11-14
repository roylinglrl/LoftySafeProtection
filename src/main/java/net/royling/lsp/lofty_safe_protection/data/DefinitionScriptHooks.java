package net.royling.lsp.lofty_safe_protection.data;

import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;

public final class DefinitionScriptHooks {
    public static BiConsumer<Map<ResourceLocation, SafeBoxManager.Definition>, Map<ResourceLocation, InventoryContainerDefinition>> apply = (boxes, containers) -> {};

    private DefinitionScriptHooks() {}
}
