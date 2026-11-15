package net.royling.lsp.lofty_safe_protection.compat.kubejs;

import com.google.gson.JsonParser;
import dev.latvian.mods.rhino.ContextFactory;
import dev.latvian.mods.rhino.ScriptableObject;
import java.util.HashMap;
import net.minecraft.resources.Identifier;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerDefinition;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SafeDefinitionEventTest {
    @Test
    void javascriptOptionsAndOptionalArgumentsAreConverted() {
        var boxes = new HashMap<Identifier, SafeBoxManager.Definition>();
        var containers = new HashMap<Identifier, InventoryContainerDefinition>();
        var context = new ContextFactory().enter();
        var scope = context.initStandardObjects();
        ScriptableObject.putProperty(scope, "event", context.javaToJS(new SafeDefinitionEvent(boxes, containers), scope), context);
        context.evaluateString(scope, "event.addSafeBox('test:box', 'test:textures/item/box.png', 3, 2, {filter: {mode: 'whitelist', items: ['minecraft:map'], tags: ['test:maps']}, experience: {enabled: true, capacity: 100}}); event.addCustomContainer('test:bag', 'test:textures/item/bag.png', 4, 4);", "test", 1, null);
        var box = boxes.get(Identifier.parse("test:box"));
        assertEquals(100, box.experience().capacity());
        assertTrue(box.tags().contains(Identifier.parse("test:maps")));
        assertEquals(4, containers.get(Identifier.parse("test:bag")).rows());
    }

    @Test
    void additionsAndRemovalsUseTheSameValidationAndOptionsAsDatapacks() {
        var boxes = new HashMap<Identifier, SafeBoxManager.Definition>();
        var containers = new HashMap<Identifier, InventoryContainerDefinition>();
        var event = new SafeDefinitionEvent(boxes, containers);
        var safeId = Identifier.parse("example:gold");
        var bagId = Identifier.parse("example:map_bag");
        var options = JsonParser.parseString("{\"translation_key\":\"item.example.special\",\"filter\":{\"mode\":\"whitelist\",\"items\":[\"minecraft:map\"],\"tags\":[\"example:maps\"]},\"experience\":{\"enabled\":true,\"capacity\":300}}").getAsJsonObject();
        event.addSafeBox(null, safeId.toString(), "example:textures/item/gold.png", 3, 2, options);
        event.addCustomContainer(null, bagId.toString(), "example:textures/item/bag.png", 4, 4, options);
        assertEquals(6, boxes.get(safeId).columns() * boxes.get(safeId).rows());
        assertEquals(300, boxes.get(safeId).experience().capacity());
        assertTrue(boxes.get(safeId).whitelist());
        assertEquals("item.example.special", containers.get(bagId).translationKey());
        assertTrue(containers.get(bagId).tags().contains(Identifier.parse("example:maps")));
        assertThrows(IllegalArgumentException.class, () -> event.addSafeBox(null, "example:invalid", "example:texture", 10, 1));
        event.removeSafeBox(safeId.toString());
        event.removeCustomContainer(bagId.toString());
        assertTrue(boxes.isEmpty());
        assertTrue(containers.isEmpty());
    }
}
