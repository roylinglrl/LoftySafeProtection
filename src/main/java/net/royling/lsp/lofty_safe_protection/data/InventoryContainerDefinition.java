package net.royling.lsp.lofty_safe_protection.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

public record InventoryContainerDefinition(ResourceLocation id, String translationKey,
        ResourceLocation texture, int columns, int rows, boolean whitelist,
        Set<ResourceLocation> items, Set<ResourceLocation> tags) {
    public Component title() {
        return Component.translatable(translationKey);
    }

    public boolean accepts(ItemStack stack) {
        if (stack.isEmpty() || !stack.canFitInsideContainerItems()) return false;
        boolean matches = items.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()))
                || tags.stream().anyMatch(id -> stack.is(TagKey.create(Registries.ITEM, id)));
        return whitelist == matches;
    }

    public static InventoryContainerDefinition parse(ResourceLocation fileId, JsonObject json) {
        ResourceLocation id = ResourceLocation.parse(json.get("id").getAsString());
        if (!id.equals(fileId)) throw new IllegalArgumentException("id must match data file path: " + fileId);
        JsonElement translationKey = json.get("translation_key");
        if (translationKey == null || !translationKey.isJsonPrimitive()
                || !translationKey.getAsJsonPrimitive().isString() || translationKey.getAsString().isBlank())
            throw new IllegalArgumentException("translation_key must be a non-blank string");
        String key = translationKey.getAsString();
        ResourceLocation texture = ResourceLocation.parse(json.has("texture")
                ? json.get("texture").getAsString() : "minecraft:textures/item/bundle.png");
        JsonObject capacity = json.getAsJsonObject("capacity");
        int columns = capacity.get("columns").getAsBigDecimal().intValueExact();
        int rows = capacity.get("rows").getAsBigDecimal().intValueExact();
        if (columns < 1 || columns > 9 || rows < 1 || rows > 6)
            throw new IllegalArgumentException("capacity must be 1..9 columns by 1..6 rows");
        JsonObject filter = json.has("filter") ? json.getAsJsonObject("filter") : new JsonObject();
        String mode = filter.has("mode") ? filter.get("mode").getAsString() : "blacklist";
        if (!mode.equals("whitelist") && !mode.equals("blacklist")) throw new IllegalArgumentException("invalid filter mode");
        return new InventoryContainerDefinition(id, key, texture, columns, rows,
                mode.equals("whitelist"), ids(filter, "items"), ids(filter, "tags"));
    }

    private static Set<ResourceLocation> ids(JsonObject json, String key) {
        Set<ResourceLocation> result = new HashSet<>();
        if (json.has(key)) for (JsonElement entry : json.getAsJsonArray(key)) result.add(ResourceLocation.parse(entry.getAsString()));
        return Set.copyOf(result);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id.toString());
        json.addProperty("translation_key", translationKey);
        json.addProperty("texture", texture.toString());
        JsonObject capacity = new JsonObject();
        capacity.addProperty("columns", columns);
        capacity.addProperty("rows", rows);
        json.add("capacity", capacity);
        JsonObject filter = new JsonObject();
        filter.addProperty("mode", whitelist ? "whitelist" : "blacklist");
        JsonArray itemIds = new JsonArray(), tagIds = new JsonArray();
        items.stream().sorted().forEach(id -> itemIds.add(id.toString()));
        tags.stream().sorted().forEach(id -> tagIds.add(id.toString()));
        filter.add("items", itemIds);
        filter.add("tags", tagIds);
        json.add("filter", filter);
        return json;
    }
}
