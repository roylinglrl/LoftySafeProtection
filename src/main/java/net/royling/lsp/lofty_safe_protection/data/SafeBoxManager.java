package net.royling.lsp.lofty_safe_protection.data;

import net.royling.lsp.lofty_safe_protection.compat.legacy.LegacySafeBoxIds;
import net.royling.lsp.lofty_safe_protection.compat.legacy.SafeBoxLayoutMigration;
import net.royling.lsp.lofty_safe_protection.experience.SafeBoxExperienceSettings;
import net.royling.lsp.lofty_safe_protection.menu.SafeBoxMenu;
import net.royling.lsp.lofty_safe_protection.menu.SafeBoxMenuAccess;


import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

public final class SafeBoxManager {
    public static final String MOD = "lofty_safe_protection";
    private static final TagKey<Item> BLACKLISTED_ITEMS = TagKey.create(Registries.ITEM, id("blacklisted_items"));
    public static Item ITEM;
    public static MenuType<SafeBoxMenu> MENU;
    public static final Gson GSON = new Gson();
    public static volatile Map<ResourceLocation, Definition> definitions = Map.of();
    public static ResourceLocation id(String path) { return new ResourceLocation(MOD, path); }
    public record Definition(ResourceLocation id, ResourceLocation texture, int columns, int rows,
                             boolean whitelist, Set<ResourceLocation> items, Set<ResourceLocation> tags, String translationKey,
                             SafeBoxExperienceSettings experience) {
        public Definition(ResourceLocation id,ResourceLocation texture,int columns,int rows,boolean whitelist,Set<ResourceLocation> items,Set<ResourceLocation> tags,String translationKey){
            this(id,texture,columns,rows,whitelist,items,tags,translationKey,SafeBoxExperienceSettings.DISABLED);
        }
        public Definition(ResourceLocation id,ResourceLocation texture,int columns,int rows,boolean whitelist,Set<ResourceLocation> items,Set<ResourceLocation> tags){
            this(id,texture,columns,rows,whitelist,items,tags,defaultTranslationKey(id));
        }
        public boolean accepts(ItemStack stack) {
            if (stack.is(ITEM) || stack.is(BLACKLISTED_ITEMS)) return false;
            boolean matches = items.contains(BuiltInRegistries.ITEM.getKey(stack.getItem())) ||
                tags.stream().anyMatch(t -> stack.is(TagKey.create(Registries.ITEM, t)));
            return whitelist == matches;
        }
    }
    public static Definition parse(ResourceLocation fileId, JsonObject json) {
        ResourceLocation id = new ResourceLocation(json.get("id").getAsString());
        if (!id.equals(fileId)) throw new IllegalArgumentException("id must match data file path: " + fileId);
        JsonObject size = json.getAsJsonObject("capacity");
        int columns = integer(size, "columns"), rows = integer(size, "rows");
        if (columns < 1 || columns > 9 || rows < 1 || rows > 6) throw new IllegalArgumentException("capacity must be 1..9 columns by 1..6 rows");
        JsonObject filter = json.has("filter") ? json.getAsJsonObject("filter") : new JsonObject();
        String mode = filter.has("mode") ? filter.get("mode").getAsString() : "blacklist";
        if (!mode.equals("whitelist") && !mode.equals("blacklist")) throw new IllegalArgumentException("invalid filter mode");
        String translationKey=json.has("translation_key")?json.get("translation_key").getAsString():defaultTranslationKey(id);
        if(translationKey.isBlank())throw new IllegalArgumentException("translation_key must not be blank");
        return new Definition(id, new ResourceLocation(json.get("texture").getAsString()), columns, rows,
            mode.equals("whitelist"), ids(filter, "items"), ids(filter, "tags"), translationKey, SafeBoxExperienceSettings.parse(json));
    }
    private static String defaultTranslationKey(ResourceLocation id){return "item."+id.getNamespace()+"."+id.getPath().replace('/','.');}
    private static int integer(JsonObject o, String key) { return o.get(key).getAsBigDecimal().intValueExact(); }
    private static Set<ResourceLocation> ids(JsonObject o, String key) {
        Set<ResourceLocation> result = new HashSet<>();
        if (o.has(key)) for (JsonElement e : o.getAsJsonArray(key)) result.add(new ResourceLocation(e.getAsString()));
        return Set.copyOf(result);
    }
    public static ResourceLocation variant(ItemStack stack) {
        if (!stack.is(ITEM) || !stack.hasTag()) return null;
        return ResourceLocation.tryParse(stack.getTag().getString("SafeBoxId"));
    }
    public static Definition definition(ItemStack stack) {
        var key = variant(stack);
        if (key == null) return null;
        var definition = definitions.get(key);
        return definition != null ? definition : definitions.get(LegacySafeBoxIds.definitionId(key));
    }
    public static ItemStack stack(ResourceLocation id) { ItemStack s = new ItemStack(ITEM); var tag=new net.minecraft.nbt.CompoundTag(); tag.putString("SafeBoxId",id.toString()); s.setTag(tag); return s; }
    public static void setExpanded(ServerPlayer player, boolean expanded) {
        if (!player.isAlive() || player.isSpectator()) return;
        if (expanded && player.containerMenu != player.inventoryMenu) return;
        if (player.inventoryMenu instanceof SafeBoxMenuAccess a) {
            if(expanded)SafeBoxLayoutMigration.update(a.lsp$storage().state());
            a.lsp$storage().expanded=expanded;
        }
        player.inventoryMenu.broadcastChanges();
    }
}
