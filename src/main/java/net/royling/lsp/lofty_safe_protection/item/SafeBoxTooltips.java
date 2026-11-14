package net.royling.lsp.lofty_safe_protection.item;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.menu.SafeBoxContainer;
import net.royling.lsp.lofty_safe_protection.menu.SafeBoxSlots;
import net.royling.lsp.lofty_safe_protection.menu.SafeBoxMenuAccess;
import net.royling.lsp.lofty_safe_protection.storage.SafeBoxSavedData;


import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SafeBoxTooltips {
    public static Component capacity(int rows,int columns){
        return Component.translatable("tooltip.lofty_safe_protection.capacity",rows*columns,rows,columns).withStyle(ChatFormatting.GRAY);
    }
    public static Component locked(){return Component.translatable("tooltip.lofty_safe_protection.empty_before_unequip").withStyle(ChatFormatting.RED);}
    public static Component incompatible(){return Component.translatable("tooltip.lofty_safe_protection.incompatible").withStyle(ChatFormatting.RED);}
    public static Component usage(int used,int total){return Component.translatable("tooltip.lofty_safe_protection.usage",used,total).withStyle(used>=total?ChatFormatting.GOLD:ChatFormatting.GRAY);}

    public static List<Component> append(ItemStack stack,Player player,List<Component> original){
        if(original.isEmpty()||SafeBoxManager.ITEM==null||!stack.is(SafeBoxManager.ITEM))return original;
        SafeBoxSavedData.PlayerStorage equipped=null;
        if(player!=null&&player.inventoryMenu instanceof SafeBoxMenuAccess access){
            var state=access.lsp$storage().state();
            if(state.getItem(0)==stack)equipped=state;
        }
        var def=SafeBoxManager.definition(stack);
        int rows=equipped!=null?equipped.rows:def!=null?def.rows():0;
        int columns=equipped!=null?equipped.columns:def!=null?def.columns():0;
        if(rows<=0||columns<=0)return original;
        var result=new ArrayList<>(original);
        result.add(1,capacity(rows,columns));
        int used=0;
        if(equipped!=null)for(int cell=1;cell<55;cell++)if(!equipped.getItem(cell).isEmpty())used++;
        result.add(2,usage(used,rows*columns));
        if(equipped!=null&&!equipped.contentsEmpty())result.add(3,locked());
        return result;
    }

    public static Component hoverWarning(Slot slot,ItemStack carried){
        if(carried.isEmpty()||slot==null||!(slot.container instanceof SafeBoxContainer box)||!slot.isActive())return null;
        if(slot.index==SafeBoxSlots.FIRST){
            if(SafeBoxManager.definition(carried)==null)return incompatible();
            return box.state().contentsEmpty()?null:locked();
        }
        if(slot.index>SafeBoxSlots.FIRST&&slot.index<SafeBoxSlots.END){
            var def=SafeBoxManager.definition(box.getItem(0));
            if(def==null||!def.accepts(carried))return incompatible();
        }
        return null;
    }
}
