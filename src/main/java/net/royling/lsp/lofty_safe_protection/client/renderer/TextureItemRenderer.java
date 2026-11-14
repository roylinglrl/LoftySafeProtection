package net.royling.lsp.lofty_safe_protection.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class TextureItemRenderer {
    private TextureItemRenderer() {}

    public static void render(ResourceLocation texture, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var vertex = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        var p = pose.last();
        vertex.addVertex(p.pose(), 0, 0, 0.5f).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(p, 0, 0, 1);
        vertex.addVertex(p.pose(), 1, 0, 0.5f).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(p, 0, 0, 1);
        vertex.addVertex(p.pose(), 1, 1, 0.5f).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(p, 0, 0, 1);
        vertex.addVertex(p.pose(), 0, 1, 0.5f).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(p, 0, 0, 1);
    }
}
