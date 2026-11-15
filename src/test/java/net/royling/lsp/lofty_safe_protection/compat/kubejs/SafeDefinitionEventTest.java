package net.royling.lsp.lofty_safe_protection.compat.kubejs;

import com.google.gson.JsonParser;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ScriptableObject;
import java.util.HashMap;
import net.minecraft.resources.ResourceLocation;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerDefinition;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SafeDefinitionEventTest {
    @Test
    void javascriptOptionsAndOptionalArgumentsAreConverted() {
        var boxes = new HashMap<ResourceLocation, SafeBoxManager.Definition>();
        var containers = new HashMap<ResourceLocation, InventoryContainerDefinition>();
        var context = Context.enter();
        var scope = context.initStandardObjects();
        ScriptableObject.putProperty(scope, "event", Context.javaToJS(context, new SafeDefinitionEvent(boxes, containers), scope), context);
        context.evaluateString(scope, "event.addSafeBox('test:box', 'test:textures/item/box.png', 3, 2, {filter: {mode: 'whitelist', items: ['minecraft:map'], tags: ['test:maps']}, experience: {enabled: true, capacity: 100}}); event.addCustomContainer('test:bag', 'test:textures/item/bag.png', 4, 4);", "test", 1, null);
        var box = boxes.get(new ResourceLocation("test:box"));
        assertEquals(100, box.experience().capacity());
        assertTrue(box.tags().contains(new ResourceLocation("test:maps")));
        assertEquals(4, containers.get(new ResourceLocation("test:bag")).rows());
    }

    @Test
    void additionsAndRemovalsUseTheSameValidationAndOptionsAsDatapacks() {
        var boxes = new HashMap<ResourceLocation, SafeBoxManager.Definition>();
        var containers = new HashMap<ResourceLocation, InventoryContainerDefinition>();
        var event = new SafeDefinitionEvent(boxes, containers);
        var safeId = new ResourceLocation("example:gold");
        var bagId = new ResourceLocation("example:map_bag");
        var options = JsonParser.parseString("{\"translation_key\":\"item.example.special\",\"filter\":{\"mode\":\"whitelist\",\"items\":[\"minecraft:map\"],\"tags\":[\"example:maps\"]},\"experience\":{\"enabled\":true,\"capacity\":300}}").getAsJsonObject();
        event.addSafeBox(safeId.toString(), "example:textures/item/gold.png", 3, 2, options);
        event.addCustomContainer(bagId.toString(), "example:textures/item/bag.png", 4, 4, options);
        assertEquals(6, boxes.get(safeId).columns() * boxes.get(safeId).rows());
        assertEquals(300, boxes.get(safeId).experience().capacity());
        assertTrue(boxes.get(safeId).whitelist());
        assertEquals("item.example.special", containers.get(bagId).translationKey());
        assertTrue(containers.get(bagId).tags().contains(new ResourceLocation("example:maps")));
        assertThrows(IllegalArgumentException.class, () -> event.addSafeBox("example:invalid", "example:texture", 10, 1));
        event.removeSafeBox(safeId.toString());
        event.removeCustomContainer(bagId.toString());
        assertTrue(boxes.isEmpty());
        assertTrue(containers.isEmpty());
    }
}
