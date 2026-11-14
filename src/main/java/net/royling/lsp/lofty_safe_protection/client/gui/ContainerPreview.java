package net.royling.lsp.lofty_safe_protection.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.item.ContainerContentsSummary;

public record ContainerPreview(ContainerContentsSummary contents) implements TooltipComponent, ClientTooltipComponent {
    private static final ResourceLocation SLOT = SafeBoxManager.id("textures/gui/slot.png");

    @Override public int getHeight() { return contents.rows() * 9 + 6; }
    @Override public int getWidth(Font font) { return contents.columns() * 9 + 4; }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y + 2, 0);
        graphics.pose().scale(0.5f, 0.5f, 1);
        SafeBoxPanel.background(graphics, 0, 0, contents.columns() * 18 + 8, contents.rows() * 18 + 8);
        for (int row = 0; row < contents.rows(); row++) for (int column = 0; column < contents.columns(); column++) {
            int cell = row * 9 + column, slotX = 5 + column * 18, slotY = 5 + row * 18;
            graphics.blit(SLOT, slotX - 1, slotY - 1, 0, 0, 18, 18, 18, 18);
            var stack = contents.items().get(cell);
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, slotX, slotY);
                graphics.renderItemDecorations(font, stack, slotX, slotY);
            }
        }
        graphics.pose().popPose();
    }
}
