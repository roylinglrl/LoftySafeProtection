package net.royling.lsp.lofty_safe_protection.compat.kubejs;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.rhino.Context;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerDefinition;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;

public final class SafeDefinitionEvent implements KubeEvent {
    private final Map<ResourceLocation, SafeBoxManager.Definition> boxes;
    private final Map<ResourceLocation, InventoryContainerDefinition> containers;

    public SafeDefinitionEvent(Map<ResourceLocation, SafeBoxManager.Definition> boxes,
                               Map<ResourceLocation, InventoryContainerDefinition> containers) {
        this.boxes = boxes;
        this.containers = containers;
    }

    public void addSafeBox(Context cx, String id, String texture, int columns, int rows) {
        addSafeBox(cx, id, texture, columns, rows, new JsonObject());
    }

    public void addSafeBox(Context cx, String id, String texture, int columns, int rows, Object options) {
        var key = ResourceLocation.parse(id);
        JsonObject json = definition(key, texture, columns, rows, JsonUtils.objectOf(cx, options));
        boxes.put(key, SafeBoxManager.parse(key, json));
    }

    public void removeSafeBox(String id) { boxes.remove(ResourceLocation.parse(id)); }

    public void addCustomContainer(Context cx, String id, String texture, int columns, int rows) {
        addCustomContainer(cx, id, texture, columns, rows, new JsonObject());
    }

    public void addCustomContainer(Context cx, String id, String texture, int columns, int rows, Object options) {
        var key = ResourceLocation.parse(id);
        JsonObject json = definition(key, texture, columns, rows, JsonUtils.objectOf(cx, options));
        containers.put(key, InventoryContainerDefinition.parse(key, json));
    }

    public void removeCustomContainer(String id) { containers.remove(ResourceLocation.parse(id)); }

    private static JsonObject definition(ResourceLocation id, String texture, int columns, int rows, JsonObject options) {
        if (options == null) options = new JsonObject();
        JsonObject json = new JsonObject();
        json.addProperty("id", id.toString());
        json.addProperty("texture", texture);
        json.addProperty("translation_key", options.has("translation_key") ? options.get("translation_key").getAsString()
                : "item." + id.getNamespace() + "." + id.getPath().replace('/', '.'));
        JsonObject capacity = new JsonObject();
        capacity.addProperty("columns", columns);
        capacity.addProperty("rows", rows);
        json.add("capacity", capacity);
        if (options.has("filter")) json.add("filter", options.get("filter"));
        if (options.has("experience")) json.add("experience", options.get("experience"));
        if (options.has("fluids")) json.add("fluids", options.get("fluids"));
        return json;
    }
}
