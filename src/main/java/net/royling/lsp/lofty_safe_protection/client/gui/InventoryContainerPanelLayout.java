package net.royling.lsp.lofty_safe_protection.client.gui;

public final class InventoryContainerPanelLayout {
    private InventoryContainerPanelLayout() {}

    public record Bounds(int x, int y, int width, int height) {
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
        public boolean titleContains(double mouseX, double mouseY) {
            return contains(mouseX, mouseY) && mouseY < y + 20;
        }
        public boolean closeContains(double mouseX, double mouseY) {
            return mouseX >= x + width - 14 && mouseX < x + width - 4 && mouseY >= y + 4 && mouseY < y + 14;
        }
        public int slotX(int cell) { return x + 5 + cell % 9 * 18; }
        public int actionX(int action) { return x + width - 38 + action * 12; }
        public boolean actionContains(int action, double mouseX, double mouseY) {
            return mouseX >= actionX(action) && mouseX < actionX(action) + 10 && mouseY >= y + 4 && mouseY < y + 14;
        }
        public int slotY(int cell) { return y + 21 + cell / 9 * 18; }
    }

    public static Bounds place(int x, int y, int columns, int rows, int screenWidth, int screenHeight) {
        int width = Math.max(80, columns * 18 + 8);
        int height = rows * 18 + 24;
        return new Bounds(Math.max(0, Math.min(x, screenWidth - width)),
                Math.max(0, Math.min(y, screenHeight - height)), width, height);
    }

    public static Bounds besideSlot(int slotX, int slotY, int columns, int rows, int screenWidth, int screenHeight) {
        return place(slotX + 18, slotY, columns, rows, screenWidth, screenHeight);
    }

    public static Bounds frame(Bounds target, float progress) {
        float value = Math.max(0, Math.min(1, progress));
        return new Bounds(target.x(), target.y(), 8 + Math.round((target.width() - 8) * value),
                8 + Math.round((target.height() - 8) * value));
    }
}
