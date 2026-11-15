package net.royling.lsp.lofty_safe_protection.compat.kubejs;

import dev.latvian.mods.rhino.ContextFactory;
import dev.latvian.mods.rhino.ScriptableObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerDefinition;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SiccPouchScriptTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        net.royling.lsp.lofty_safe_protection.TestRegistries.initialize();
    }

    @Test
    void realScriptRegistersTheExpectedContainerAndFilters() throws Exception {
        var boxes = new HashMap<Identifier, SafeBoxManager.Definition>();
        var containers = new HashMap<Identifier, InventoryContainerDefinition>();
        var context = new ContextFactory().enter();
        var scope = context.initStandardObjects();
        ScriptableObject.putProperty(scope, "event", context.javaToJS(new SafeDefinitionEvent(boxes, containers), scope), context);
        context.evaluateString(scope, "var SafeEvents = { definitions: function(callback) { callback(event) } };", "setup", 1, null);
        var path = Path.of(System.getProperty("lsp.testProjectDir"), "examples", "kubejs", "server_scripts", "sicc_pouch.js");
        context.evaluateString(scope, Files.readString(path, StandardCharsets.UTF_8), path.toString(), 1, null);
        var pouch = containers.get(Identifier.parse("kubejs:sicc_pouch"));
        assertNotNull(pouch);
        assertEquals(5, pouch.columns());
        assertEquals(5, pouch.rows());
        assertTrue(pouch.whitelist());
        for (String tag : Set.of("c:nuggets", "c:dyes", "c:dusts", "c:gunpowders", "minecraft:trim_templates"))
            assertTrue(pouch.tags().contains(Identifier.parse(tag)), tag);
        for (var id : pouch.items()) assertTrue(BuiltInRegistries.ITEM.containsKey(id), id.toString());
        for (var item : new net.minecraft.world.item.Item[]{Items.PAPER, Items.ENCHANTED_BOOK, Items.GUNPOWDER,
                Items.FLOWER_BANNER_PATTERN, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE})
            assertTrue(pouch.accepts(new ItemStack(item)), item.toString());
        assertFalse(pouch.accepts(new ItemStack(Items.DIAMOND)));
    }
}
