package net.royling.lsp.lofty_safe_protection.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.menu.ContainerActions;
import net.royling.lsp.lofty_safe_protection.menu.InventoryContainerMenuAccess;
import net.royling.lsp.lofty_safe_protection.menu.InventoryItemContainer;
import net.royling.lsp.lofty_safe_protection.network.InventoryContainerPayloads;

public final class InventoryContainerPanel {
    private static final Identifier SLOT = SafeBoxManager.id("textures/gui/slot.png");
    private static final Map<Integer, Window> WINDOWS = new HashMap<>();
    private static final Map<Integer, Integer> PENDING = new HashMap<>();
    private static final WindowStack<Window> ORDER = new WindowStack<>();
    private static AbstractContainerScreen<?> owner;
    private static Window dragging;
    private static double dragOffsetX;
    private static double dragOffsetY;
    private static boolean drawingTooltip;
    private static boolean renderingSlots;

    private static final class Window {
        private final int index;
        private final InventoryItemContainer storage;
        private final InventoryContainerPanelState state = new InventoryContainerPanelState();
        private Window(int index, InventoryItemContainer storage) { this.index = index; this.storage = storage; }
    }

    private InventoryContainerPanel() {}

    public static boolean supports(Screen screen) {
        var player = Minecraft.getInstance().player;
        return screen instanceof AbstractContainerScreen<?> container && !(screen instanceof CreativeModeInventoryScreen)
                && player != null && container.getMenu() == player.containerMenu
                && container.getMenu() instanceof InventoryContainerMenuAccess;
    }

    public static int hostSlotCount() {
        return owner != null && owner.getMenu() instanceof InventoryContainerMenuAccess access ? access.lsp$hostSlotCount() : 0;
    }

    private static boolean visible(Window window) {
        return owner != null && Minecraft.getInstance().gui.screen() == owner && window.storage.isOpen()
                && window.state.bounds() != null && window.storage.columns() > 0 && window.storage.rows() > 0;
    }

    public static boolean visible() { return ORDER.top(InventoryContainerPanel::visible) != null; }

    public static void position(AbstractContainerScreen<?> screen) {
        if (owner != screen) {
            owner = screen;
            WINDOWS.clear();
            PENDING.clear();
            ORDER.clear();
            dragging = null;
        }
        if (!(screen.getMenu() instanceof InventoryContainerMenuAccess access)) return;
        var storages = access.lsp$inventoryContainers();
        for (int index = 0; index < storages.size(); index++) {
            Window window = WINDOWS.get(index);
            if (window == null) {
                window = new Window(index, storages.get(index));
                WINDOWS.put(index, window);
                ORDER.raise(window);
            }
            var storage = window.storage;
            if (!storage.isOpen() || storage.columns() < 1 || storage.rows() < 1) {
                window.state.reset();
                if (dragging == window) dragging = null;
                continue;
            }
            Slot source = storage.source();
            window.state.update(storage.sourceSlot(), screen.getLeftPos() + source.x, screen.getTopPos() + source.y,
                    storage.columns(), storage.rows(), screen.width, screen.height, System.nanoTime());
            var bounds = window.state.bounds();
            for (int cell = 0; cell < 54; cell++) {
                Slot slot = screen.getMenu().slots.get(storage.firstSlot() + cell);
                ((SlotPositionAccess) slot).lsp$position(bounds.slotX(cell) - screen.getLeftPos(), bounds.slotY(cell) - screen.getTopPos());
            }
        }
    }

    private static Window at(double x, double y) {
        return ORDER.top(window -> visible(window) && window.state.frame().contains(x, y));
    }

    public static boolean contains(double x, double y) { return at(x, y) != null; }
    public static boolean drawingTooltip() { return drawingTooltip; }
    public static boolean renderingSlots() { return renderingSlots; }

    public static Slot slotAt(double x, double y) {
        Window window = at(x, y);
        if (window == null || !window.state.interactive()) return null;
        var bounds = window.state.bounds();
        for (int cell = 0; cell < 54; cell++) if (window.storage.activeCell(cell)) {
            int slotX = bounds.slotX(cell), slotY = bounds.slotY(cell);
            if (x >= slotX - 1 && x < slotX + 17 && y >= slotY - 1 && y < slotY + 17)
                return owner.getMenu().slots.get(window.storage.firstSlot() + cell);
        }
        return null;
    }

    public static List<Rect2i> occupiedAreas(AbstractContainerScreen<?> screen) {
        position(screen);
        return ORDER.backToFront().stream().filter(InventoryContainerPanel::visible).map(window -> {
            var bounds = window.state.bounds();
            return new Rect2i(bounds.x(), bounds.y(), bounds.width(), bounds.height());
        }).toList();
    }

