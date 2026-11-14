package net.royling.lsp.lofty_safe_protection.client.gui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryContainerPanelLayoutTest {
    @Test
    void everySupportedSizeKeepsSlotsAndCloseButtonWithinThePanel() {
        for (int columns = 1; columns <= 9; columns++) for (int rows = 1; rows <= 6; rows++) {
            var bounds = InventoryContainerPanelLayout.place(25, 40, columns, rows, 320, 240);
            for (int row = 0; row < rows; row++) for (int column = 0; column < columns; column++) {
                int cell = row * 9 + column;
                assertTrue(bounds.contains(bounds.slotX(cell) - 1, bounds.slotY(cell) - 1));
                assertTrue(bounds.contains(bounds.slotX(cell) + 16, bounds.slotY(cell) + 16));
                assertFalse(bounds.titleContains(bounds.slotX(cell), bounds.slotY(cell)));
            }
            assertTrue(bounds.titleContains(bounds.x() + bounds.width() - 9, bounds.y() + 9));
            assertTrue(bounds.closeContains(bounds.x() + bounds.width() - 9, bounds.y() + 9));
            assertFalse(bounds.closeContains(bounds.x() + 5, bounds.y() + 9));
            for (int action = 0; action <= 1; action++) {
                int actionX = bounds.actionX(action);
                assertTrue(bounds.titleContains(actionX + 5, bounds.y() + 9));
                assertTrue(bounds.actionContains(action, actionX + 5, bounds.y() + 9));
                assertFalse(bounds.closeContains(actionX + 5, bounds.y() + 9));
                assertFalse(bounds.actionContains(1 - action, actionX + 5, bounds.y() + 9));
            }
        }
    }

    @Test
    void draggingAndResizingKeepTheWholePanelOnScreen() {
        var topLeft = InventoryContainerPanelLayout.place(-900, -400, 4, 4, 320, 240);
        assertEquals(0, topLeft.x());
        assertEquals(0, topLeft.y());
        var bottomRight = InventoryContainerPanelLayout.place(900, 800, 4, 4, 320, 240);
        assertEquals(320, bottomRight.x() + bottomRight.width());
        assertEquals(240, bottomRight.y() + bottomRight.height());
        var moved = InventoryContainerPanelLayout.place(80, 70, 4, 4, 320, 240);
        assertEquals(80, moved.slotX(0) - topLeft.slotX(0));
        assertEquals(70, moved.slotY(30) - topLeft.slotY(30));
        assertFalse(moved.contains(moved.x() + moved.width(), moved.y()));
    }
}
