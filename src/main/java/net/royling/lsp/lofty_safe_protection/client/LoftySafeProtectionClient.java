package net.royling.lsp.lofty_safe_protection.client;

import net.royling.lsp.lofty_safe_protection.client.gui.SafeBoxExperienceControls;
import net.royling.lsp.lofty_safe_protection.client.gui.SafeBoxPanel;
import net.royling.lsp.lofty_safe_protection.client.gui.ContainerPreview;
import net.royling.lsp.lofty_safe_protection.client.gui.ContainerActionIcons;
import net.royling.lsp.lofty_safe_protection.client.gui.InventoryContainerPanel;
import net.royling.lsp.lofty_safe_protection.menu.ContainerActions;
import net.royling.lsp.lofty_safe_protection.client.renderer.SafeBoxItemRenderer;
import net.royling.lsp.lofty_safe_protection.client.renderer.TextureItemRenderer;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerManager;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.item.SafeBoxTooltips;
import net.royling.lsp.lofty_safe_protection.network.SafeBoxPayloads;
import net.royling.lsp.lofty_safe_protection.registry.SafeBoxCreativeTab;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.extensions.common.*;
import net.royling.lsp.lofty_safe_protection.network.PacketTransport;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.*;
import com.mojang.blaze3d.vertex.PoseStack;

@EventBusSubscriber(modid=SafeBoxManager.MOD,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class LoftySafeProtectionClient {
    @SubscribeEvent public static void menus(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent e){SafeBoxCreativeTab.refresh=SafeBoxPanel::refreshCreative;}
    @SubscribeEvent public static void tooltipComponents(RegisterClientTooltipComponentFactoriesEvent event){event.register(ContainerPreview.class,preview->preview);}
    private static void sendExpanded(boolean expanded){PacketTransport.sendToServer(new SafeBoxPayloads.SetExpandedPayload(expanded));}
    public static IClientItemExtensions itemExtension(){return new IClientItemExtensions(){
        private BlockEntityWithoutLevelRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){
            if(renderer==null) renderer=new BlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels()) {
                @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
                    if (stack.is(InventoryContainerManager.ITEM)) {
                        var definition = InventoryContainerManager.definition(stack);
                        var texture = definition == null ? new net.minecraft.resources.ResourceLocation("textures/item/bundle.png") : definition.texture();
                        TextureItemRenderer.render(texture, pose, buffers, light, overlay);
                    } else SafeBoxItemRenderer.render(stack,context,pose,buffers,light,overlay);
                }
            };return renderer;
        }
    };}
    @EventBusSubscriber(modid=SafeBoxManager.MOD,value=Dist.CLIENT)
    public static class InventoryScreenEvents {
        private static Button button;private static Button closeButton;private static InventoryScreen owner;
        private static Button withdrawExperience,depositExperience;
        private static Button sortButton,depositMatchingButton;
        private static boolean inventoryRendered;
        @SubscribeEvent public static void init(ScreenEvent.Init.Post event) {
            if(event.getScreen() instanceof InventoryScreen screen) {
                owner=screen;button=SafeBoxPanel.button(LoftySafeProtectionClient::sendExpanded);closeButton=SafeBoxPanel.closeButton(LoftySafeProtectionClient::sendExpanded);
                withdrawExperience=SafeBoxExperienceControls.button(false,p->PacketTransport.sendToServer(p));
                depositExperience=SafeBoxExperienceControls.button(true,p->PacketTransport.sendToServer(p));
                SafeBoxPanel.position(button,closeButton,screen.getGuiLeft(),screen.getGuiTop());event.addListener(button);event.addListener(closeButton);
                SafeBoxExperienceControls.position(withdrawExperience,depositExperience,screen.getGuiLeft(),screen.getGuiTop());
                event.addListener(withdrawExperience);event.addListener(depositExperience);
                sortButton=SafeBoxPanel.actionButton(ContainerActions.SORT);
                depositMatchingButton=SafeBoxPanel.actionButton(ContainerActions.DEPOSIT_MATCHING);
                SafeBoxPanel.positionActions(sortButton,depositMatchingButton,closeButton);
                event.addListener(sortButton);event.addListener(depositMatchingButton);
            }
        }
        @SubscribeEvent public static void render(ScreenEvent.Render.Pre event) {
            inventoryRendered=false;
            if(event.getScreen()==owner && button!=null){
                SafeBoxPanel.position(button,closeButton,owner.getGuiLeft(),owner.getGuiTop());
                SafeBoxExperienceControls.position(withdrawExperience,depositExperience,owner.getGuiLeft(),owner.getGuiTop());
                SafeBoxPanel.positionActions(sortButton,depositMatchingButton,closeButton);
            }
        }
        @SubscribeEvent public static void background(ContainerScreenEvent.Render.Background event) {
            if(event.getContainerScreen() instanceof InventoryScreen screen) {
                inventoryRendered=true;
                SafeBoxPanel.draw(event.getGuiGraphics(),screen.getGuiLeft(),screen.getGuiTop());
            }
        }
        @SubscribeEvent public static void itemTooltip(ItemTooltipEvent event) {
            var original=event.getToolTip();
            var updated=SafeBoxTooltips.append(event.getItemStack(),event.getEntity(),original);
            if(updated!=original){original.clear();original.addAll(updated);}
        }
        @SubscribeEvent public static void carriedTooltip(ScreenEvent.Render.Post event) {
            if(inventoryRendered && event.getScreen() instanceof InventoryScreen screen) {
                var message=SafeBoxTooltips.hoverWarning(screen.getSlotUnderMouse(),screen.getMenu().getCarried());
                if(message!=null&&!InventoryContainerPanel.contains(event.getMouseX(),event.getMouseY())){
                    var slot=screen.getSlotUnderMouse();
                    var graphics=event.getGuiGraphics();
                    graphics.pose().pushPose();graphics.pose().translate(0,0,350);
                    ContainerActionIcons.outline(graphics,screen.getGuiLeft()+slot.x,screen.getGuiTop()+slot.y,0xffe34c4c);
                    graphics.pose().popPose();
                }
                if(message!=null)event.getGuiGraphics().renderTooltip(Minecraft.getInstance().font,message,event.getMouseX(),event.getMouseY());
            }
        }
        @SubscribeEvent public static void close(ScreenEvent.Closing event){
            if(event.getScreen()==owner){
                SafeBoxPanel.close(LoftySafeProtectionClient::sendExpanded);
                owner=null;button=null;closeButton=null;inventoryRendered=false;
                withdrawExperience=null;depositExperience=null;
                sortButton=null;depositMatchingButton=null;
            }
        }
    }
}
