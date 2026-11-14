package net.royling.lsp.lofty_safe_protection.data;



import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.*;

public class SafeBoxDefinitionReloadListener extends SimpleJsonResourceReloadListener {
    public SafeBoxDefinitionReloadListener() { super(SafeBoxManager.GSON, "safeboxes"); }
    @Override protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, SafeBoxManager.Definition> next = new HashMap<>();
        files.forEach((id, json) -> next.put(id, SafeBoxManager.parse(id, json.getAsJsonObject())));
        SafeBoxManager.definitions = Map.copyOf(next);
    }
}
