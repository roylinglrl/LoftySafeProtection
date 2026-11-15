package net.royling.lsp.lofty_safe_protection.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerDefinition;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerManager;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.menu.InventoryContainerMenuAccess;
import net.royling.lsp.lofty_safe_protection.menu.InventoryItemContainer;
import net.royling.lsp.lofty_safe_protection.menu.ContainerActions;
import net.royling.lsp.lofty_safe_protection.menu.SafeBoxMenuAccess;
import net.royling.lsp.lofty_safe_protection.menu.SafeBoxSlots;
import net.royling.lsp.lofty_safe_protection.registry.SafeBoxCreativeTab;

public final class InventoryContainerNetworking {
    private InventoryContainerNetworking() {}

    public static InventoryContainerPayloads.SyncDefinitionsPayload definitionsPayload() {
        JsonArray json = new JsonArray();
        InventoryContainerManager.definitions.values().forEach(definition -> json.add(definition.toJson()));
        return new InventoryContainerPayloads.SyncDefinitionsPayload(json.toString());
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(InventoryContainerPayloads.ContainerActionPayload.TYPE, InventoryContainerPayloads.ContainerActionPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()) return;
                    var menu = player.containerMenu;
                    if (menu.containerId != payload.menuId() || !menu.stillValid(player)) return;
                    List<Slot> targets;
                    if (payload.sourceSlot() == -1) {
                        if (menu != player.inventoryMenu || !(menu instanceof SafeBoxMenuAccess access)
                                || !access.lsp$storage().expanded || SafeBoxManager.definition(access.lsp$storage().getItem(0)) == null) return;
                        targets = menu.slots.subList(SafeBoxSlots.FIRST + 1, SafeBoxSlots.END).stream().filter(Slot::isActive).toList();
                    } else {
                        if (!(menu instanceof InventoryContainerMenuAccess access)) return;
                        var storage = access.lsp$inventoryContainers().stream().filter(window -> window.isSource(payload.sourceSlot())).findFirst().orElse(null);
                        if (storage == null) return;
                        storage.validateBinding();
                        if (!storage.isOpen() || storage.sourceSlot() != payload.sourceSlot()) return;
                        access.lsp$focusInventoryContainer(payload.sourceSlot());
                        if (payload.action() == ContainerActions.FOCUS) return;
                        targets = menu.slots.subList(storage.firstSlot(), storage.endSlot()).stream().filter(Slot::isActive).toList();
                    }
                    if (!menu.getCarried().isEmpty()) return;
                    if (payload.action() == ContainerActions.SORT) ContainerActions.sort(targets);
                    else if (payload.action() == ContainerActions.DEPOSIT_MATCHING) ContainerActions.depositMatching(menu, player, targets, payload.sourceSlot());
                    else return;
                    menu.broadcastChanges();
                }));
        registrar.playToServer(InventoryContainerPayloads.SetOpenPayload.TYPE, InventoryContainerPayloads.SetOpenPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;
                    var menu = player.containerMenu;
                    if (menu.containerId != payload.menuId() || !(menu instanceof InventoryContainerMenuAccess access)) return;
                    handleWindow(player, access, payload);
                    menu.sendAllDataToRemote();
                    PacketDistributor.sendToPlayer(player, new InventoryContainerPayloads.WindowAcknowledgedPayload(menu.containerId, payload.windowId(), payload.slotId()));
                }));
        registrar.playToClient(InventoryContainerPayloads.WindowAcknowledgedPayload.TYPE, InventoryContainerPayloads.WindowAcknowledgedPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> net.royling.lsp.lofty_safe_protection.client.gui.InventoryContainerPanel.acknowledge(payload.menuId(), payload.windowId(), payload.sourceSlot())));
        registrar.playToClient(InventoryContainerPayloads.SyncDefinitionsPayload.TYPE, InventoryContainerPayloads.SyncDefinitionsPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    Map<Identifier, InventoryContainerDefinition> next = new HashMap<>();
                    for (JsonElement entry : JsonParser.parseString(payload.json()).getAsJsonArray()) {
                        var json = entry.getAsJsonObject();
                        var id = Identifier.parse(json.get("id").getAsString());
                        next.put(id, InventoryContainerDefinition.parse(id, json));
                    }
                    InventoryContainerManager.definitions = Map.copyOf(next);
                    SafeBoxCreativeTab.refresh.run();
                }));
    }

    private static void handleWindow(ServerPlayer player, InventoryContainerMenuAccess access, InventoryContainerPayloads.SetOpenPayload payload) {
        var menu = player.containerMenu;
        if (payload.windowId() == -1 && payload.slotId() == -1) { access.lsp$closeInventoryContainers(); return; }
        int index = payload.windowId();
        int hostSlots = access.lsp$hostSlotCount();
        if (index < 0 || index >= hostSlots) return;
        var windows = access.lsp$inventoryContainers();
        if (payload.slotId() == -1) {
            if (index < windows.size()) windows.get(index).close();
            return;
        }
        if (!player.isAlive() || player.isSpectator() || !menu.stillValid(player) || !menu.getCarried().isEmpty()
                || payload.slotId() < 0 || payload.slotId() >= hostSlots
                || !InventoryItemContainer.canOpen(menu.slots.get(payload.slotId()), player)) return;
        windows.forEach(InventoryItemContainer::validateBinding);
        if (access.lsp$isInventoryContainerSource(payload.slotId())) return;
        while (access.lsp$inventoryContainers().size() <= index) if (access.lsp$addInventoryContainer(player) == null) return;
        var storage = access.lsp$inventoryContainers().get(index);
        if (storage.isOpen()) return;
        PacketDistributor.sendToPlayer(player, definitionsPayload());
        storage.open(payload.slotId());
        if (storage.isOpen()) access.lsp$focusInventoryContainer(payload.slotId());
    }
}
