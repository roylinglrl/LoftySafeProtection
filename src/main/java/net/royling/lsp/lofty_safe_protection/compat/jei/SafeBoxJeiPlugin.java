package net.royling.lsp.lofty_safe_protection.compat.jei;

import net.royling.lsp.lofty_safe_protection.client.gui.SafeBoxPanel;
import net.royling.lsp.lofty_safe_protection.client.gui.InventoryContainerPanel;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;


import java.util.List;
import java.util.Collection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;

@JeiPlugin
public final class SafeBoxJeiPlugin implements IModPlugin, IGuiContainerHandler<InventoryScreen> {
    @Override
    public Identifier getPluginUid() {
        return SafeBoxManager.id("inventory_sidecar");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(InventoryScreen.class, this);
        registration.addGlobalGuiHandler(new IGlobalGuiHandler() {
            @Override public Collection<Rect2i> getGuiExtraAreas() {
                var screen = Minecraft.getInstance().screen;
                if (!InventoryContainerPanel.supports(screen)) return List.of();
                return InventoryContainerPanel.occupiedAreas((AbstractContainerScreen<?>) screen);
            }
        });
    }

    @Override
    public List<Rect2i> getGuiExtraAreas(InventoryScreen screen) {
        return List.of(SafeBoxPanel.occupiedArea(screen.getGuiLeft(), screen.getGuiTop()));
    }
}
