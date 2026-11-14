package net.royling.lsp.lofty_safe_protection.menu;

import net.royling.lsp.lofty_safe_protection.storage.SafeBoxSavedData;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
public final class SafeBoxContainer extends SimpleContainer {
    private final Player owner;
    private final SafeBoxSavedData.PlayerStorage clientState=new SafeBoxSavedData.PlayerStorage(()->{});
    public boolean expanded;
    public boolean interactive=true;
    public SafeBoxContainer(Player owner){super(55);this.owner=owner;}
    public SafeBoxSavedData.PlayerStorage state(){return owner instanceof ServerPlayer p?SafeBoxSavedData.get(p).forPlayer(p.getUUID()):clientState;}
    @Override public ItemStack getItem(int slot){return state().getItem(slot);}
    @Override public void setItem(int slot,ItemStack stack){state().setItem(slot,stack);}
    @Override public ItemStack removeItem(int slot,int amount){return state().removeItem(slot,amount);}
    @Override public ItemStack removeItemNoUpdate(int slot){return state().removeItemNoUpdate(slot);}
    @Override public boolean isEmpty(){return state().isEmpty();}
    @Override public void clearContent(){state().clearContent();}
    @Override public void setChanged(){if(owner!=null)state().setChanged();}
}
