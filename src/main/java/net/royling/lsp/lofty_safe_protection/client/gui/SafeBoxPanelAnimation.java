package net.royling.lsp.lofty_safe_protection.client.gui;



public final class SafeBoxPanelAnimation {
    public static final long DURATION=180_000_000L;
    private boolean open;
    private float from;
    private long started,duration=DURATION;
    public void setOpen(boolean value,long now){
        if(value==open)return;
        from=value(now);open=value;started=now;
        duration=Math.max(1,Math.round(DURATION*Math.abs((open?1:0)-from)));
    }
    public float value(long now){
        float t=Math.max(0,Math.min(1,(float)(now-started)/duration));
        float eased=t*t*(3-2*t);
        return from+((open?1:0)-from)*eased;
    }
    public void reset(){open=false;from=0;started=0;duration=DURATION;}
}
