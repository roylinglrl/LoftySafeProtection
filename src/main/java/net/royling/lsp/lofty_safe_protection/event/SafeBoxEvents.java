package net.royling.lsp.lofty_safe_protection.event;

import net.royling.lsp.lofty_safe_protection.compat.legacy.LegacySafeBoxIds;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxDefinitionReloadListener;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerManager;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerReloadListener;
import net.royling.lsp.lofty_safe_protection.data.DefinitionScriptReloadListener;
import net.royling.lsp.lofty_safe_protection.network.InventoryContainerNetworking;
import net.royling.lsp.lofty_safe_protection.network.SafeBoxDefinitionCodec;
import net.royling.lsp.lofty_safe_protection.network.SafeBoxPayloads;
import net.royling.lsp.lofty_safe_protection.menu.InventoryContainerMenuAccess;


import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SafeBoxEvents {
    private SafeBoxEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(SafeBoxEvents::onReload);
        NeoForge.EVENT_BUS.addListener(SafeBoxEvents::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(SafeBoxEvents::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(SafeBoxEvents::onContainerClose);
        NeoForge.EVENT_BUS.addListener(SafeBoxEvents::onContainerOpen);
    }

    private static void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getContainer() instanceof InventoryContainerMenuAccess access) access.lsp$closeInventoryContainers();
    }

    private static void onContainerOpen(PlayerContainerEvent.Open event) {
        var inventory = event.getEntity().inventoryMenu;
        if (event.getContainer() != inventory && inventory instanceof InventoryContainerMenuAccess access) access.lsp$closeInventoryContainers();
    }

    private static void onReload(AddServerReloadListenersEvent event) {
        event.addListener(SafeBoxManager.id("safeboxes"), new SafeBoxDefinitionReloadListener());
        event.addListener(SafeBoxManager.id("inventory_containers"), new InventoryContainerReloadListener());
        event.addListener(SafeBoxManager.id("script_definitions"), new DefinitionScriptReloadListener());
        event.addDependency(SafeBoxManager.id("safeboxes"), SafeBoxManager.id("script_definitions"));
        event.addDependency(SafeBoxManager.id("inventory_containers"), SafeBoxManager.id("script_definitions"));
    }

    private static void onDatapackSync(OnDatapackSyncEvent event) {
        var payload = new SafeBoxPayloads.SyncDefinitionsPayload(SafeBoxDefinitionCodec.encode());
        if (event.getPlayer() != null) PacketDistributor.sendToPlayer(event.getPlayer(), payload);
        else PacketDistributor.sendToAllPlayers(payload);
        var containers = InventoryContainerNetworking.definitionsPayload();
        if (event.getPlayer() != null) PacketDistributor.sendToPlayer(event.getPlayer(), containers);
        else PacketDistributor.sendToAllPlayers(containers);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        var command = event.getDispatcher().register(Commands.literal(SafeBoxManager.MOD)
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("give")
                        .then(Commands.argument("id", IdentifierArgument.id())
                                .suggests((context, builder) -> {
                                    SafeBoxManager.definitions.keySet().forEach(id -> builder.suggest(id.toString()));
                                    InventoryContainerManager.definitions.keySet().forEach(id -> builder.suggest(id.toString()));
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    var id = IdentifierArgument.getId(context, "id");
                                    boolean container = InventoryContainerManager.definitions.containsKey(id);
                                    if (!container && !SafeBoxManager.definitions.containsKey(id)) id = LegacySafeBoxIds.definitionId(id);
                                    if (!container && !SafeBoxManager.definitions.containsKey(id)) return 0;
                                    var player = context.getSource().getPlayerOrException();
                                    var stack = container ? InventoryContainerManager.stack(id) : SafeBoxManager.stack(id);
                                    if (!player.getInventory().add(stack)) player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
                                    return 1;
                                }))));
        event.getDispatcher().register(Commands.literal("safebox")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).redirect(command));
    }
}
