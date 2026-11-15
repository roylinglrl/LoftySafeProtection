package net.royling.lsp.lofty_safe_protection.network;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.registry.SafeBoxCreativeTab;

import com.google.gson.*;
import net.minecraft.resources.Identifier;
import java.util.*;

public final class SafeBoxDefinitionCodec {
    public static String encode() {
        JsonArray all=new JsonArray();
        SafeBoxManager.definitions.values().forEach(d->{
            JsonObject o=new JsonObject(); o.addProperty("id",d.id().toString()); o.addProperty("texture",d.texture().toString());
            o.addProperty("translation_key",d.translationKey());
            o.add("experience",d.experience().toJson());
            JsonObject size=new JsonObject(); size.addProperty("columns",d.columns()); size.addProperty("rows",d.rows()); o.add("capacity",size);
            JsonObject filter=new JsonObject(); filter.addProperty("mode",d.whitelist()?"whitelist":"blacklist");
            JsonArray items=new JsonArray(),tags=new JsonArray(); d.items().forEach(id->items.add(id.toString())); d.tags().forEach(id->tags.add(id.toString()));
            filter.add("items",items); filter.add("tags",tags); o.add("filter",filter); all.add(o);
        });
        return all.toString();
    }
    public static void decode(String json) {
        Map<Identifier,SafeBoxManager.Definition> next=new HashMap<>();
        for(JsonElement e:JsonParser.parseString(json).getAsJsonArray()) { JsonObject o=e.getAsJsonObject(); var id=Identifier.parse(o.get("id").getAsString()); next.put(id,SafeBoxManager.parse(id,o)); }
        SafeBoxManager.definitions=Map.copyOf(next); SafeBoxCreativeTab.refresh.run();
    }
}
