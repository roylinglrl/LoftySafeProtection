package net.royling.lsp.lofty_safe_protection.mixin;

import net.royling.lsp.lofty_safe_protection.client.gui.SlotPositionAccess;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.*;
@Mixin(value=Slot.class,remap=false)
public abstract class SlotMixin implements SlotPositionAccess {
    @Shadow() @Final @Mutable public int x;
    @Shadow() @Final @Mutable public int y;
    @Override public void lsp$position(int x,int y){this.x=x;this.y=y;}
}
