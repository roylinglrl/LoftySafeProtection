package net.royling.lsp.lofty_safe_protection.client.renderer;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

public final class SafeBoxItemRenderer {
    public static void render(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
        var def=SafeBoxManager.definition(stack);
        ResourceLocation texture=def==null?SafeBoxManager.id("textures/safebox/safe_box_default.png"):def.texture();
        TextureItemRenderer.render(texture, pose, buffers, light, overlay);
    }
}
