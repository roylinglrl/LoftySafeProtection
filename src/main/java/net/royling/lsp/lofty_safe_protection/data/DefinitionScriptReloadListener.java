package net.royling.lsp.lofty_safe_protection.data;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class DefinitionScriptReloadListener extends SimplePreparableReloadListener<Boolean> {
    @Override
    protected Boolean prepare(ResourceManager resources, ProfilerFiller profiler) { return Boolean.TRUE; }

    @Override
    protected void apply(Boolean ignored, ResourceManager resources, ProfilerFiller profiler) {
        var boxes = new HashMap<>(SafeBoxManager.definitions);
        var containers = new HashMap<>(InventoryContainerManager.definitions);
        DefinitionScriptHooks.apply.accept(boxes, containers);
        SafeBoxManager.definitions = Map.copyOf(boxes);
        InventoryContainerManager.definitions = Map.copyOf(containers);
    }
}
