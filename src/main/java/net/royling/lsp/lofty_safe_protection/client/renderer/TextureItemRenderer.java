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
        vertex.vertex(p.pose(), 0, 0, 0.5f).color(255, 255, 255, 255).uv(0, 1).overlayCoords(overlay).uv2(light).normal(p.normal(), 0, 0, 1).endVertex();
        vertex.vertex(p.pose(), 1, 0, 0.5f).color(255, 255, 255, 255).uv(1, 1).overlayCoords(overlay).uv2(light).normal(p.normal(), 0, 0, 1).endVertex();
        vertex.vertex(p.pose(), 1, 1, 0.5f).color(255, 255, 255, 255).uv(1, 0).overlayCoords(overlay).uv2(light).normal(p.normal(), 0, 0, 1).endVertex();
        vertex.vertex(p.pose(), 0, 1, 0.5f).color(255, 255, 255, 255).uv(0, 0).overlayCoords(overlay).uv2(light).normal(p.normal(), 0, 0, 1).endVertex();
    }
}
