package net.royling.lsp.lofty_safe_protection.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerManager;

public final class InventoryContainerItem extends Item {
    public InventoryContainerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public Component getName(ItemStack stack) {
        var definition = InventoryContainerManager.definition(stack);
        return definition == null ? super.getName(stack) : definition.title();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canFitInsideContainerItems() {
        return false;
    }


    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, List<Component> lines, TooltipFlag flag) {
        var contents = ContainerContentsSummary.fromItem(stack);
        if (contents != null) {
            lines.add(SafeBoxTooltips.capacity(contents.rows(), contents.columns()));
            lines.add(SafeBoxTooltips.usage(contents.used(), contents.capacity()));
        }
    }
    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(net.royling.lsp.lofty_safe_protection.client.LoftySafeProtectionClient.itemExtension());
    }
}
