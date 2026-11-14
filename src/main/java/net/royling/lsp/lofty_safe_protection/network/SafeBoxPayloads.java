package net.royling.lsp.lofty_safe_protection.network;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public final class SafeBoxPayloads {
    public record ExperienceTransferPayload(boolean deposit,int modifiers) implements CustomPacketPayload {
        public static final Type<ExperienceTransferPayload> TYPE=new Type<>(SafeBoxManager.id("experience"));
        public static final StreamCodec<FriendlyByteBuf,ExperienceTransferPayload> CODEC=StreamCodec.of((b,p)->{b.writeBoolean(p.deposit());b.writeByte(p.modifiers());},b->new ExperienceTransferPayload(b.readBoolean(),b.readUnsignedByte()));
        @Override public Type<ExperienceTransferPayload> type(){return TYPE;}
    }
    public record SetExpandedPayload(boolean expanded) implements CustomPacketPayload {
        public static final Type<SetExpandedPayload> TYPE=new Type<>(SafeBoxManager.id("open"));
        public static final StreamCodec<FriendlyByteBuf,SetExpandedPayload> CODEC=StreamCodec.of((b,p)->b.writeBoolean(p.expanded()),b->new SetExpandedPayload(b.readBoolean()));
        @Override public Type<SetExpandedPayload> type(){return TYPE;}
    }
    public record SyncDefinitionsPayload(String json) implements CustomPacketPayload {
        public static final Type<SyncDefinitionsPayload> TYPE=new Type<>(SafeBoxManager.id("definitions"));
        public static final StreamCodec<FriendlyByteBuf,SyncDefinitionsPayload> CODEC=StreamCodec.of((b,p)->b.writeUtf(p.json(),1048576),b->new SyncDefinitionsPayload(b.readUtf(1048576)));
        @Override public Type<SyncDefinitionsPayload> type(){return TYPE;}
    }
}
