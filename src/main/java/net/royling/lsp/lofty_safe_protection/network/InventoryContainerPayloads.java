package net.royling.lsp.lofty_safe_protection.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;

public final class InventoryContainerPayloads {
    private InventoryContainerPayloads() {}

    public record ContainerActionPayload(int menuId, int sourceSlot, int action) implements CustomPacketPayload {
        public static final Type<ContainerActionPayload> TYPE = new Type<>(SafeBoxManager.id("container_action"));
        public static final StreamCodec<FriendlyByteBuf, ContainerActionPayload> CODEC = StreamCodec.of(
                (buffer, payload) -> { buffer.writeVarInt(payload.menuId()); buffer.writeVarInt(payload.sourceSlot()); buffer.writeVarInt(payload.action()); },
                buffer -> new ContainerActionPayload(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()));
        @Override public Type<ContainerActionPayload> type() { return TYPE; }
    }

    public record SetOpenPayload(int menuId, int windowId, int slotId) implements CustomPacketPayload {
        public static final Type<SetOpenPayload> TYPE = new Type<>(SafeBoxManager.id("inventory_container_open"));
        public static final StreamCodec<FriendlyByteBuf, SetOpenPayload> CODEC = StreamCodec.of(
                (buffer, payload) -> { buffer.writeVarInt(payload.menuId()); buffer.writeVarInt(payload.windowId()); buffer.writeVarInt(payload.slotId()); },
                buffer -> new SetOpenPayload(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()));
        @Override public Type<SetOpenPayload> type() { return TYPE; }
    }

    public record WindowAcknowledgedPayload(int menuId, int windowId, int sourceSlot) implements CustomPacketPayload {
        public static final Type<WindowAcknowledgedPayload> TYPE = new Type<>(SafeBoxManager.id("inventory_container_acknowledged"));
        public static final StreamCodec<FriendlyByteBuf, WindowAcknowledgedPayload> CODEC = StreamCodec.of(
                (buffer, payload) -> { buffer.writeVarInt(payload.menuId()); buffer.writeVarInt(payload.windowId()); buffer.writeVarInt(payload.sourceSlot()); },
                buffer -> new WindowAcknowledgedPayload(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()));
        @Override public Type<WindowAcknowledgedPayload> type() { return TYPE; }
    }

    public record SyncDefinitionsPayload(String json) implements CustomPacketPayload {
        public static final Type<SyncDefinitionsPayload> TYPE = new Type<>(SafeBoxManager.id("inventory_container_definitions"));
        public static final StreamCodec<FriendlyByteBuf, SyncDefinitionsPayload> CODEC = StreamCodec.of(
                (buffer, payload) -> buffer.writeUtf(payload.json(), 1048576),
                buffer -> new SyncDefinitionsPayload(buffer.readUtf(1048576)));
        @Override public Type<SyncDefinitionsPayload> type() { return TYPE; }
    }
}