    private static void focus(Window window) {
        if (ORDER.top(InventoryContainerPanel::visible) == window) return;
        ORDER.raise(window);
        if (window.storage.isOpen()) action(window, ContainerActions.FOCUS);
    }

    public static boolean pressHeader(double x, double y, int button) {
        Window window = at(x, y);
        if (window == null) return false;
        focus(window);
        if (!window.state.interactive()) return false;
        var bounds = window.state.bounds();
        if (!bounds.titleContains(x, y)) return false;
        if (button == 0) {
            if (bounds.closeContains(x, y)) close(window);
            else if (bounds.actionContains(ContainerActions.SORT, x, y)) action(window, ContainerActions.SORT);
            else if (bounds.actionContains(ContainerActions.DEPOSIT_MATCHING, x, y)) action(window, ContainerActions.DEPOSIT_MATCHING);
            else {
                dragging = window;
                dragOffsetX = x - bounds.x();
                dragOffsetY = y - bounds.y();
            }
        }
        return true;
    }

    public static boolean drag(double x, double y) {
        if (dragging == null || !visible(dragging)) return false;
        dragging.state.move((int) Math.round(x - dragOffsetX), (int) Math.round(y - dragOffsetY), owner.width, owner.height);
        position(owner);
        return true;
    }

    public static boolean release() {
        boolean result = dragging != null;
        dragging = null;
        return result;
    }

