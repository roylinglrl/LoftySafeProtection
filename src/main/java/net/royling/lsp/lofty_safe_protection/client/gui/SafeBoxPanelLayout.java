package net.royling.lsp.lofty_safe_protection.client.gui;



public final class SafeBoxPanelLayout {
    public static final int BORDER=4, ICON=22, OFFSET_X=178, BOTTOM=166;
    public static int width(int columns){return Math.max(40,26+18*columns);}
    public static int height(int rows){return Math.max(38,18+18*rows);}
    public static int width(int columns,boolean experience){return experience?Math.max(80,width(columns)):width(columns);}
    public static int height(int rows,boolean experience){return height(rows)+(experience?24:0);}
    public static int equipmentX(int left){return left+OFFSET_X+3;}
    public static int equipmentY(int top){return top+BOTTOM-ICON+3;}
    public static int cargoX(int left,int column){return equipmentX(left)+18*(column+1);}
    public static int cargoY(int top,int rows,int row){return equipmentY(top)-18*(rows-1-row);}
    public static boolean showToggle(boolean expanded,float progress){return !expanded&&progress<=0;}
    public static Frame frame(int left,int top,int columns,int rows,float progress){
        return frame(left,top,columns,rows,false,progress);
    }
    public static Frame frame(int left,int top,int columns,int rows,boolean experience,float progress){
        float p=Math.max(0,Math.min(1,progress));
        int w=Math.round(ICON+(width(columns,experience)-ICON)*p),h=Math.round(ICON+(height(rows,experience)-ICON)*p);
        return new Frame(left+OFFSET_X,top+BOTTOM-h,w,h,p);
    }
    public record Frame(int x,int y,int width,int height,float progress){
        public boolean contains(double mx,double my){return mx>=x&&mx<x+width&&my>=y&&my<y+height;}
        public int closeX(){return x+width-14;}
        public int closeY(){return y+4;}
    }
}
