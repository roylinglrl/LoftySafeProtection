package net.royling.lsp.lofty_safe_protection.mixin;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.menu.SafeBoxContainer;
import net.royling.lsp.lofty_safe_protection.menu.SafeBoxSlots;
import net.royling.lsp.lofty_safe_protection.menu.SafeBoxMenuAccess;
import net.royling.lsp.lofty_safe_protection.menu.InventoryContainerMenuAccess;

import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=InventoryMenu.class,remap=false)
public abstract class InventoryMenuMixin extends AbstractContainerMenu implements SafeBoxMenuAccess {
    @Unique private SafeBoxContainer lsp$box;
    protected InventoryMenuMixin(MenuType<?> type,int id){super(type,id);}
    @Inject(method="<init>",at=@At("TAIL"))
    private void lsp$init(Inventory inventory,boolean active,Player owner,CallbackInfo ci){
        lsp$box=new SafeBoxContainer(owner);
        for(Slot slot:SafeBoxSlots.slots(lsp$box))addSlot(slot);
        for(DataSlot data:SafeBoxSlots.data(lsp$box))addDataSlot(data);
        ((InventoryContainerMenuAccess) this).lsp$attachInventoryContainer(owner);
    }
    @Override public SafeBoxContainer lsp$storage(){return lsp$box;}
    @Inject(method="quickMoveStack",at=@At("HEAD"),cancellable=true)
    private void lsp$move(Player player,int index,CallbackInfoReturnable<ItemStack> ci){
        if(index<0||index>=slots.size()||lsp$box==null)return;
        Slot slot=slots.get(index);ItemStack stack=slot.getItem(),before=stack.copy();
        if(index>=SafeBoxSlots.FIRST&&index<SafeBoxSlots.END){
            if(stack.isEmpty()||!slot.mayPickup(player)||!moveItemStackTo(stack,9,45,true)){ci.setReturnValue(ItemStack.EMPTY);return;}
        }else if(index>=9&&index<45&&lsp$box.expanded&&lsp$box.interactive&&!stack.isEmpty()){
            boolean moved=false;
            if(SafeBoxManager.definition(stack)!=null)moved=moveItemStackTo(stack,46,47,false);
            else {var def=SafeBoxManager.definition(lsp$box.getItem(0));if(def!=null&&def.accepts(stack))moved=moveItemStackTo(stack,47,101,false);}
            if(!moved)return;
        }else return;
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);ci.setReturnValue(before);
    }
}
