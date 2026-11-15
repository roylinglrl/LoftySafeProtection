package net.royling.lsp.lofty_safe_protection.compat.kubejs;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.EventJS;

import dev.latvian.mods.rhino.Context;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerDefinition;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;

public final class SafeDefinitionEvent extends EventJS {
    private final Map<ResourceLocation, SafeBoxManager.Definition> boxes;
    private final Map<ResourceLocation, InventoryContainerDefinition> containers;

    public SafeDefinitionEvent(Map<ResourceLocation, SafeBoxManager.Definition> boxes,
                               Map<ResourceLocation, InventoryContainerDefinition> containers) {
        this.boxes = boxes;
        this.containers = containers;
    }

    public void addSafeBox(String id, String texture, int columns, int rows) {
        addSafeBox(id, texture, columns, rows, new JsonObject());
    }

    public void addSafeBox(String id, String texture, int columns, int rows, Object options) {
        var key = new ResourceLocation(id);
        JsonObject json = definition(key, texture, columns, rows, optionsJson(options));
        boxes.put(key, SafeBoxManager.parse(key, json));
    }

    public void removeSafeBox(String id) { boxes.remove(new ResourceLocation(id)); }

    public void addCustomContainer(String id, String texture, int columns, int rows) {
        addCustomContainer(id, texture, columns, rows, new JsonObject());
    }

    public void addCustomContainer(String id, String texture, int columns, int rows, Object options) {
        var key = new ResourceLocation(id);
        JsonObject json = definition(key, texture, columns, rows, optionsJson(options));
        containers.put(key, InventoryContainerDefinition.parse(key, json));
    }

    public void removeCustomContainer(String id) { containers.remove(new ResourceLocation(id)); }

    private static JsonObject optionsJson(Object value) {
        return dev.latvian.mods.kubejs.util.JsonIO.of(value).getAsJsonObject();
    }

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
        return json;
    }
}
