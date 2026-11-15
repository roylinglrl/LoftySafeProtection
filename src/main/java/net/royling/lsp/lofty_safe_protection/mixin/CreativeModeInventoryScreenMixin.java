package net.royling.lsp.lofty_safe_protection.mixin;

import net.royling.lsp.lofty_safe_protection.menu.SafeBoxContainer;
import net.royling.lsp.lofty_safe_protection.menu.InventoryItemContainer;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=CreativeModeInventoryScreen.class,remap=true)
public abstract class CreativeModeInventoryScreenMixin {
 @Inject(method="selectTab",at=@At("TAIL"))
 private void lsp$vanillaSlots(CreativeModeTab tab,CallbackInfo ci){((CreativeModeInventoryScreen)(Object)this).getMenu().slots.removeIf(slot->slot.container instanceof SafeBoxContainer || slot.container instanceof InventoryItemContainer);}
}
