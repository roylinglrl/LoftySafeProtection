package net.royling.lsp.lofty_safe_protection.data;

import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class InventoryContainerReloadListener extends SimpleJsonResourceReloadListener {
    public InventoryContainerReloadListener() {
        super(SafeBoxManager.GSON, "inventory_containers");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resources, ProfilerFiller profiler) {
        Map<ResourceLocation, InventoryContainerDefinition> next = new HashMap<>();
        files.forEach((id, json) -> next.put(id, InventoryContainerDefinition.parse(id, json.getAsJsonObject())));
        InventoryContainerManager.definitions = Map.copyOf(next);
    }
}
