package net.royling.lsp.lofty_safe_protection.network;

import net.minecraft.network.FriendlyByteBuf;
import net.royling.lsp.lofty_safe_protection.network.PacketCodec;
import net.royling.lsp.lofty_safe_protection.network.PacketPayload;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;

public final class InventoryContainerPayloads {
    private InventoryContainerPayloads() {}

    public record ContainerActionPayload(int menuId, int sourceSlot, int action) implements PacketPayload {
        public static final Type<ContainerActionPayload> TYPE = new Type<>(SafeBoxManager.id("container_action"), ContainerActionPayload.class);
        public static final PacketCodec<FriendlyByteBuf, ContainerActionPayload> CODEC = PacketCodec.of(
                (buffer, payload) -> { buffer.writeVarInt(payload.menuId()); buffer.writeVarInt(payload.sourceSlot()); buffer.writeVarInt(payload.action()); },
                buffer -> new ContainerActionPayload(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()));
        @Override public Type<ContainerActionPayload> type() { return TYPE; }
    }

    public record SetOpenPayload(int menuId, int windowId, int slotId) implements PacketPayload {
        public static final Type<SetOpenPayload> TYPE = new Type<>(SafeBoxManager.id("inventory_container_open"), SetOpenPayload.class);
        public static final PacketCodec<FriendlyByteBuf, SetOpenPayload> CODEC = PacketCodec.of(
                (buffer, payload) -> { buffer.writeVarInt(payload.menuId()); buffer.writeVarInt(payload.windowId()); buffer.writeVarInt(payload.slotId()); },
                buffer -> new SetOpenPayload(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()));
        @Override public Type<SetOpenPayload> type() { return TYPE; }
    }

    public record WindowAcknowledgedPayload(int menuId, int windowId, int sourceSlot) implements PacketPayload {
        public static final Type<WindowAcknowledgedPayload> TYPE = new Type<>(SafeBoxManager.id("inventory_container_acknowledged"), WindowAcknowledgedPayload.class);
        public static final PacketCodec<FriendlyByteBuf, WindowAcknowledgedPayload> CODEC = PacketCodec.of(
                (buffer, payload) -> { buffer.writeVarInt(payload.menuId()); buffer.writeVarInt(payload.windowId()); buffer.writeVarInt(payload.sourceSlot()); },
                buffer -> new WindowAcknowledgedPayload(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()));
        @Override public Type<WindowAcknowledgedPayload> type() { return TYPE; }
    }

    public record SyncDefinitionsPayload(String json) implements PacketPayload {
        public static final Type<SyncDefinitionsPayload> TYPE = new Type<>(SafeBoxManager.id("inventory_container_definitions"), SyncDefinitionsPayload.class);
        public static final PacketCodec<FriendlyByteBuf, SyncDefinitionsPayload> CODEC = PacketCodec.of(
                (buffer, payload) -> buffer.writeUtf(payload.json(), 1048576),
                buffer -> new SyncDefinitionsPayload(buffer.readUtf(1048576)));
        @Override public Type<SyncDefinitionsPayload> type() { return TYPE; }
    }
}
