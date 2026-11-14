package net.royling.lsp.lofty_safe_protection.client.gui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryContainerPanelStateTest {
    private static final long START = 1_000_000_000L;

    @Test
    void opensBesideTheItemAndExpandsOnlyRightAndDown() {
        var state = new InventoryContainerPanelState();
        state.update(12, 100, 60, 4, 4, 640, 360, START);
        var target = state.bounds();
        assertEquals(118, target.x());
        assertEquals(60, target.y());
        assertEquals(8, state.frame().width());
        assertEquals(8, state.frame().height());
        assertFalse(state.interactive());
        int previousWidth = 8, previousHeight = 8;
        for (int step = 1; step <= 10; step++) {
            state.update(12, 100, 60, 4, 4, 640, 360, START + SafeBoxPanelAnimation.DURATION * step / 10);
            var frame = state.frame();
            assertEquals(target.x(), frame.x());
            assertEquals(target.y(), frame.y());
            assertTrue(frame.width() >= previousWidth && frame.width() <= target.width());
            assertTrue(frame.height() >= previousHeight && frame.height() <= target.height());
            if (step < 10) assertFalse(state.interactive());
            previousWidth = frame.width();
            previousHeight = frame.height();
        }
        assertEquals(target, state.frame());
        assertTrue(state.interactive());
    }

    @Test
    void closingAndReopeningTheSameItemDiscardsItsDraggedPosition() {
        var state = new InventoryContainerPanelState();
        state.update(0, 100, 60, 4, 4, 640, 360, START);
        var defaultBounds = state.bounds();
        state.update(0, 100, 60, 4, 4, 640, 360, START + SafeBoxPanelAnimation.DURATION);
        state.move(330, 180, 640, 360);
        state.update(0, 100, 60, 4, 4, 640, 360, START + SafeBoxPanelAnimation.DURATION + 1);
        assertEquals(330, state.bounds().x());
        assertEquals(180, state.bounds().y());
        state.reset();
        assertNull(state.bounds());
        state.update(0, 100, 60, 4, 4, 640, 360, START + SafeBoxPanelAnimation.DURATION * 2);
        assertEquals(defaultBounds, state.bounds());
        assertEquals(0, state.progress());
        assertFalse(state.interactive());
    }

    @Test
    void switchingItemsRestartsAtTheNewSlotAndClosedStateForgetsThePosition() {
        var state = new InventoryContainerPanelState();
        state.update(12, 100, 60, 4, 4, 640, 360, START);
        state.update(12, 110, 70, 4, 4, 640, 360, START + SafeBoxPanelAnimation.DURATION);
        assertEquals(128, state.bounds().x());
        assertEquals(70, state.bounds().y());
        state.move(330, 180, 640, 360);
        state.update(20, 220, 96, 4, 4, 640, 360, START + SafeBoxPanelAnimation.DURATION * 2);
        assertEquals(238, state.bounds().x());
        assertEquals(96, state.bounds().y());
        assertFalse(state.interactive());
        state.move(400, 200, 640, 360);
        assertEquals(238, state.bounds().x());
        state.update(-1, 0, 0, 0, 0, 640, 360, START + SafeBoxPanelAnimation.DURATION * 3);
        assertNull(state.bounds());
        assertNull(state.frame());
    }
}
