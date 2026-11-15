package net.royling.lsp.lofty_safe_protection.data;

import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.resources.Identifier;

public final class DefinitionScriptHooks {
    public static BiConsumer<Map<Identifier, SafeBoxManager.Definition>, Map<Identifier, InventoryContainerDefinition>> apply = (boxes, containers) -> {};

    private DefinitionScriptHooks() {}
}
