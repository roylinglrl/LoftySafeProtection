package net.royling.lsp.lofty_safe_protection.client;

import net.royling.lsp.lofty_safe_protection.client.gui.SafeBoxExperienceControls;
import net.royling.lsp.lofty_safe_protection.client.gui.SafeBoxPanel;
import net.royling.lsp.lofty_safe_protection.client.gui.ContainerPreview;
import net.royling.lsp.lofty_safe_protection.client.gui.ContainerActionIcons;
import net.royling.lsp.lofty_safe_protection.client.gui.InventoryContainerPanel;
import net.royling.lsp.lofty_safe_protection.menu.ContainerActions;
import net.royling.lsp.lofty_safe_protection.client.renderer.SafeBoxItemRenderer;

import net.royling.lsp.lofty_safe_protection.data.InventoryContainerManager;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.item.SafeBoxTooltips;
import net.royling.lsp.lofty_safe_protection.network.SafeBoxPayloads;
import net.royling.lsp.lofty_safe_protection.registry.SafeBoxCreativeTab;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.*;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.*;
import com.mojang.blaze3d.vertex.PoseStack;

@EventBusSubscriber(modid=SafeBoxManager.MOD,value=Dist.CLIENT)
public final class LoftySafeProtectionClient {
    @SubscribeEvent public static void menus(RegisterMenuScreensEvent e){SafeBoxCreativeTab.refresh=SafeBoxPanel::refreshCreative;}
    @SubscribeEvent public static void tooltipComponents(RegisterClientTooltipComponentFactoriesEvent event){event.register(ContainerPreview.class,preview->preview);}
    private static void sendExpanded(boolean expanded){net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new SafeBoxPayloads.SetExpandedPayload(expanded));}
    @SubscribeEvent public static void models(RegisterSpecialModelRendererEvent event) {
        event.register(SafeBoxManager.id("container"), SafeBoxItemRenderer.Unbaked.CODEC);
    }
    @EventBusSubscriber(modid=SafeBoxManager.MOD,value=Dist.CLIENT)
    public static class InventoryScreenEvents {
        private static Button button;private static Button closeButton;private static InventoryScreen owner;
        private static Button withdrawExperience,depositExperience;
        private static Button sortButton,depositMatchingButton;
        private static boolean inventoryRendered;
        @SubscribeEvent public static void init(ScreenEvent.Init.Post event) {
            if(event.getScreen() instanceof InventoryScreen screen) {
                owner=screen;button=SafeBoxPanel.button(LoftySafeProtectionClient::sendExpanded);closeButton=SafeBoxPanel.closeButton(LoftySafeProtectionClient::sendExpanded);
                withdrawExperience=SafeBoxExperienceControls.button(false,p->net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(p));
                depositExperience=SafeBoxExperienceControls.button(true,p->net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(p));
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
        @SubscribeEvent public static void background(ScreenEvent.Render.Background event) {
            if(event.getScreen() instanceof InventoryScreen screen) {
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
                    graphics.pose().pushMatrix();graphics.pose().translate(0,0);
                    ContainerActionIcons.outline(graphics,screen.getGuiLeft()+slot.x,screen.getGuiTop()+slot.y,0xffe34c4c);
                    graphics.pose().popMatrix();
                }
                if(message!=null)net.royling.lsp.lofty_safe_protection.client.gui.GuiTooltips.draw(event.getGuiGraphics(), Minecraft.getInstance().font,message,event.getMouseX(),event.getMouseY());
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
