package net.royling.lsp.lofty_safe_protection.menu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.royling.lsp.lofty_safe_protection.storage.legacy.ItemContainerContents;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerDefinition;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerManager;
import net.royling.lsp.lofty_safe_protection.storage.InventoryContainerBinding;

public final class InventoryItemContainer extends SimpleContainer {
    private final Player owner;
    private final AbstractContainerMenu menu;
    private final int firstSlot;
    private InventoryContainerBinding binding;
    private int sourceSlot = -1;
    private int columns;
    private int rows;
    private boolean loading;
    private java.util.List<ItemStack> getItems() { return java.util.stream.IntStream.range(0, 54).mapToObj(this::getItem).toList(); }

    public InventoryItemContainer(Player owner, AbstractContainerMenu menu, int firstSlot) {
        super(54);
        this.owner = owner;
        this.menu = menu;
        this.firstSlot = firstSlot;
    }

    public int firstSlot() { return firstSlot; }
    public int endSlot() { return firstSlot + 54; }
    public int sourceSlot() { return sourceSlot; }
    public int columns() { return columns; }
    public int rows() { return rows; }
    public void syncSourceSlot(int value) { sourceSlot = value; }
    public void syncColumns(int value) { columns = value; }
    public void syncRows(int value) { rows = value; }
    public boolean isOpen() { return sourceSlot >= 0 && sourceSlot < firstSlot; }
    public boolean isSource(int slot) { return isOpen() && sourceSlot == slot; }
    public boolean ownsSlot(int slot) { return slot >= firstSlot && slot < endSlot(); }
    public Slot source() { return isOpen() ? menu.slots.get(sourceSlot) : null; }
    public ItemStack sourceStack() { return isOpen() ? source().getItem() : ItemStack.EMPTY; }
    public InventoryContainerDefinition definition() { return InventoryContainerManager.definition(sourceStack()); }

    public boolean activeCell(int cell) {
        return isOpen() && cell >= 0 && cell < 54 && cell % 9 < columns && cell / 9 < rows;
    }

    public boolean canInsert(ItemStack stack) {
        var definition = definition();
        return isOpen() && definition != null && definition.accepts(stack);
    }

    public void open(int slot) {
        if (!(owner instanceof ServerPlayer) || slot < 0 || slot >= firstSlot
                || !owner.isAlive() || owner.isSpectator() || owner.containerMenu != menu || !menu.stillValid(owner)
                || !menu.getCarried().isEmpty() || !canOpen(menu.slots.get(slot), owner)) return;
        ItemStack stack = menu.slots.get(slot).getItem();
        var definition = InventoryContainerManager.definition(stack);
        if (definition == null || stack.getCount() != 1) return;
        ItemContainerContents contents = ItemContainerContents.fromStack(stack);
        if (contents.getSlots() > 54) return;
        close();
        binding = new InventoryContainerBinding(menu.slots.get(slot));
        sourceSlot = slot;
        loading = true;
        try { for (int cell = 0; cell < 54; cell++) super.setItem(cell, contents.getStackInSlot(cell)); }
        finally { loading = false; }
        CompoundTag tag = stack.getOrCreateTag().copy();
        columns = isEmpty() ? definition.columns() : tag.getInt("ContainerColumns");
        rows = isEmpty() ? definition.rows() : tag.getInt("ContainerRows");
        if (columns < 1 || columns > 9) columns = definition.columns();
        if (rows < 1 || rows > 6) rows = definition.rows();
        for (int cell = 0; cell < 54; cell++) if (!getItem(cell).isEmpty()) {
            columns = Math.max(columns, cell % 9 + 1);
            rows = Math.max(rows, cell / 9 + 1);
        }
        setChanged();
    }

    public void validateBinding() {
        if (owner instanceof ServerPlayer && isOpen()
                && (!owner.isAlive() || owner.isSpectator() || owner.containerMenu != menu || !menu.stillValid(owner)
                    || !source().isActive() || binding == null || !binding.matches())) close();
    }

    public static boolean canOpen(Slot slot, Player player) {
        ItemStack stack = slot.getItem();
        return !(slot.container instanceof InventoryItemContainer) && slot.isActive()
                && stack.getCount() == 1 && InventoryContainerManager.definition(stack) != null
                && slot.mayPickup(player) && slot.mayPlace(stack);
    }

    public void close() {
        sourceSlot = -1;
        columns = 0;
        rows = 0;
        binding = null;
        clearContent();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (loading || !(owner instanceof ServerPlayer) || !isOpen()) return;
        if (binding == null || !binding.write(getItems(), columns, rows)) close();
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = super.removeItemNoUpdate(slot);
        if (!stack.isEmpty()) setChanged();
        return stack;
    }
}
