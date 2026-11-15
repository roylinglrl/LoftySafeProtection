package net.royling.lsp.lofty_safe_protection.client.gui;

import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.experience.ExperienceMath;
import net.royling.lsp.lofty_safe_protection.experience.SafeBoxExperienceService;
import net.royling.lsp.lofty_safe_protection.menu.SafeBoxContainer;
import net.royling.lsp.lofty_safe_protection.network.SafeBoxPayloads;


import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;

public final class SafeBoxExperienceControls {
    private static final Identifier BAR = SafeBoxManager.id("textures/gui/experience_bar.png");
    private static final int TEXTURE_WIDTH = 33, TEXTURE_HEIGHT = 11, BAR_HEIGHT = 5;
    private static final int LEFT_WIDTH = 11, MIDDLE_U = 12, MIDDLE_WIDTH = 10, RIGHT_WIDTH = 10;
    private static final int PROGRESS_V = 6;
    public static final int BUTTON = 14;

    public static Button button(boolean deposit, Consumer<SafeBoxPayloads.ExperienceTransferPayload> send) {
        return new Button.Plain(0, 0, BUTTON, BUTTON, Component.literal(deposit ? "+" : "−"), button -> {
            var box = SafeBoxPanel.storage();
            if (box == null || !box.expanded || !box.interactive) return;
            var window = Minecraft.getInstance().getWindow();
            boolean shift = down(window, InputConstants.KEY_LSHIFT, InputConstants.KEY_RSHIFT);
            boolean control = down(window, InputConstants.KEY_LCONTROL, InputConstants.KEY_RCONTROL);
            send.accept(new SafeBoxPayloads.ExperienceTransferPayload(deposit, (shift ? 1 : 0) | (control ? 2 : 0)));
        }, supplier -> Component.translatable(deposit ? "gui.lofty_safe_protection.deposit_experience" : "gui.lofty_safe_protection.withdraw_experience")) {};
    }

    private static boolean down(com.mojang.blaze3d.platform.Window window, int left, int right) {
        return InputConstants.isKeyDown(left) || InputConstants.isKeyDown(right);
    }

    public static void position(Button withdraw, Button deposit, int left, int top) {
        var box = SafeBoxPanel.storage();
        boolean visible = box != null && box.expanded && box.interactive && SafeBoxExperienceService.visible(box.state());
        withdraw.visible = deposit.visible = visible;
        withdraw.active = deposit.active = false;
        if (!visible) return;
        var state = box.state();
        var frame = SafeBoxPanelLayout.frame(left, top, state.columns, state.rows, true, 1);
        withdraw.setPosition(frame.x() + 4, frame.y() + 16);
        deposit.setPosition(frame.x() + frame.width() - 4 - BUTTON, frame.y() + 16);
        var player = Minecraft.getInstance().player;
        long points = ExperienceMath.balance(player.experienceLevel, player.experienceProgress);
        withdraw.active = ExperienceMath.transfer(false, 0, points, state.experience(), SafeBoxExperienceService.capacity(state)) != 0;
        deposit.active = ExperienceMath.transfer(true, 0, points, state.experience(), SafeBoxExperienceService.capacity(state)) != 0;
    }

    public static void draw(GuiGraphicsExtractor graphics, SafeBoxContainer box, int left, int top, float progress) {
        if (!SafeBoxExperienceService.visible(box.state())) return;
        var state = box.state();
        int width = SafeBoxPanelLayout.width(state.columns, true), height = SafeBoxPanelLayout.height(state.rows, true);
        int capacity = SafeBoxExperienceService.capacity(state);
        int barX = 21, barY = -height + 21, barWidth = width - 42;
        int filled = capacity <= 0 ? (state.experience() > 0 ? barWidth : 0)
                : (int) Math.min(barWidth, (long) state.experience() * barWidth / capacity);
        graphics.pose().pushMatrix();
        graphics.pose().translate(left + SafeBoxPanelLayout.OFFSET_X, top + SafeBoxPanelLayout.BOTTOM);
        graphics.pose().scale(progress, progress);


        drawBarLayer(graphics, barX, barY, barWidth, barWidth, 0);
        if (filled > 0) drawBarLayer(graphics, barX, barY, barWidth, filled, PROGRESS_V);

        var font = Minecraft.getInstance().font;
        String value = state.experience() + "/" + capacity;
        float textScale = Math.min(1, (width - 24f) / Math.max(1, font.width(value)));
        graphics.pose().translate((width - 10f) / 2, -height + 5);
        graphics.pose().scale(textScale, textScale);
        graphics.text(font, value, -font.width(value) / 2, 0, 0xff80ff20, true);
        graphics.pose().popMatrix();
    }

    private static void drawBarLayer(GuiGraphicsExtractor graphics, int x, int y, int width, int visibleWidth, int v) {
        int leftWidth = Math.min(LEFT_WIDTH, (width + 1) / 2);
        int rightWidth = Math.min(RIGHT_WIDTH, width - leftWidth);
        int middleEnd = width - rightWidth;
        drawBarPart(graphics, x, y, 0, leftWidth, 0, v, visibleWidth);
        for (int offset = leftWidth; offset < middleEnd; offset += MIDDLE_WIDTH) {
            drawBarPart(graphics, x, y, offset, Math.min(MIDDLE_WIDTH, middleEnd - offset), MIDDLE_U, v, visibleWidth);
        }
        drawBarPart(graphics, x, y, middleEnd, rightWidth, TEXTURE_WIDTH - rightWidth, v, visibleWidth);
    }

    private static void drawBarPart(GuiGraphicsExtractor graphics, int x, int y, int offset, int width, int u, int v, int visibleWidth) {
        int drawnWidth = Math.min(width, visibleWidth - offset);
        if (drawnWidth > 0) graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,BAR, x + offset, y, u, v, drawnWidth, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
