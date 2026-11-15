package net.royling.lsp.lofty_safe_protection.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.inventory.Slot;

public interface ContainerScreenAccess {
    void lsp$renderOverlaySlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY);
    void lsp$renderOverlayCarried(GuiGraphicsExtractor graphics, int mouseX, int mouseY);
    void lsp$setHoveredSlot(Slot slot);
}
