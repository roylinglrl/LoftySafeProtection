package net.royling.lsp.lofty_safe_protection.data;

import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class InventoryContainerManager {
    public static Item ITEM;
    public static volatile Map<Identifier, InventoryContainerDefinition> definitions = Map.of();

    private InventoryContainerManager() {}

    public static InventoryContainerDefinition definition(ItemStack stack) {
        if (ITEM == null || !stack.is(ITEM)) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        Identifier id = Identifier.tryParse(data.copyTag().getStringOr("InventoryContainerId", ""));
        return id == null ? null : definitions.get(id);
    }

    public static ItemStack stack(Identifier id) {
        ItemStack stack = new ItemStack(ITEM);
        CompoundTag tag = new CompoundTag();
        tag.putString("InventoryContainerId", id.toString());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }
}
