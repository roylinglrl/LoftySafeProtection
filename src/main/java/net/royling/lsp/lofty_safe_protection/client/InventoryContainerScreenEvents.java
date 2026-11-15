package net.royling.lsp.lofty_safe_protection.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.datafixers.util.Either;
import net.royling.lsp.lofty_safe_protection.client.gui.ContainerPreview;
import net.royling.lsp.lofty_safe_protection.client.gui.SafeBoxPanel;
import net.royling.lsp.lofty_safe_protection.item.ContainerContentsSummary;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.royling.lsp.lofty_safe_protection.client.gui.InventoryContainerPanel;
import net.royling.lsp.lofty_safe_protection.menu.InventoryItemContainer;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = SafeBoxManager.MOD, value = Dist.CLIENT)
public final class InventoryContainerScreenEvents {
    private static int suppressedRelease = -1;

    private InventoryContainerScreenEvents() {}

    @SubscribeEvent
    public static void preview(RenderTooltipEvent.GatherComponents event) {
        if (!Screen.hasShiftDown()) return;
        var summary = ContainerContentsSummary.fromItem(event.getItemStack());
        if (summary == null) {
            var box = SafeBoxPanel.storage();
            if (box == null || event.getItemStack() != box.getItem(0) || box.state().columns < 1 || box.state().rows < 1) return;
            var items = new ArrayList<net.minecraft.world.item.ItemStack>();
            for (int cell = 1; cell < 55; cell++) items.add(box.getItem(cell));
            summary = new ContainerContentsSummary(box.state().columns, box.state().rows, List.copyOf(items));
        }
        event.getTooltipElements().add(Either.right(new ContainerPreview(summary)));
    }

    @SubscribeEvent
    public static void render(ScreenEvent.Render.Pre event) {
        if (InventoryContainerPanel.supports(event.getScreen()))
            InventoryContainerPanel.position((AbstractContainerScreen<?>) event.getScreen());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void foreground(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen && InventoryContainerPanel.supports(screen))
            InventoryContainerPanel.draw(event.getGuiGraphics(), screen, event.getMouseX(), event.getMouseY());
    }

    @SubscribeEvent
    public static void sourceHighlights(ContainerScreenEvent.Render.Foreground event) {
        if (InventoryContainerPanel.supports(event.getContainerScreen())) InventoryContainerPanel.drawSourceHighlights(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void tooltip(RenderTooltipEvent.Pre event) {
        var screen = Minecraft.getInstance().screen;
        Slot slot = screen instanceof AbstractContainerScreen<?> container ? container.getSlotUnderMouse() : null;
        boolean nativeSlotTooltip = slot != null && slot.hasItem() && !event.getItemStack().isEmpty()
                && net.minecraft.world.item.ItemStack.isSameItemSameTags(slot.getItem(), event.getItemStack());
        if (!InventoryContainerPanel.drawingTooltip() && InventoryContainerPanel.visible()
                && (nativeSlotTooltip || InventoryContainerPanel.contains(event.getX(), event.getY())))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void click(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen) || !InventoryContainerPanel.supports(screen)) return;
        InventoryContainerPanel.position(screen);
        double x = event.getMouseX(), y = event.getMouseY();
        int button = event.getButton();
        if (InventoryContainerPanel.contains(x, y)) screen.setFocused(null);
        if (InventoryContainerPanel.pressHeader(x, y, button)) {
            suppressedRelease = button;
            event.setCanceled(true);
            return;
        }
        if (InventoryContainerPanel.contains(x, y)) {
            if (InventoryContainerPanel.slotAt(x, y) == null) {
                suppressedRelease = button;
                event.setCanceled(true);
                return;
            }
            clickSlots(screen, screen.children(), x, y, button);
            event.setCanceled(true);
            return;
        }
        if (button != 1 || !screen.getMenu().getCarried().isEmpty()) return;
        if (screen.width < 379 && screen instanceof RecipeUpdateListener recipe && recipe.getRecipeBookComponent().isVisible()) return;
        int count = InventoryContainerPanel.hostSlotCount();
        for (int index = 0; index < count; index++) {
            Slot slot = screen.getMenu().slots.get(index);
            int slotX = screen.getGuiLeft() + slot.x, slotY = screen.getGuiTop() + slot.y;
            if (x >= slotX - 1 && x < slotX + 17 && y >= slotY - 1 && y < slotY + 17
                    && InventoryItemContainer.canOpen(slot, Minecraft.getInstance().player)) {
                InventoryContainerPanel.requestOpen(index);
                suppressedRelease = button;
                event.setCanceled(true);
                return;
            }
        }
    }

    private static <T extends GuiEventListener> void clickSlots(AbstractContainerScreen<?> screen, List<T> children, double x, double y, int button) {
        var saved = new ArrayList<>(children);
        children.clear();
        try {
            screen.mouseClicked(x, y, button);
        } finally {
            children.addAll(saved);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void drag(ScreenEvent.MouseDragged.Pre event) {
        if (InventoryContainerPanel.supports(event.getScreen()) && event.getMouseButton() == 0
                && InventoryContainerPanel.drag(event.getMouseX(), event.getMouseY())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void release(ScreenEvent.MouseButtonReleased.Pre event) {
        if (!InventoryContainerPanel.supports(event.getScreen())) return;
        boolean dragging = InventoryContainerPanel.release();
        if (dragging || suppressedRelease == event.getButton()) {
            suppressedRelease = -1;
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void key(ScreenEvent.KeyPressed.Pre event) {
        if (InventoryContainerPanel.supports(event.getScreen()) && InventoryContainerPanel.visible()
                && event.getKeyCode() == GLFW.GLFW_KEY_ESCAPE) {
            InventoryContainerPanel.close();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void scroll(ScreenEvent.MouseScrolled.Pre event) {
        if (InventoryContainerPanel.supports(event.getScreen()) && InventoryContainerPanel.contains(event.getMouseX(), event.getMouseY()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void close(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?>) {
            InventoryContainerPanel.reset();
            suppressedRelease = -1;
        }
    }
}
