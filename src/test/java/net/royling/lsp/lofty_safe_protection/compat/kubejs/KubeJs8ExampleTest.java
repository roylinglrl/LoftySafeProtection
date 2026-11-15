package net.royling.lsp.lofty_safe_protection.compat.kubejs;

import dev.latvian.mods.rhino.ContextFactory;
import dev.latvian.mods.rhino.ScriptableObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import net.minecraft.resources.Identifier;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerDefinition;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KubeJs8ExampleTest {
    @Test void documentedExampleUsesTheKubeJs8ApiAndBundledTextures() throws Exception {
        var boxes = new HashMap<Identifier, SafeBoxManager.Definition>();
        var containers = new HashMap<Identifier, InventoryContainerDefinition>();
        var context = new ContextFactory().enter();
        var scope = context.initStandardObjects();
        ScriptableObject.putProperty(scope, "event", context.javaToJS(new SafeDefinitionEvent(boxes, containers), scope), context);
        context.evaluateString(scope, "var SafeEvents = { definitions: function(callback) { callback(event) } };", "setup", 1, null);
        Path project = Path.of(System.getProperty("lsp.testProjectDir"));
        Path script = project.resolve("examples/kubejs8/server_scripts/storage_definitions.js");
        context.evaluateString(scope, Files.readString(script, StandardCharsets.UTF_8), script.toString(), 1, null);
        var safe = boxes.get(Identifier.parse("kubejs:experience_case"));
        var pouch = containers.get(Identifier.parse("kubejs:supply_pouch"));
        assertNotNull(safe);
        assertNotNull(pouch);
        assertEquals(4, safe.columns());
        assertEquals(3, safe.rows());
        assertEquals(500, safe.experience().capacity());
        assertTrue(safe.tags().contains(Identifier.parse("minecraft:logs")));
        assertEquals(25, pouch.columns() * pouch.rows());
        assertTrue(pouch.whitelist());
        for (var texture : new Identifier[]{safe.texture(), pouch.texture()}) {
            assertTrue(Files.isRegularFile(project.resolve("src/main/resources/assets/" + texture.getNamespace() + "/" + texture.getPath())));
        }
    }
}
