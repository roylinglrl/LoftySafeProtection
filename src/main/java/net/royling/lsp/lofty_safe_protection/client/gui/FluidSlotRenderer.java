package net.royling.lsp.lofty_safe_protection.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import org.joml.Vector3f;

public final class FluidSlotRenderer {
    private static final ResourceLocation FRAME = SafeBoxManager.id("textures/gui/fluid_slot.png");
    private FluidSlotRenderer() {}

    public static void draw(GuiGraphics graphics, int x, int y, FluidStack fluid, int capacity) {
        graphics.blit(FRAME, x, y, 0, 0, 64, 16, 64, 16);
        if (!fluid.isEmpty() && capacity > 0) {
            var extension = IClientFluidTypeExtensions.of(fluid.getFluid());
            var texture = extension.getStillTexture(fluid);
            if (texture != null) {
                var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
                int width = Math.max(1, (int) (60L * Math.min(fluid.getAmount(), capacity) / capacity));
                var matrix = graphics.pose().last().pose();
                var first = matrix.transformPosition(x + 2, y + 2, 0, new Vector3f());
                var second = matrix.transformPosition(x + 2 + width, y + 14, 0, new Vector3f());
                int tint = extension.getTintColor(fluid);
                graphics.flush();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor((tint >> 16 & 255) / 255f, (tint >> 8 & 255) / 255f, (tint & 255) / 255f, (tint >>> 24) / 255f);
                graphics.enableScissor((int) Math.floor(first.x), (int) Math.floor(first.y), (int) Math.ceil(second.x), (int) Math.ceil(second.y));
                try {
                    for (int offset = 0; offset < width; offset += 16) graphics.blit(x + 2 + offset, y + 2, 0, 16, 16, sprite);
                } finally {
                    graphics.disableScissor();
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                }
            }
        }
        for (int mark = 2; mark <= 60; mark += 2) {
            int height = mark % 10 == 6 ? 4 : 2;
            graphics.blit(FRAME, x + mark, y + 14 - height, mark, 14 - height, 1, height, 64, 16);
        }
    }

    public static List<Component> tooltip(FluidStack fluid, int capacity) {
        return List.of(fluid.isEmpty() ? Component.translatable("tooltip.lofty_safe_protection.fluid_empty") : fluid.getHoverName(),
                Component.translatable("tooltip.lofty_safe_protection.fluid_amount", fluid.getAmount(), capacity));
    }
}
