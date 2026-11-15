package net.royling.lsp.lofty_safe_protection.mixin;

import net.royling.lsp.lofty_safe_protection.client.gui.SafeBoxPanel;
import net.royling.lsp.lofty_safe_protection.client.gui.InventoryContainerPanel;


import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen.class, remap = false)
public abstract class RecipeBookScreenMixin extends AbstractContainerScreen<AbstractContainerMenu> {
    protected RecipeBookScreenMixin(AbstractContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void lsp$overlayClick(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> callback) {
        if (InventoryContainerPanel.contains(event.x(), event.y())) callback.setReturnValue(super.mouseClicked(event, doubleClick));
    }

    @Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
    private void lsp$outside(double x, double y, int left, int top,
                                CallbackInfoReturnable<Boolean> callback) {
        if ((Object) this instanceof InventoryScreen && SafeBoxPanel.contains(left, top, x, y)
                || InventoryContainerPanel.contains(x, y)) {
            callback.setReturnValue(false);
        }
    }
}
