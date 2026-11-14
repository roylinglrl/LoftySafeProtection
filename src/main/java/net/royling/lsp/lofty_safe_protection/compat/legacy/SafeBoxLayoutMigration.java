package net.royling.lsp.lofty_safe_protection.compat.legacy;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.storage.SafeBoxSavedData;

import net.minecraft.world.item.ItemStack;

public final class SafeBoxLayoutMigration {
    public static boolean update(SafeBoxSavedData.PlayerStorage state){
        var variant=LegacySafeBoxIds.definitionId(SafeBoxManager.variant(state.getItem(0)));
        int rows,columns;
        switch(variant==null?"":variant.toString()){
            case "lofty_safe_protection:case_2x3" -> {rows=2;columns=3;}
            case "lofty_safe_protection:case_2x4" -> {rows=2;columns=4;}
            case "lofty_safe_protection:case_3x4" -> {rows=3;columns=4;}
            default -> {return false;}
        }
        var definition=SafeBoxManager.definition(state.getItem(0));
        if(definition==null||definition.rows()!=rows||definition.columns()!=columns||state.columns!=rows||state.rows!=columns)return false;
        for(int cell=0;cell<54;cell++)if((cell%9>=state.columns||cell/9>=state.rows)&&!state.getItem(cell+1).isEmpty())return false;
        ItemStack[] moved=new ItemStack[54];java.util.Arrays.fill(moved,ItemStack.EMPTY);
        for(int r=0;r<state.rows;r++)for(int c=0;c<state.columns;c++)moved[c*9+r]=state.getItem(1+r*9+c);
        for(int cell=0;cell<54;cell++)state.setItem(cell+1,moved[cell]);
        state.columns=columns;state.rows=rows;state.setChanged();return true;
    }
}
