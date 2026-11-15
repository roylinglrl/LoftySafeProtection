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
    public boolean canFitInsideContainerItems(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        var contents = ContainerContentsSummary.fromItem(stack);
        if (contents != null) {
            if (contents.capacity() > 0) {
                lines.add(SafeBoxTooltips.capacity(contents.rows(), contents.columns()));
                lines.add(SafeBoxTooltips.usage(contents.used(), contents.capacity()));
            }
            if (contents.fluids().enabled()) lines.add(Component.translatable("tooltip.lofty_safe_protection.fluid_capacity",
                    contents.fluids().slots(), contents.fluids().capacity()));
        }
    }
}
