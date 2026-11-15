package net.royling.lsp.lofty_safe_protection.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;

public final class PacketTransport {
    static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(SafeBoxManager.id("main"), () -> "7", "7"::equals, "7"::equals);
    private PacketTransport() {}
    public static void sendToServer(PacketPayload packet) { CHANNEL.sendToServer(packet); }
    public static void sendToPlayer(ServerPlayer player, PacketPayload packet) { CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet); }
    public static void sendToAllPlayers(PacketPayload packet) { CHANNEL.send(PacketDistributor.ALL.noArg(), packet); }
}
