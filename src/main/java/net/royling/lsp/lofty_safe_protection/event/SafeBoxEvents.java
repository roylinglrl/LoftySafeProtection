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
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.royling.lsp.lofty_safe_protection.network.PacketTransport;

public final class SafeBoxEvents {
    private SafeBoxEvents() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(SafeBoxEvents::onReload);
        MinecraftForge.EVENT_BUS.addListener(SafeBoxEvents::onDatapackSync);
        MinecraftForge.EVENT_BUS.addListener(SafeBoxEvents::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(SafeBoxEvents::onContainerClose);
        MinecraftForge.EVENT_BUS.addListener(SafeBoxEvents::onContainerOpen);
    }

    private static void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getContainer() instanceof InventoryContainerMenuAccess access) access.lsp$closeInventoryContainers();
    }

    private static void onContainerOpen(PlayerContainerEvent.Open event) {
        var inventory = event.getEntity().inventoryMenu;
        if (event.getContainer() != inventory && inventory instanceof InventoryContainerMenuAccess access) access.lsp$closeInventoryContainers();
    }

    private static void onReload(AddReloadListenerEvent event) {
        event.addListener(new SafeBoxDefinitionReloadListener());
        event.addListener(new InventoryContainerReloadListener());
        event.addListener(new DefinitionScriptReloadListener());
    }

    private static void onDatapackSync(OnDatapackSyncEvent event) {
        var payload = new SafeBoxPayloads.SyncDefinitionsPayload(SafeBoxDefinitionCodec.encode());
        if (event.getPlayer() != null) PacketTransport.sendToPlayer(event.getPlayer(), payload);
        else PacketTransport.sendToAllPlayers(payload);
        var containers = InventoryContainerNetworking.definitionsPayload();
        if (event.getPlayer() != null) PacketTransport.sendToPlayer(event.getPlayer(), containers);
        else PacketTransport.sendToAllPlayers(containers);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        var command = event.getDispatcher().register(Commands.literal(SafeBoxManager.MOD)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("give")
                        .then(Commands.argument("id", ResourceLocationArgument.id())
                                .suggests((context, builder) -> {
                                    SafeBoxManager.definitions.keySet().forEach(id -> builder.suggest(id.toString()));
                                    InventoryContainerManager.definitions.keySet().forEach(id -> builder.suggest(id.toString()));
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    var id = ResourceLocationArgument.getId(context, "id");
                                    boolean container = InventoryContainerManager.definitions.containsKey(id);
                                    if (!container && !SafeBoxManager.definitions.containsKey(id)) id = LegacySafeBoxIds.definitionId(id);
                                    if (!container && !SafeBoxManager.definitions.containsKey(id)) return 0;
                                    var player = context.getSource().getPlayerOrException();
                                    var stack = container ? InventoryContainerManager.stack(id) : SafeBoxManager.stack(id);
                                    if (!player.getInventory().add(stack)) player.drop(stack, false);
                                    return 1;
                                }))));
        event.getDispatcher().register(Commands.literal("safebox")
                .requires(source -> source.hasPermission(2)).redirect(command));
    }
}
