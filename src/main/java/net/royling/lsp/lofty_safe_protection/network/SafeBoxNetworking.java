package net.royling.lsp.lofty_safe_protection.network;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.experience.SafeBoxExperienceService;


import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class SafeBoxNetworking {
    private SafeBoxNetworking() {}

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("8");
        InventoryContainerNetworking.register(registrar);
        registrar.playToServer(SafeBoxPayloads.ExperienceTransferPayload.TYPE, SafeBoxPayloads.ExperienceTransferPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SafeBoxExperienceService.transfer(player, payload.deposit(), payload.modifiers());
                    }
                }));
        registrar.playToServer(SafeBoxPayloads.SetExpandedPayload.TYPE, SafeBoxPayloads.SetExpandedPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && player.isAlive() && !player.isSpectator()) {
                        PacketDistributor.sendToPlayer(player, new SafeBoxPayloads.SyncDefinitionsPayload(SafeBoxDefinitionCodec.encode()));
                        SafeBoxManager.setExpanded(player, payload.expanded());
                    }
                }));
        registrar.playToClient(SafeBoxPayloads.SyncDefinitionsPayload.TYPE, SafeBoxPayloads.SyncDefinitionsPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> SafeBoxDefinitionCodec.decode(payload.json())));
    }
}
