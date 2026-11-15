package net.royling.lsp.lofty_safe_protection.network;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;

import net.minecraft.network.FriendlyByteBuf;
import net.royling.lsp.lofty_safe_protection.network.PacketCodec;
import net.royling.lsp.lofty_safe_protection.network.PacketPayload;
public final class SafeBoxPayloads {
    public record ExperienceTransferPayload(boolean deposit,int modifiers) implements PacketPayload {
        public static final Type<ExperienceTransferPayload> TYPE=new Type<>(SafeBoxManager.id("experience"), ExperienceTransferPayload.class);
        public static final PacketCodec<FriendlyByteBuf,ExperienceTransferPayload> CODEC=PacketCodec.of((b,p)->{b.writeBoolean(p.deposit());b.writeByte(p.modifiers());},b->new ExperienceTransferPayload(b.readBoolean(),b.readUnsignedByte()));
        @Override public Type<ExperienceTransferPayload> type(){return TYPE;}
    }
    public record SetExpandedPayload(boolean expanded) implements PacketPayload {
        public static final Type<SetExpandedPayload> TYPE=new Type<>(SafeBoxManager.id("open"), SetExpandedPayload.class);
        public static final PacketCodec<FriendlyByteBuf,SetExpandedPayload> CODEC=PacketCodec.of((b,p)->b.writeBoolean(p.expanded()),b->new SetExpandedPayload(b.readBoolean()));
        @Override public Type<SetExpandedPayload> type(){return TYPE;}
    }
    public record SyncDefinitionsPayload(String json) implements PacketPayload {
        public static final Type<SyncDefinitionsPayload> TYPE=new Type<>(SafeBoxManager.id("definitions"), SyncDefinitionsPayload.class);
        public static final PacketCodec<FriendlyByteBuf,SyncDefinitionsPayload> CODEC=PacketCodec.of((b,p)->b.writeUtf(p.json(),1048576),b->new SyncDefinitionsPayload(b.readUtf(1048576)));
        @Override public Type<SyncDefinitionsPayload> type(){return TYPE;}
    }
}
