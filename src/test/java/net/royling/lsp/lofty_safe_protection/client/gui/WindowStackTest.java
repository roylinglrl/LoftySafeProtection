package net.royling.lsp.lofty_safe_protection.client.gui;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WindowStackTest {
    @Test
    void onlyTheTopOverlappingWindowReceivesTheHitAndRaisingDoesNotDuplicateIt() {
        var order = new WindowStack<String>();
        var bounds = Map.of("a", InventoryContainerPanelLayout.place(10, 10, 4, 4, 400, 300),
                "b", InventoryContainerPanelLayout.place(20, 20, 4, 4, 400, 300));
        order.raise("a"); order.raise("b");
        assertEquals("b", order.top(window -> bounds.get(window).contains(30, 30)));
        order.raise("a");
        assertEquals("a", order.top(window -> bounds.get(window).contains(30, 30)));
        assertEquals(2, order.backToFront().size());
        assertEquals("b", order.top(window -> Set.of("b").contains(window)));
    }

    @Test
    void movingAndClosingOneStateDoesNotAlterTheOther() {
        var first = new InventoryContainerPanelState();
        var second = new InventoryContainerPanelState();
        first.update(10, 40, 40, 4, 4, 400, 300, 0);
        second.update(11, 60, 60, 5, 5, 400, 300, 0);
        first.update(10, 40, 40, 4, 4, 400, 300, 1_000_000_000);
        var original = second.bounds();
        first.move(200, 100, 400, 300);
        first.reset();
        assertEquals(original, second.bounds());
        assertEquals(11, second.sourceSlot());
    }
}
