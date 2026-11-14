package net.royling.lsp.lofty_safe_protection.client.gui;


import java.util.*;

public final class NineSliceLayout {
    public record Patch(int x,int y,int width,int height,int u,int v,int sourceWidth,int sourceHeight){}
    public static List<Patch> patches(int x,int y,int width,int height){
        if(width<8||height<8)throw new IllegalArgumentException("Nine-slice size must be at least 8x8");
        int[] dx={x,x+4,x+width-4},dy={y,y+4,y+height-4};
        int[] dw={4,width-8,4},dh={4,height-8,4},uv={0,4,28},source={4,24,4};
        List<Patch> result=new ArrayList<>(9);
        for(int row=0;row<3;row++)for(int col=0;col<3;col++)
            if(dw[col]>0&&dh[row]>0)result.add(new Patch(dx[col],dy[row],dw[col],dh[row],uv[col],uv[row],source[col],source[row]));
        return result;
    }
}
