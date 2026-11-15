package net.royling.lsp.lofty_safe_protection.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.royling.lsp.lofty_safe_protection.menu.ContainerActions;

public final class ContainerActionIcons {
    private ContainerActionIcons() {}

    public static void draw(GuiGraphicsExtractor graphics, int x, int y, int action, boolean hover) {
        int color = hover ? 0xffeeeeee : 0xff404040;
        if (hover) graphics.fill(x, y, x + 10, y + 10, 0xff777777);
        if (action == ContainerActions.SORT) {
            for (int row = 0; row < 3; row++) graphics.fill(x + 2, y + 2 + row * 3, x + 8 - row * 2, y + 3 + row * 3, color);
        } else {
            graphics.fill(x + 4, y + 1, x + 6, y + 6, color);
            graphics.fill(x + 2, y + 4, x + 8, y + 5, color);
            graphics.fill(x + 3, y + 5, x + 7, y + 6, color);
            graphics.fill(x + 1, y + 7, x + 9, y + 9, color);
            graphics.fill(x + 1, y + 5, x + 2, y + 7, color);
            graphics.fill(x + 8, y + 5, x + 9, y + 7, color);
        }
    }

    public static void outline(GuiGraphicsExtractor graphics, int x, int y, int color) {
        graphics.fill(x - 1, y - 1, x + 17, y, color);
        graphics.fill(x - 1, y + 16, x + 17, y + 17, color);
        graphics.fill(x - 1, y, x, y + 16, color);
        graphics.fill(x + 16, y, x + 17, y + 16, color);
    }
}
