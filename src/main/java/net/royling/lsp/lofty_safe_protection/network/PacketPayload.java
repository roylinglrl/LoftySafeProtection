package net.royling.lsp.lofty_safe_protection.network;

import net.minecraft.resources.ResourceLocation;

public interface PacketPayload {
    record Type<T extends PacketPayload>(ResourceLocation id, Class<T> payloadClass) {}
    Type<? extends PacketPayload> type();
}
