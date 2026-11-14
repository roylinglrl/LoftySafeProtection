package net.royling.lsp.lofty_safe_protection.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;

public interface ContainerScreenAccess {
    void lsp$renderOverlaySlot(GuiGraphics graphics, Slot slot);
    void lsp$renderOverlayCarried(GuiGraphics graphics, int mouseX, int mouseY);
    void lsp$renderOverlayTooltip(GuiGraphics graphics, int mouseX, int mouseY);
    void lsp$setHoveredSlot(Slot slot);
}
