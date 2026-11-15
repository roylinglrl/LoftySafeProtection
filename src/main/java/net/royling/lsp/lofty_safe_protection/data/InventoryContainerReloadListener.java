package net.royling.lsp.lofty_safe_protection.data;

import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class InventoryContainerReloadListener extends SimplePreparableReloadListener<Map<Identifier, InventoryContainerDefinition>> {
    @Override
    protected Map<Identifier, InventoryContainerDefinition> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, InventoryContainerDefinition> next = new HashMap<>();
        var finder = FileToIdConverter.json("inventory_containers");
        finder.listMatchingResources(manager).forEach((file, resource) -> {
            var id = finder.fileToId(file);
            try (var reader = resource.openAsReader()) {
                next.put(id, InventoryContainerDefinition.parse(id, JsonParser.parseReader(reader).getAsJsonObject()));
            } catch (Exception error) { throw new IllegalArgumentException("Invalid storage definition " + id, error); }
        });
        return Map.copyOf(next);
    }
    @Override
    protected void apply(Map<Identifier, InventoryContainerDefinition> next, ResourceManager resources, ProfilerFiller profiler) {
        InventoryContainerManager.definitions = next;
    }
}
