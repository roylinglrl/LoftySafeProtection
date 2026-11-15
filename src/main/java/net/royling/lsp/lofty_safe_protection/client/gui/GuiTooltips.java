package net.royling.lsp.lofty_safe_protection.client.gui;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;

public final class GuiTooltips {
    private GuiTooltips() {}
    public static void draw(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y) {
        graphics.nextStratum();
        graphics.tooltip(font, List.of(ClientTooltipComponent.create(text.getVisualOrderText())), x, y, DefaultTooltipPositioner.INSTANCE, null);
    }
    public static void drawItem(GuiGraphicsExtractor graphics, net.minecraft.world.item.ItemStack stack, int x, int y) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        var font = minecraft.font;
        var lines = net.neoforged.neoforge.client.ClientHooks.gatherTooltipComponents(stack,
                net.minecraft.client.gui.screens.Screen.getTooltipFromItem(minecraft, stack), stack.getTooltipImage(),
                x, graphics.guiWidth(), graphics.guiHeight(), font);
        graphics.nextStratum();
        graphics.tooltip(font, lines, x, y, DefaultTooltipPositioner.INSTANCE, stack.get(net.minecraft.core.component.DataComponents.TOOLTIP_STYLE), stack);
    }
}
