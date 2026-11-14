package net.royling.lsp.lofty_safe_protection.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryContainerWindowPayloadTest {
    @Test
    void openingAndClosingKeepWindowAndSourceIdentifiersSeparate() {
        for (var payload : new InventoryContainerPayloads.SetOpenPayload[]{
                new InventoryContainerPayloads.SetOpenPayload(0, 2, 37),
                new InventoryContainerPayloads.SetOpenPayload(0, 2, -1),
                new InventoryContainerPayloads.SetOpenPayload(8, -1, -1)}) {
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                InventoryContainerPayloads.SetOpenPayload.CODEC.encode(buffer, payload);
                assertEquals(payload, InventoryContainerPayloads.SetOpenPayload.CODEC.decode(buffer));
            } finally { buffer.release(); }
        }
        var reply = new InventoryContainerPayloads.WindowAcknowledgedPayload(0, 2, 37);
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            InventoryContainerPayloads.WindowAcknowledgedPayload.CODEC.encode(buffer, reply);
            assertEquals(reply, InventoryContainerPayloads.WindowAcknowledgedPayload.CODEC.decode(buffer));
        } finally { buffer.release(); }
    }
}
