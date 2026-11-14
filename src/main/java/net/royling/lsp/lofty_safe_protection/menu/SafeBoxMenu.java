package net.royling.lsp.lofty_safe_protection.menu;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.storage.SafeBoxSavedData;


import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class SafeBoxMenu extends AbstractContainerMenu {
    public final SafeBoxSavedData.PlayerStorage state;
    public SafeBoxMenu(int id, Inventory inventory) { this(id, inventory, new SafeBoxSavedData.PlayerStorage(() -> {})); }
    public SafeBoxMenu(int id, Inventory inventory, SafeBoxSavedData.PlayerStorage state) {
        super(SafeBoxManager.MENU, id); this.state = state;
        addDataSlot(new DataSlot() { public int get() { return state.columns; } public void set(int n) { state.columns=n; } });
        addDataSlot(new DataSlot() { public int get() { return state.rows; } public void set(int n) { state.rows=n; } });
        addSlot(new Slot(state, 0, 80, 18) {
            @Override public boolean mayPlace(ItemStack s) { return state.contentsEmpty() && SafeBoxManager.definition(s) != null; }
            @Override public boolean mayPickup(Player p) { return state.contentsEmpty(); }
            @Override public int getMaxStackSize() { return 1; }
            @Override public void setChanged() { state.refreshSize(); super.setChanged(); }
        });
        for (int i=0;i<54;i++) {
            final int cellIndex=i;
            addSlot(new Slot(state, i+1, 8+(i%9)*18, 40+(i/9)*18) {
                @Override public boolean isActive() { return cellIndex%9 < state.columns && cellIndex/9 < state.rows; }
                @Override public boolean mayPlace(ItemStack s) { var def=SafeBoxManager.definition(state.getItem(0)); return isActive() && def!=null && def.accepts(s); }
                @Override public boolean mayPickup(Player p) { return isActive(); }
            });
        }
        for (int row=0;row<3;row++) for(int col=0;col<9;col++) addSlot(new Slot(inventory, col+row*9+9, 8+col*18, 158+row*18));
        for(int col=0;col<9;col++) addSlot(new Slot(inventory,col,8+col*18,216));
    }
    @Override public boolean stillValid(Player p) { return p.isAlive() && !p.isSpectator(); }
    @Override public ItemStack quickMoveStack(Player p, int index) {
        if(index<0 || index>=slots.size()) return ItemStack.EMPTY;
        Slot slot=slots.get(index);
        if(!slot.hasItem() || !slot.mayPickup(p)) return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(), original=stack.copy();
        boolean moved;
        if(index<55) moved=moveItemStackTo(stack,55,91,true);
        else if(SafeBoxManager.definition(stack)!=null) moved=moveItemStackTo(stack,0,1,false);
        else { var def=SafeBoxManager.definition(state.getItem(0)); moved=def!=null && def.accepts(stack) && moveItemStackTo(stack,1,55,false); }
        if(!moved) return ItemStack.EMPTY;
        if(stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(p,stack); return original;
    }
}
