package net.royling.lsp.lofty_safe_protection.network;

import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public final class PayloadRegistrar {
    private int nextId;
    public <T extends PacketPayload> void playToServer(PacketPayload.Type<T> type, PacketCodec<FriendlyByteBuf, T> codec, BiConsumer<T, Context> handler) {
        register(type, codec, handler, NetworkDirection.PLAY_TO_SERVER);
    }
    public <T extends PacketPayload> void playToClient(PacketPayload.Type<T> type, PacketCodec<FriendlyByteBuf, T> codec, BiConsumer<T, Context> handler) {
        register(type, codec, handler, NetworkDirection.PLAY_TO_CLIENT);
    }
    private <T extends PacketPayload> void register(PacketPayload.Type<T> type, PacketCodec<FriendlyByteBuf, T> codec, BiConsumer<T, Context> handler, NetworkDirection direction) {
        PacketTransport.CHANNEL.registerMessage(nextId++, type.payloadClass(), (packet, buffer) -> codec.encode(buffer, packet), codec::decode, (packet, context) -> {
            var incoming = context.get();
            handler.accept(packet, new Context(incoming));
            incoming.setPacketHandled(true);
        }, Optional.of(direction));
    }
    public record Context(NetworkEvent.Context context) {
        public net.minecraft.world.entity.player.Player player() { return context.getSender(); }
        public void enqueueWork(Runnable work) { context.enqueueWork(work); }
    }
}
