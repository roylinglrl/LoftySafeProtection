package net.royling.lsp.lofty_safe_protection.fluid;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerManager;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.menu.InventoryContainerMenuAccess;
import net.royling.lsp.lofty_safe_protection.storage.SafeBoxSavedData;

@EventBusSubscriber(modid = "lofty_safe_protection")
public final class FluidServerCheck {
    @SubscribeEvent public static void started(ServerStartedEvent event) throws Exception {
        if (!Boolean.getBoolean("lsp.fluidCheck")) return;
        try {
            var player = FakePlayerFactory.getMinecraft(event.getServer().overworld());
            player.setGameMode(GameType.SURVIVAL);
            var menu = player.inventoryMenu;
            player.containerMenu = menu;
            menu.setCarried(ItemStack.EMPTY);
            var id = SafeBoxManager.id("fluid_container");
            require(InventoryContainerManager.definitions.containsKey(id), "Default fluid container JSON was not loaded");
            player.getInventory().setItem(9, InventoryContainerManager.stack(id));
            var access = (InventoryContainerMenuAccess) menu;
            var storage = access.lsp$inventoryContainer();
            require(storage != null, "Portable container was not attached to the native inventory menu");
            storage.open(9);
            require(storage.isOpen() && storage.columns() == 0 && storage.fluids().slots() == 2, "Fluid-only container did not open");
            menu.setCarried(new ItemStack(Items.WATER_BUCKET));
            require(ContainerFluidTransfers.transfer(player, storage, 0, true), "Water bucket deposit failed");
            require(menu.getCarried().is(Items.BUCKET) && storage.fluidContents().fluid(0).getAmount() == 1000, "Bucket or fluid count was incorrect after deposit");
            ItemStack savedItem = storage.sourceStack().copy();
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++)
                if (slot != 9) player.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
            menu.setCarried(new ItemStack(Items.BUCKET, 2));
            require(!ContainerFluidTransfers.transfer(player, storage, 0, false), "Stacked buckets should not overflow a full inventory");
            require(menu.getCarried().getCount() == 2 && storage.fluidContents().fluid(0).getAmount() == 1000, "Failed withdrawal changed items or fluid");
            player.getInventory().setItem(10, ItemStack.EMPTY);
            require(ContainerFluidTransfers.transfer(player, storage, 0, false), "Stacked bucket withdrawal failed after making inventory space");
            require(menu.getCarried().is(Items.BUCKET) && menu.getCarried().getCount() == 1, "Cursor remainder was incorrect");
            require(player.getInventory().getItem(10).is(Items.WATER_BUCKET) && storage.fluidContents().isEmpty(), "Filled bucket was lost or fluid duplicated");
            menu.setCarried(new ItemStack(Items.MILK_BUCKET));
            require(ContainerFluidTransfers.transfer(player, storage, 1, true), "Milk bucket deposit failed");
            require(ContainerFluidTransfers.transfer(player, storage, 1, false) && menu.getCarried().is(Items.MILK_BUCKET), "Milk bucket withdrawal failed");
            var store = new SafeBoxSavedData();
            var state = store.forPlayer(player.getUUID());
            state.columns = 2;
            state.rows = 2;
            state.setItem(0, SafeBoxManager.stack(SafeBoxManager.id("case_2x2")));
            state.setItem(1, savedItem);
            var root = store.save(new CompoundTag(), event.getServer().registryAccess());
            var load = SafeBoxSavedData.class.getDeclaredMethod("load", CompoundTag.class, HolderLookup.Provider.class);
            load.setAccessible(true);
            var restored = (SafeBoxSavedData) load.invoke(null, root, event.getServer().registryAccess());
            require(ContainerFluidHandler.contents(restored.forPlayer(player.getUUID()).getItem(1)).fluid(0).getAmount() == 1000,
                    "Fluid container contents did not survive protected safe-box storage serialization");
            storage.close();
            Path report = Path.of(System.getProperty("lsp.testProjectDir"), "build/reports/fluid-server/result.txt");
            Files.createDirectories(report.getParent());
            Files.writeString(report, "PASS: live fluid definition, native menu, bucket deposit/withdrawal, full inventory rejection and protected storage persistence", StandardCharsets.UTF_8);
        } finally { event.getServer().halt(false); }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
