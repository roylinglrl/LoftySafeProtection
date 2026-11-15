package net.royling.lsp.lofty_safe_protection.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.royling.lsp.lofty_safe_protection.client.gui.ContainerScreenAccess;
import net.royling.lsp.lofty_safe_protection.client.gui.InventoryContainerPanel;
import net.royling.lsp.lofty_safe_protection.menu.InventoryItemContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractContainerScreen.class, remap = false)
public abstract class ContainerScreenMixin implements ContainerScreenAccess {
    @Override
    @Invoker("extractSlot")
    public abstract void lsp$renderOverlaySlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY);

    @Override
    @Accessor("hoveredSlot")
    public abstract void lsp$setHoveredSlot(Slot slot);

    @Override
    public void lsp$renderOverlayCarried(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        ((AbstractContainerScreen<?>) (Object) this).extractCarriedItem(graphics, mouseX, mouseY);
    }

    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
    private void lsp$overlayHover(Slot slot, double x, double y, CallbackInfoReturnable<Boolean> callback) {
        if (InventoryContainerPanel.supports((AbstractContainerScreen<?>) (Object) this)
                && (slot.container instanceof InventoryItemContainer || InventoryContainerPanel.contains(x, y)))
            callback.setReturnValue(slot == InventoryContainerPanel.slotAt(x, y));
    }

    @Inject(method = "extractSlot", at = @At("HEAD"), cancellable = true)
    private void lsp$overlaySlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo callback) {
        if (InventoryContainerPanel.supports((AbstractContainerScreen<?>) (Object) this) && slot.container instanceof InventoryItemContainer
                && !InventoryContainerPanel.renderingSlots()) callback.cancel();
    }

    @Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
    private void lsp$outside(double x, double y, int left, int top, CallbackInfoReturnable<Boolean> callback) {
        if (InventoryContainerPanel.contains(x, y)) callback.setReturnValue(false);
    }
}
