package net.royling.lsp.lofty_safe_protection.data;

import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class SafeBoxDefinitionReloadListener extends SimplePreparableReloadListener<Map<Identifier, SafeBoxManager.Definition>> {
    @Override
    protected Map<Identifier, SafeBoxManager.Definition> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, SafeBoxManager.Definition> next = new HashMap<>();
        var finder = FileToIdConverter.json("safeboxes");
        finder.listMatchingResources(manager).forEach((file, resource) -> {
            var id = finder.fileToId(file);
            try (var reader = resource.openAsReader()) {
                next.put(id, SafeBoxManager.parse(id, JsonParser.parseReader(reader).getAsJsonObject()));
            } catch (Exception error) { throw new IllegalArgumentException("Invalid storage definition " + id, error); }
        });
        return Map.copyOf(next);
    }
    @Override
    protected void apply(Map<Identifier, SafeBoxManager.Definition> next, ResourceManager resources, ProfilerFiller profiler) {
        SafeBoxManager.definitions = next;
    }
}
