package net.royling.lsp.lofty_safe_protection.menu;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;

import java.util.function.Consumer;
import net.minecraft.world.inventory.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
public final class SafeBoxSlots {
    public static final int FIRST=46, END=101;
    public static java.util.List<DataSlot> data(SafeBoxContainer box){return java.util.List.of(
        new DataSlot(){public int get(){return box.state().columns;}public void set(int value){box.state().columns=value;}},
        new DataSlot(){public int get(){return box.state().rows;}public void set(int value){box.state().rows=value;}},
        new DataSlot(){public int get(){return box.state().experience()&0xffff;}public void set(int value){box.state().setExperience((box.state().experience()&0x7fff0000)|(value&0xffff));}},
        new DataSlot(){public int get(){return box.state().experience()>>>16;}public void set(int value){box.state().setExperience((box.state().experience()&0xffff)|((value&0x7fff)<<16));}});
    }
    public static java.util.List<Slot> slots(SafeBoxContainer box){
        var slots=new java.util.ArrayList<Slot>();
        slots.add(new Slot(box,0,184,8){
            @Override public boolean isActive(){return box.expanded&&box.interactive;}
            @Override public boolean mayPlace(ItemStack stack){return isActive()&&box.state().contentsEmpty()&&SafeBoxManager.definition(stack)!=null;}
            @Override public boolean mayPickup(Player player){return isActive()&&box.state().contentsEmpty();}
            @Override public int getMaxStackSize(){return 1;}
            @Override public void setChanged(){box.state().refreshSize();super.setChanged();}
        });
        for(int i=0;i<54;i++){
            final int cell=i;
            slots.add(new Slot(box,i+1,184+(i%9)*18,32+(i/9)*18){
                @Override public boolean isActive(){return box.expanded&&box.interactive&&cell%9<box.state().columns&&cell/9<box.state().rows;}
                @Override public boolean mayPlace(ItemStack stack){var def=SafeBoxManager.definition(box.getItem(0));return isActive()&&def!=null&&def.accepts(stack);}
                @Override public boolean mayPickup(Player p){return isActive();}
            });
        }
        return slots;
    }
}
