package net.royling.lsp.lofty_safe_protection.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import java.util.Set;
import net.royling.lsp.lofty_safe_protection.client.gui.ContainerScreenAccess;
import net.royling.lsp.lofty_safe_protection.client.gui.InventoryContainerPanel;
import net.royling.lsp.lofty_safe_protection.menu.InventoryItemContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractContainerScreen.class, remap = true)
public abstract class ContainerScreenMixin implements ContainerScreenAccess {
    @Shadow private ItemStack draggingItem;
    @Shadow private boolean isSplittingStack;
    @Shadow protected boolean isQuickCrafting;
    @Shadow @Final protected Set<Slot> quickCraftSlots;
    @Shadow private int quickCraftingRemainder;

    @Override
    @Invoker("renderSlot")
    public abstract void lsp$renderOverlaySlot(GuiGraphics graphics, Slot slot);

    @Override
    @Invoker("renderTooltip")
    public abstract void lsp$renderOverlayTooltip(GuiGraphics graphics, int mouseX, int mouseY);

    @Override
    @Accessor("hoveredSlot")
    public abstract void lsp$setHoveredSlot(Slot slot);

    @Invoker("renderFloatingItem")
    protected abstract void lsp$renderFloatingItem(GuiGraphics graphics, ItemStack stack, int x, int y, String label);

    @Override
    public void lsp$renderOverlayCarried(GuiGraphics graphics, int mouseX, int mouseY) {
        var screen = (AbstractContainerScreen<?>) (Object) this;
        ItemStack stack = draggingItem.isEmpty() ? screen.getMenu().getCarried() : draggingItem;
        if (stack.isEmpty()) return;
        int offset = draggingItem.isEmpty() ? 8 : 16;
        String label = null;
        if (!draggingItem.isEmpty() && isSplittingStack) stack = stack.copyWithCount(Mth.ceil(stack.getCount() / 2.0F));
        else if (isQuickCrafting && quickCraftSlots.size() > 1) {
            stack = stack.copyWithCount(quickCraftingRemainder);
            if (stack.isEmpty()) label = ChatFormatting.YELLOW + "0";
        }
        lsp$renderFloatingItem(graphics, stack, mouseX - screen.getGuiLeft() - 8, mouseY - screen.getGuiTop() - offset, label);
    }

    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
    private void lsp$overlayHover(Slot slot, double x, double y, CallbackInfoReturnable<Boolean> callback) {
        if (InventoryContainerPanel.supports((AbstractContainerScreen<?>) (Object) this)
                && (slot.container instanceof InventoryItemContainer || InventoryContainerPanel.contains(x, y)))
            callback.setReturnValue(slot == InventoryContainerPanel.slotAt(x, y));
    }

    @Inject(method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V", at = @At("HEAD"), cancellable = true)
    private void lsp$overlaySlot(GuiGraphics graphics, Slot slot, CallbackInfo callback) {
        if (InventoryContainerPanel.supports((AbstractContainerScreen<?>) (Object) this) && slot.container instanceof InventoryItemContainer
                && !InventoryContainerPanel.renderingSlots()) callback.cancel();
    }

    @Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
    private void lsp$outside(double x, double y, int left, int top, int button, CallbackInfoReturnable<Boolean> callback) {
        if (InventoryContainerPanel.contains(x, y)) callback.setReturnValue(false);
    }
}
