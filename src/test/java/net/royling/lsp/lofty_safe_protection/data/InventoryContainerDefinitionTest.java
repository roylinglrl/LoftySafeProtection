package net.royling.lsp.lofty_safe_protection.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryContainerDefinitionTest {
    private static final Identifier ID = Identifier.parse("example:container");

    @Test
    void defaultMapPouchHasSixteenSlotsAndOnlyAcceptsMapIds() throws Exception {
        try (var stream = getClass().getResourceAsStream("/data/lofty_safe_protection/inventory_containers/map_pouch.json")) {
            assertNotNull(stream);
            var json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            var definition = InventoryContainerDefinition.parse(Identifier.parse("lofty_safe_protection:map_pouch"), json);
            assertEquals(4, definition.rows());
            assertEquals(4, definition.columns());
            assertEquals("item.lofty_safe_protection.map_pouch", definition.translationKey());
            assertTrue(definition.whitelist());
            assertEquals(Set.of(Identifier.parse("minecraft:map"), Identifier.parse("minecraft:filled_map")), definition.items());
            assertTrue(definition.tags().isEmpty());
        }
    }

    @Test
    void syncRoundTripPreservesTranslationKeysDimensionsAndBothFilterModes() {
        for (String mode : new String[]{"blacklist", "whitelist"}) {
            var json = base();
            json.add("filter", JsonParser.parseString("{\"mode\":\"" + mode
                    + "\",\"items\":[\"minecraft:stone\"],\"tags\":[\"example:maps\"]}"));
            var definition = InventoryContainerDefinition.parse(ID, json);
            assertEquals("item.example.container", definition.translationKey());
            assertEquals(3, definition.columns());
            assertEquals(2, definition.rows());
            assertEquals(mode.equals("whitelist"), definition.whitelist());
            assertEquals(Set.of(Identifier.parse("example:maps")), definition.tags());
            var encoded = definition.toJson();
            assertFalse(encoded.has("name"));
            assertEquals(definition, InventoryContainerDefinition.parse(ID, encoded));
        }
    }

    @Test
    void missingFilterAllowsOrdinaryItemsByDefault() {
        var definition = InventoryContainerDefinition.parse(ID, base());
        assertFalse(definition.whitelist());
        assertTrue(definition.items().isEmpty());
        assertTrue(definition.tags().isEmpty());
    }

    @Test
    void invalidDefinitionsCannotReplaceInstalledDefinitions() {
        for (String capacity : new String[]{"{\"columns\":0,\"rows\":2}", "{\"columns\":10,\"rows\":2}",
                "{\"columns\":3,\"rows\":7}", "{\"columns\":3,\"rows\":0}", "{\"columns\":2.5,\"rows\":2}"}) {
            JsonObject json = base();
            json.add("capacity", JsonParser.parseString(capacity));
            assertThrows(RuntimeException.class, () -> InventoryContainerDefinition.parse(ID, json));
        }
        for (String key : new String[]{"null", "\"\"", "\" \"", "123", "true", "[]"}) {
            JsonObject badKey = base();
            badKey.add("translation_key", JsonParser.parseString(key));
            assertThrows(IllegalArgumentException.class, () -> InventoryContainerDefinition.parse(ID, badKey));
        }
        JsonObject missingKey = base();
        missingKey.remove("translation_key");
        assertThrows(IllegalArgumentException.class, () -> InventoryContainerDefinition.parse(ID, missingKey));
        JsonObject badFilter = base();
        badFilter.add("filter", JsonParser.parseString("{\"mode\":\"unknown\"}"));
        assertThrows(IllegalArgumentException.class, () -> InventoryContainerDefinition.parse(ID, badFilter));
        assertThrows(IllegalArgumentException.class, () -> InventoryContainerDefinition.parse(Identifier.parse("example:other"), base()));
    }

    private JsonObject base() {
        return JsonParser.parseString("{\"id\":\"example:container\",\"translation_key\":\"item.example.container\",\"capacity\":{\"columns\":3,\"rows\":2}}").getAsJsonObject();
    }
}
