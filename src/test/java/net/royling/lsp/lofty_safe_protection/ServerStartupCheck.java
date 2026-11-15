package net.royling.lsp.lofty_safe_protection;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerManager;

@EventBusSubscriber(modid = "lofty_safe_protection")
public final class ServerStartupCheck {
    private static int syncEvents;

    @SubscribeEvent public static void synced(OnDatapackSyncEvent event) {
        if (Boolean.getBoolean("lsp.startupCheck")) syncEvents++;
    }

    @SubscribeEvent public static void started(ServerStartedEvent event) throws Exception {
        if (!Boolean.getBoolean("lsp.startupCheck")) return;
        try {
            int[][] sizes = {{2,2,0},{3,2,0},{4,2,100},{3,3,300},{4,3,500}};
            String[] names = {"case_2x2","case_2x3","case_2x4","case_3x3","case_3x4"};
            for (int index = 0; index < names.length; index++) {
                var definition = SafeBoxManager.definitions.get(SafeBoxManager.id(names[index]));
                require(definition != null, "Missing default safe box " + names[index]);
                require(definition.columns() == sizes[index][0] && definition.rows() == sizes[index][1], "Incorrect safe box size");
                require(definition.experience().capacity() == sizes[index][2], "Incorrect XP capacity");
                require(!definition.accepts(new ItemStack(Items.BEDROCK)), "Global blacklist tag was not applied");
                require(definition.accepts(new ItemStack(Items.PAPER)), "Default safe box rejected paper");
                require(SafeBoxManager.definition(SafeBoxManager.stack(definition.id())) == definition, "Safe box item definition binding failed");
            }
            var map = InventoryContainerManager.definitions.get(SafeBoxManager.id("map_pouch"));
            require(map != null && map.columns() == 4 && map.rows() == 4, "Map pouch was not loaded");
            require(map.accepts(new ItemStack(Items.MAP)) && !map.accepts(new ItemStack(Items.DIAMOND)), "Map pouch filter failed");
            if (ModList.get().isLoaded("kubejs")) {
                var sicc = InventoryContainerManager.definitions.get(net.minecraft.resources.Identifier.parse("kubejs:sicc_pouch"));
                require(sicc != null && sicc.columns() == 5 && sicc.rows() == 5, "KubeJS SICC definition event failed");
                require(sicc.accepts(new ItemStack(Items.ENCHANTED_BOOK)), "KubeJS item filter failed");
                KubeJs8RuntimeCheck.verify(321, true);
                Path script = Path.of("kubejs/server_scripts/lsp_integration.js");
                String source = Files.readString(script, StandardCharsets.UTF_8);
                String updated = source.replace("capacity: 321", "capacity: 654")
                        .replace("  event.addSafeBox('lsp_kubejs8_test:retired_safe', 'lofty_safe_protection:textures/safebox/stone_safe_box.png', 2, 2)", "");
                require(!source.equals(updated), "Reload fixture was not updated");
                Files.writeString(script, updated, StandardCharsets.UTF_8);
                int previousSyncs = syncEvents;
                event.getServer().reloadResources(event.getServer().getPackRepository().getSelectedIds()).join();
                KubeJs8RuntimeCheck.verify(654, false);
                require(syncEvents > previousSyncs, "Definition reload did not trigger client data synchronization");
            }
            Path report = Path.of(System.getProperty("lsp.testProjectDir"), "build", "reports", "server-startup", "result.txt");
            Files.createDirectories(report.getParent());
            Files.writeString(report, "PASS: default definitions, item bindings, XP settings, live item tags, filters, KubeJS 8 add/replace/remove, script reload and definition synchronization", StandardCharsets.UTF_8);
        } finally { event.getServer().halt(false); }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
