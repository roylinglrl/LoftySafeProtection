package net.royling.lsp.lofty_safe_protection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerDefinition;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerManager;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.network.InventoryContainerNetworking;
import net.royling.lsp.lofty_safe_protection.network.SafeBoxDefinitionCodec;

final class KubeJs8RuntimeCheck {
    static void verify(int experience, boolean retiredPresent) {
        var safe = SafeBoxManager.definitions.get(id("replaced_safe"));
        require(safe != null && safe.columns() == 3 && safe.rows() == 2, "KubeJS 8 did not replace the data pack safe box");
        require(safe.experience().enabled() && safe.experience().capacity() == experience, "KubeJS 8 experience settings did not update");
        require(!safe.accepts(new ItemStack(Items.DIAMOND)) && !safe.accepts(new ItemStack(Items.OAK_LOG))
                && safe.accepts(new ItemStack(Items.PAPER)), "KubeJS 8 blacklist IDs or tags failed");
        var pouch = InventoryContainerManager.definitions.get(id("replaced_pouch"));
        require(pouch != null && pouch.columns() == 4 && pouch.rows() == 3, "KubeJS 8 did not replace the data pack container");
        require(pouch.accepts(new ItemStack(Items.PAPER)) && pouch.accepts(new ItemStack(Items.OAK_PLANKS))
                && !pouch.accepts(new ItemStack(Items.DIAMOND)), "KubeJS 8 whitelist IDs or tags failed");
        require(!SafeBoxManager.definitions.containsKey(id("removed_safe")), "KubeJS 8 did not remove the data pack safe box");
        require(!InventoryContainerManager.definitions.containsKey(id("removed_pouch")), "KubeJS 8 did not remove the data pack container");
        require(!SafeBoxManager.definitions.containsKey(id("temporary_safe"))
                && !InventoryContainerManager.definitions.containsKey(id("temporary_pouch")), "KubeJS 8 did not remove script definitions");
        require(SafeBoxManager.definitions.containsKey(id("retired_safe")) == retiredPresent, "Reload retained a deleted script definition");
        if (retiredPresent) {
            var retired = SafeBoxManager.definitions.get(id("retired_safe"));
            require(!retired.experience().enabled() && retired.translationKey().equals("item.lsp_kubejs8_test.retired_safe"), "KubeJS 8 optional arguments failed");
        }
        JsonArray boxes = JsonParser.parseString(SafeBoxDefinitionCodec.encode()).getAsJsonArray();
        JsonArray containers = JsonParser.parseString(InventoryContainerNetworking.definitionsPayload().json()).getAsJsonArray();
        require(SafeBoxManager.parse(safe.id(), find(boxes, safe.id())).equals(safe), "Safe box client synchronization lost script settings");
        require(InventoryContainerDefinition.parse(pouch.id(), find(containers, pouch.id())).equals(pouch), "Container client synchronization lost script settings");
        require(find(boxes, id("removed_safe")) == null && find(containers, id("removed_pouch")) == null, "Removed definitions remained in client synchronization");
    }

    private static Identifier id(String name) { return Identifier.fromNamespaceAndPath("lsp_kubejs8_test", name); }
    private static JsonObject find(JsonArray definitions, Identifier id) {
        for (var entry : definitions) {
            JsonObject definition = entry.getAsJsonObject();
            if (id.toString().equals(definition.get("id").getAsString())) return definition;
        }
        return null;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
