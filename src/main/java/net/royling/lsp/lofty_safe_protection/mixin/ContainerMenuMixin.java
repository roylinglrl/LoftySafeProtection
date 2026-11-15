package net.royling.lsp.lofty_safe_protection.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.ArrayList;
import net.royling.lsp.lofty_safe_protection.menu.InventoryContainerMenuAccess;
import net.royling.lsp.lofty_safe_protection.menu.InventoryContainerSlots;
import net.royling.lsp.lofty_safe_protection.menu.InventoryContainerTransfers;
import net.royling.lsp.lofty_safe_protection.menu.InventoryItemContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractContainerMenu.class, remap = false)
public abstract class ContainerMenuMixin implements InventoryContainerMenuAccess {
    @Unique private final List<InventoryItemContainer> lsp$inventoryContainers = new ArrayList<>();
    @Unique private final List<Integer> lsp$focusOrder = new ArrayList<>();
    @Shadow protected abstract Slot addSlot(Slot slot);
    @Shadow protected abstract DataSlot addDataSlot(DataSlot slot);

    @Override
    public List<InventoryItemContainer> lsp$inventoryContainers() { return List.copyOf(lsp$inventoryContainers); }

    @Override
    public InventoryItemContainer lsp$focusedInventoryContainer() {
        for (int index = lsp$focusOrder.size() - 1; index >= 0; index--)
            for (var storage : lsp$inventoryContainers) if (storage.isSource(lsp$focusOrder.get(index))) return storage;
        for (int i = lsp$inventoryContainers.size() - 1; i >= 0; i--)
            if (lsp$inventoryContainers.get(i).isOpen()) return lsp$inventoryContainers.get(i);
        return null;
    }

    @Override
    public void lsp$focusInventoryContainer(int sourceSlot) {
        lsp$focusOrder.remove(Integer.valueOf(sourceSlot));
        lsp$focusOrder.add(sourceSlot);
    }

    @Override
    public void lsp$attachInventoryContainer(Player player) {
        if (lsp$inventoryContainers.isEmpty()) lsp$addInventoryContainer(player);
    }

    @Override
    public InventoryItemContainer lsp$addInventoryContainer(Player player) {
        var menu = (AbstractContainerMenu) (Object) this;
        if (menu.slots.size() + 54 > 32000 || lsp$inventoryContainers.size() >= lsp$hostSlotCount()) return null;
        var storage = new InventoryItemContainer(player, menu, menu.slots.size());
        lsp$inventoryContainers.add(storage);
        for (Slot slot : InventoryContainerSlots.slots(storage)) addSlot(slot);
        for (DataSlot slot : InventoryContainerSlots.data(storage)) addDataSlot(slot);
        return storage;
    }

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void lsp$validate(CallbackInfo callback) {
        lsp$inventoryContainers.forEach(InventoryItemContainer::validateBinding);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void lsp$close(Player player, CallbackInfo callback) {
        lsp$closeInventoryContainers();
    }

    @Inject(method = "moveItemStackTo", at = @At("HEAD"), cancellable = true)
    private void lsp$limitNativeTransfer(ItemStack stack, int start, int end, boolean reverse, CallbackInfoReturnable<Boolean> callback) {
        var storage = lsp$inventoryContainer();
        if (storage != null && end > storage.firstSlot())
            callback.setReturnValue(InventoryContainerTransfers.moveWithinHost(
                    (AbstractContainerMenu) (Object) this, storage, stack, start, end, reverse));
    }

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void lsp$click(int slotId, int button, ContainerInput type, Player player, CallbackInfo callback) {
        InventoryItemContainer clicked = null;
        for (var storage : lsp$inventoryContainers) {
            storage.validateBinding();
            if (storage.ownsSlot(slotId)) {
                if (!storage.activeCell(slotId - storage.firstSlot())) { callback.cancel(); return; }
                clicked = storage;
            }
            if (!storage.isOpen()) continue;
            Slot source = storage.source();
            if (storage.isSource(slotId) || type == ContainerInput.SWAP
                    && source.container == player.getInventory() && source.getContainerSlot() == button) {
                callback.cancel();
                return;
            }
        }
        var target = clicked == null ? lsp$focusedInventoryContainer() : clicked;
        if (target != null && type == ContainerInput.QUICK_MOVE && InventoryContainerTransfers.quickMove(
                (AbstractContainerMenu) (Object) this, target, player, slotId)) callback.cancel();
    }
}
