package net.royling.lsp.lofty_safe_protection.client.gui;

public final class InventoryContainerPanelState {
    private final SafeBoxPanelAnimation animation = new SafeBoxPanelAnimation();
    private InventoryContainerPanelLayout.Bounds bounds;
    private int sourceSlot = -1;
    private int columns;
    private int rows;
    private int fluidSlots;
    private boolean moved;
    private float progress;

    public int sourceSlot() { return sourceSlot; }
    public InventoryContainerPanelLayout.Bounds bounds() { return bounds; }
    public float progress() { return progress; }
    public boolean interactive() { return bounds != null && progress >= 1; }

    public InventoryContainerPanelLayout.Bounds frame() {
        return bounds == null ? null : InventoryContainerPanelLayout.frame(bounds, progress);
    }

    public void update(int slot, int slotX, int slotY, int columns, int rows, int screenWidth, int screenHeight, long now) {
        update(slot, slotX, slotY, columns, rows, 0, screenWidth, screenHeight, now);
    }

    public void update(int slot, int slotX, int slotY, int columns, int rows, int fluidSlots, int screenWidth, int screenHeight, long now) {
        if (slot < 0 || ((columns < 1 || rows < 1) && fluidSlots < 1)) {
            reset();
            return;
        }
        if (sourceSlot != slot || bounds == null) {
            sourceSlot = slot;
            moved = false;
            animation.reset();
            animation.setOpen(true, now);
        }
        this.columns = columns;
        this.rows = rows;
        this.fluidSlots = fluidSlots;
        bounds = moved ? InventoryContainerPanelLayout.place(bounds.x(), bounds.y(), columns, rows, fluidSlots, screenWidth, screenHeight)
                : InventoryContainerPanelLayout.besideSlot(slotX, slotY, columns, rows, fluidSlots, screenWidth, screenHeight);
        progress = animation.value(now);
    }

    public void move(int x, int y, int screenWidth, int screenHeight) {
        if (!interactive()) return;
        bounds = InventoryContainerPanelLayout.place(x, y, columns, rows, fluidSlots, screenWidth, screenHeight);
        moved = true;
    }

    public void reset() {
        sourceSlot = -1;
        columns = 0;
        rows = 0;
        fluidSlots = 0;
        bounds = null;
        moved = false;
        progress = 0;
        animation.reset();
    }
}
