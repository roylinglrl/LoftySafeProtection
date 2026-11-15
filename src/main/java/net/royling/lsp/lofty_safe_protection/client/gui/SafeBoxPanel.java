package net.royling.lsp.lofty_safe_protection.client.gui;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.experience.SafeBoxExperienceService;
import net.royling.lsp.lofty_safe_protection.menu.SafeBoxContainer;
import net.royling.lsp.lofty_safe_protection.menu.SafeBoxSlots;
import net.royling.lsp.lofty_safe_protection.menu.SafeBoxMenuAccess;
import net.royling.lsp.lofty_safe_protection.registry.SafeBoxCreativeTab;
import net.royling.lsp.lofty_safe_protection.menu.ContainerActions;
import net.royling.lsp.lofty_safe_protection.network.InventoryContainerPayloads;
import net.royling.lsp.lofty_safe_protection.network.PacketTransport;
import net.minecraft.client.gui.components.Tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Consumer;

public final class SafeBoxPanel {
    private static final ResourceLocation BACKGROUND=SafeBoxManager.id("textures/gui/background.png"),SLOT=SafeBoxManager.id("textures/gui/slot.png");
    private static final SafeBoxPanelAnimation MOTION=new SafeBoxPanelAnimation();
    private static SafeBoxContainer owner;
    private static SafeBoxPanelLayout.Frame frame;
    public static void refreshCreative(){var c=Minecraft.getInstance();if(c.level!=null&&c.player!=null&&SafeBoxCreativeTab.TAB!=null)SafeBoxCreativeTab.TAB.buildContents(new net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters(c.level.enabledFeatures(),c.player.canUseGameMasterBlocks(),c.level.registryAccess()));}
    public static SafeBoxContainer storage(){var p=Minecraft.getInstance().player;return p!=null&&p.inventoryMenu instanceof SafeBoxMenuAccess a?a.lsp$storage():null;}
    public static void position(Button button,Button close,int left,int top){
        var box=storage();button.setX(left+SafeBoxPanelLayout.OFFSET_X);button.setY(top+SafeBoxPanelLayout.BOTTOM-SafeBoxPanelLayout.ICON);
        if(box==null){button.visible=true;button.active=true;close.visible=false;return;}
        if(owner!=box){MOTION.reset();owner=box;}
        long now=System.nanoTime();MOTION.setOpen(box.expanded,now);
        frame=SafeBoxPanelLayout.frame(left,top,box.state().columns,box.state().rows,SafeBoxExperienceService.visible(box.state()),MOTION.value(now));
        button.visible=SafeBoxPanelLayout.showToggle(box.expanded,frame.progress());button.active=button.visible;
        box.interactive=box.expanded&&frame.progress()>=1;
        close.setX(frame.closeX());close.setY(frame.closeY());
        close.visible=frame.progress()>0&&frame.height()>=36;close.active=box.expanded;
        var slots=Minecraft.getInstance().player.inventoryMenu.slots;
        for(int i=0;i<55;i++){
            int cell=i-1,x=i==0?SafeBoxPanelLayout.equipmentX(left):SafeBoxPanelLayout.cargoX(left,cell%9);
            int y=i==0?SafeBoxPanelLayout.equipmentY(top):SafeBoxPanelLayout.cargoY(top,box.state().rows,cell/9);
            ((SlotPositionAccess)slots.get(SafeBoxSlots.FIRST+i)).lsp$position(x-left,y-top);
        }
    }
    public static boolean contains(int left,int top,double x,double y){return frame!=null&&frame.progress()>0&&frame.contains(x,y);}
    public static Rect2i occupiedArea(int left,int top){
        var box=storage();
        boolean visibleFrame=box!=null&&owner==box&&frame!=null&&frame.progress()>0;
        boolean reservePanel=box!=null&&(box.expanded||visibleFrame);
        var bounds=SafeBoxPanelLayout.frame(left,top,box==null?0:box.state().columns,box==null?0:box.state().rows,box!=null&&SafeBoxExperienceService.visible(box.state()),reservePanel?1:0);
        int width=bounds.width(),height=bounds.height();
        if(visibleFrame){
            width=Math.max(width,frame.width());height=Math.max(height,frame.height());
        }
        return new Rect2i(left+SafeBoxPanelLayout.OFFSET_X,top+SafeBoxPanelLayout.BOTTOM-height,width,height);
    }
    public static void background(GuiGraphics g,int x,int y,int width,int height){
        var texture=BACKGROUND;
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        for(var p:NineSliceLayout.patches(x,y,width,height))
            g.blit(texture,p.x(),p.y(),p.width(),p.height(),(float)p.u(),(float)p.v(),p.sourceWidth(),p.sourceHeight(),32,32);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
    }
    public static void draw(GuiGraphics g,int left,int top){
        var box=storage();if(box==null||frame==null||frame.progress()<=0)return;
        background(g,frame.x(),frame.y(),frame.width(),frame.height());
        cell(g,box,0,SafeBoxPanelLayout.equipmentX(left),SafeBoxPanelLayout.equipmentY(top));
        if(box.interactive)ContainerActionIcons.outline(g,SafeBoxPanelLayout.equipmentX(left),SafeBoxPanelLayout.equipmentY(top),0xffe8be57);
        g.enableScissor(frame.x()+4,frame.y()+4,frame.x()+frame.width()-4,frame.y()+frame.height()-1);
        g.pose().pushPose();g.pose().translate(SafeBoxPanelLayout.cargoX(left,0),SafeBoxPanelLayout.equipmentY(top)+16,0);
        float scaleX=frame.progress(),scaleY=frame.progress();
        g.pose().scale(scaleX,scaleY,1);
        for(int r=0;r<box.state().rows;r++)for(int c=0;c<box.state().columns;c++)cell(g,box,1+r*9+c,c*18,-16-18*(box.state().rows-1-r));
        g.pose().popPose();
        SafeBoxExperienceControls.draw(g,box,left,top,frame.progress());
        g.disableScissor();
    }
    private static void cell(GuiGraphics g,SafeBoxContainer box,int index,int x,int y){
        var texture=SLOT;g.blit(texture,x-1,y-1,0,0,18,18,18,18);
        if(!box.interactive&&!box.getItem(index).isEmpty())g.renderItem(box.getItem(index),x,y);
    }
    private static void request(boolean expanded,Consumer<Boolean> send){var box=storage();if(box!=null&&box.expanded!=expanded){box.expanded=expanded;box.interactive=false;send.accept(expanded);}}
    public static Button button(Consumer<Boolean> send){return new Button(0,0,SafeBoxPanelLayout.ICON,SafeBoxPanelLayout.ICON,Component.translatable("container.lofty_safe_protection"),b->{var box=storage();if(box!=null)request(!box.expanded,send);},supplier->supplier.get()){
        @Override protected void renderWidget(GuiGraphics g,int mouseX,int mouseY,float partial){
            background(g,getX(),getY(),SafeBoxPanelLayout.ICON,SafeBoxPanelLayout.ICON);
            var box=storage();var def=box==null?null:SafeBoxManager.definition(box.getItem(0));
            var texture=def==null?SafeBoxManager.id("textures/safebox/safe_box_default.png"):def.texture();
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
            g.blit(texture,getX()+3,getY()+3,0,0,16,16,16,16);
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        }
    };}
    public static Button closeButton(Consumer<Boolean> send){return new Button(0,0,10,10,Component.translatable("gui.close"),b->request(false,send),supplier->supplier.get()){
        @Override protected void renderWidget(GuiGraphics g,int mouseX,int mouseY,float partial){
            int color=isHoveredOrFocused()?0xffaa3030:0xff404040;
            for(int i=0;i<6;i++){g.fill(getX()+2+i,getY()+2+i,getX()+3+i,getY()+3+i,color);g.fill(getX()+7-i,getY()+2+i,getX()+8-i,getY()+3+i,color);}
        }
    };}
    public static Button actionButton(int action){
        var label=Component.translatable(action==ContainerActions.SORT?"gui.lofty_safe_protection.sort":"gui.lofty_safe_protection.deposit_matching");
        var button=new Button(0,0,10,10,label,b->{
            var player=Minecraft.getInstance().player;
            if(player!=null&&player.containerMenu==player.inventoryMenu&&player.containerMenu.getCarried().isEmpty())
                PacketTransport.sendToServer(new InventoryContainerPayloads.ContainerActionPayload(player.containerMenu.containerId,-1,action));
        },supplier->supplier.get()){
            @Override protected void renderWidget(GuiGraphics graphics,int mouseX,int mouseY,float partial){ContainerActionIcons.draw(graphics,getX(),getY(),action,isHoveredOrFocused());}
        };
        button.setTooltip(Tooltip.create(label));
        return button;
    }
    public static void positionActions(Button sort,Button deposit,Button close){
        var box=storage();
        boolean visible=box!=null&&box.interactive&&SafeBoxManager.definition(box.getItem(0))!=null;
        sort.visible=visible;deposit.visible=visible;
        boolean active=visible&&Minecraft.getInstance().player.containerMenu.getCarried().isEmpty();
        sort.active=active;deposit.active=active;
        sort.setX(close.getX()-24);deposit.setX(close.getX()-12);sort.setY(close.getY());deposit.setY(close.getY());
    }
    public static void close(Consumer<Boolean> send){request(false,send);MOTION.reset();frame=null;owner=null;}
}