    public static void requestOpen(int slotId) {
        if (owner == null || !(owner.getMenu() instanceof InventoryContainerMenuAccess access)) return;
        for (Window window : ORDER.backToFront()) if (window.storage.isSource(slotId)) { close(window); return; }
        for (var request : List.copyOf(PENDING.entrySet())) if (request.getValue() == slotId) {
            close(WINDOWS.get(request.getKey()));
            return;
        }
        var storages = access.lsp$inventoryContainers();
        int index = 0;
        while (index < storages.size() && (storages.get(index).isOpen() || PENDING.containsKey(index))) index++;
        if (index == storages.size() && access.lsp$addInventoryContainer(Minecraft.getInstance().player) == null) return;
        position(owner);
        Window window = WINDOWS.get(index);
        window.state.reset();
        ORDER.raise(window);
        PENDING.put(index, slotId);
        net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new InventoryContainerPayloads.SetOpenPayload(owner.getMenu().containerId, index, slotId));
    }

    public static void acknowledge(int menuId, int windowId, int sourceSlot) {
        if (owner == null || owner.getMenu().containerId != menuId) return;
        boolean requested = sourceSlot >= 0 && PENDING.remove(windowId, sourceSlot);
        Window window = WINDOWS.get(windowId);
        if (requested && window != null && window.storage.isSource(sourceSlot)) ORDER.raise(window);
    }

    private static void action(Window window, int action) {
        if (owner != null && window.storage.isOpen() && (action == ContainerActions.FOCUS || owner.getMenu().getCarried().isEmpty()))
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new InventoryContainerPayloads.ContainerActionPayload(owner.getMenu().containerId, window.storage.sourceSlot(), action));
    }

    private static void close(Window window) {
        if (window == null) return;
        window.storage.close();
        window.state.reset();
        PENDING.remove(window.index);
        if (dragging == window) dragging = null;
        if (owner != null && Minecraft.getInstance().getConnection() != null)
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new InventoryContainerPayloads.SetOpenPayload(owner.getMenu().containerId, window.index, -1));
        Window top = ORDER.top(InventoryContainerPanel::visible);
        if (top != null) action(top, ContainerActions.FOCUS);
    }

    public static void close() { close(ORDER.top(InventoryContainerPanel::visible)); }

    public static void reset() {
        for (Window window : ORDER.backToFront()) window.storage.close();
        if (owner != null && Minecraft.getInstance().getConnection() != null)
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new InventoryContainerPayloads.SetOpenPayload(owner.getMenu().containerId, -1, -1));
        WINDOWS.clear();
        PENDING.clear();
        ORDER.clear();
        dragging = null;
        owner = null;
    }

    public static void drawSourceHighlights(GuiGraphicsExtractor graphics) {
        for (Window window : ORDER.backToFront()) if (visible(window)) {
            Slot source = window.storage.source();
            ContainerActionIcons.outline(graphics, source.x, source.y, 0xffe8be57);
        }

    }

    public static void draw(GuiGraphicsExtractor graphics, AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        if (!visible()) return;
        Slot hovered = slotAt(mouseX, mouseY);
        var windows = ORDER.backToFront();
        int layer = 400;
        for (Window window : windows) if (visible(window)) drawWindow(graphics, screen, window, hovered, mouseX, mouseY, layer += 20);
        var access = (ContainerScreenAccess) screen;
        if (contains(mouseX, mouseY)) access.lsp$setHoveredSlot(hovered);
        Slot tooltipSlot = contains(mouseX, mouseY) ? hovered : screen.getHoveredSlot();
        graphics.nextStratum();

        access.lsp$renderOverlayCarried(graphics, mouseX, mouseY);
        graphics.pose().pushMatrix();
        graphics.pose().translate(0, 0);
        graphics.nextStratum();
        drawingTooltip = true;
        try {
            if (screen.getMenu().getCarried().isEmpty()) {
                Window window = at(mouseX, mouseY);
                boolean actionTooltip = false;
                if (window != null && window.state.interactive()) for (int action = 0; action <= 1; action++) {
                    if (window.state.bounds().actionContains(action, mouseX, mouseY)) {
                        net.royling.lsp.lofty_safe_protection.client.gui.GuiTooltips.draw(graphics, Minecraft.getInstance().font, Component.translatable(action == ContainerActions.SORT
                                ? "gui.lofty_safe_protection.sort" : "gui.lofty_safe_protection.deposit_matching"), mouseX, mouseY);
                        actionTooltip = true;
                    }
                }
                if (!actionTooltip && tooltipSlot != null && tooltipSlot.hasItem()) GuiTooltips.drawItem(graphics, tooltipSlot.getItem(), mouseX, mouseY);
            }
        } finally { drawingTooltip = false;  graphics.pose().popMatrix(); }

    }

    private static void drawWindow(GuiGraphicsExtractor graphics, AbstractContainerScreen<?> screen, Window window, Slot hovered,
                                   int mouseX, int mouseY, int layer) {
        graphics.nextStratum();
        int left = screen.getLeftPos(), top = screen.getTopPos();
        var font = Minecraft.getInstance().font;
        var container = window.storage;
        var state = window.state;
        var bounds = state.bounds();
        var frame = state.frame();
        float progress = state.progress();

        graphics.pose().pushMatrix();
        graphics.pose().translate(left, top);

        SafeBoxPanel.background(graphics, frame.x() - left, frame.y() - top, frame.width(), frame.height());
        if (progress > 0) {
            graphics.enableScissor(frame.x() + 4, frame.y() + 4, frame.x() + frame.width() - 4, frame.y() + frame.height() - 4);
            graphics.pose().pushMatrix();
            graphics.pose().translate(bounds.x() - left, bounds.y() - top);
            graphics.pose().scale(progress, progress);
            graphics.pose().translate(left - bounds.x(), top - bounds.y());
            renderingSlots = true;
            try {
                String title = font.plainSubstrByWidth(container.sourceStack().getHoverName().getString(), bounds.width() - 49);
                graphics.text(font, title, bounds.x() - left + 5, bounds.y() - top + 6, 0xff404040, false);
                int closeX = bounds.x() + bounds.width() - 14 - left, closeY = bounds.y() + 4 - top;
                int color = state.interactive() && bounds.closeContains(mouseX, mouseY) && at(mouseX, mouseY) == window ? 0xffaa3030 : 0xff404040;
                for (int i = 0; i < 6; i++) {
                    graphics.fill(closeX + 2 + i, closeY + 2 + i, closeX + 3 + i, closeY + 3 + i, color);
                    graphics.fill(closeX + 7 - i, closeY + 2 + i, closeX + 8 - i, closeY + 3 + i, color);
                }
                for (int action = 0; action <= 1; action++)
                    ContainerActionIcons.draw(graphics, bounds.actionX(action) - left, bounds.y() + 4 - top, action,
                            state.interactive() && at(mouseX, mouseY) == window && bounds.actionContains(action, mouseX, mouseY));

                for (int cell = 0; cell < 54; cell++) if (container.activeCell(cell)) {
                    Slot slot = screen.getMenu().slots.get(container.firstSlot() + cell);

                    graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,SLOT, slot.x - 1, slot.y - 1, 0, 0, 18, 18, 18, 18);
                    ((ContainerScreenAccess) screen).lsp$renderOverlaySlot(graphics, slot, mouseX, mouseY);
                    if (slot == hovered) graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x80ffffff);
                    if (slot == hovered && !screen.getMenu().getCarried().isEmpty() && !slot.mayPlace(screen.getMenu().getCarried()))
                        ContainerActionIcons.outline(graphics, slot.x, slot.y, 0xffe34c4c);
                }

            } finally { renderingSlots = false; graphics.pose().popMatrix(); graphics.disableScissor(); }
        }

        graphics.pose().popMatrix();
    }
}
