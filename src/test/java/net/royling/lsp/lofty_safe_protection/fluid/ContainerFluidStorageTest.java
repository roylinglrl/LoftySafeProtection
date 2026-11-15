package net.royling.lsp.lofty_safe_protection.fluid;

import com.google.gson.JsonParser;
import java.util.Map;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerDefinition;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerManager;
import net.royling.lsp.lofty_safe_protection.registry.SafeBoxDataComponents;
import net.royling.lsp.lofty_safe_protection.storage.InventoryContainerBinding;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContainerFluidStorageTest {
    private static final ResourceLocation ID = ResourceLocation.parse("test:fluid_storage");
    private Map<ResourceLocation, InventoryContainerDefinition> previous;

    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }
    @BeforeEach void configure() {
        previous = InventoryContainerManager.definitions;
        install(2, 4000);
    }
    @AfterEach void restore() { InventoryContainerManager.definitions = previous; }

    private void install(int slots, int capacity) {
        var json = JsonParser.parseString("{\"id\":\"test:fluid_storage\",\"translation_key\":\"test.fluid\",\"fluids\":{\"slots\":" + slots + ",\"capacity\":" + capacity + "}}").getAsJsonObject();
        InventoryContainerManager.definitions = Map.of(ID, InventoryContainerDefinition.parse(ID, json));
    }
    private ItemStack item() { return InventoryContainerManager.stack(ID); }

    @Test void individualTanksKeepTypesAndHonorCapacityWithoutChangingOnSimulation() {
        ItemStack item = item();
        var handler = new ContainerFluidHandler(item);
        assertEquals(2, handler.getTanks());
        assertEquals(4000, handler.fill(0, new FluidStack(Fluids.WATER, 6000), FluidAction.SIMULATE));
        assertTrue(ContainerFluidHandler.contents(item).isEmpty());
        assertEquals(4000, handler.fill(0, new FluidStack(Fluids.WATER, 6000), FluidAction.EXECUTE));
        assertEquals(0, handler.fill(0, new FluidStack(Fluids.LAVA, 1000), FluidAction.EXECUTE));
        assertEquals(4000, handler.fill(1, new FluidStack(Fluids.LAVA, 4000), FluidAction.EXECUTE));
        assertEquals(2000, handler.drain(0, 2000, FluidAction.SIMULATE).getAmount());
        assertEquals(4000, handler.getFluidInTank(0).getAmount());
        handler.getFluidInTank(0).setAmount(1);
        assertEquals(4000, handler.getFluidInTank(0).getAmount());
        assertTrue(handler.drain(0, 1000, FluidAction.EXECUTE).is(Fluids.WATER));
        assertTrue(handler.getFluidInTank(1).is(Fluids.LAVA));
    }

    @Test void bucketsTransferOnlyWhenTheCompleteBucketFits() {
        ItemStack item = item();
        ItemStack water = new ItemStack(Items.WATER_BUCKET);
        var deposit = ContainerFluidTransfers.prepare(item, water, 0, true);
        assertNotNull(deposit);
        assertTrue(deposit.cursor().is(Items.BUCKET));
        assertEquals(1000, deposit.contents().fluid(0).getAmount());
        assertTrue(ContainerFluidHandler.contents(item).isEmpty());
        assertTrue(water.is(Items.WATER_BUCKET));
        item.set(SafeBoxDataComponents.FLUID_CONTENTS.get(), deposit.contents());
        var withdraw = ContainerFluidTransfers.prepare(item, new ItemStack(Items.BUCKET, 3), 0, false);
        assertNotNull(withdraw);
        assertEquals(2, withdraw.cursor().getCount());
        assertTrue(withdraw.extra().is(Items.WATER_BUCKET));
        assertTrue(withdraw.contents().isEmpty());
        assertEquals(1000, ContainerFluidHandler.contents(item).fluid(0).getAmount());
        install(2, 500);
        assertNull(ContainerFluidTransfers.prepare(item(), water, 0, true));
        assertNull(ContainerFluidTransfers.prepare(item(), water, -1, true));
        assertNull(ContainerFluidTransfers.prepare(item(), water, 2, true));
    }

    @Test void fluidItemCapabilitiesAllowPartialTransfersAndPreserveTheOtherFluid() {
        ItemStack heldTank = item();
        var handler = new ContainerFluidHandler(heldTank);
        handler.fill(0, new FluidStack(Fluids.WATER, 1250), FluidAction.EXECUTE);
        handler.fill(1, new FluidStack(Fluids.LAVA, 500), FluidAction.EXECUTE);
        var deposit = ContainerFluidTransfers.prepare(item(), heldTank, 1, true);
        assertNotNull(deposit);
        assertEquals(1250, deposit.contents().fluid(1).getAmount());
        assertTrue(ContainerFluidHandler.contents(deposit.cursor()).fluid(0).isEmpty());
        assertEquals(500, ContainerFluidHandler.contents(deposit.cursor()).fluid(1).getAmount());
        assertEquals(1250, ContainerFluidHandler.contents(heldTank).fluid(0).getAmount());
    }

    @Test void milkBucketsUseTheRegisteredMilkFluidAndRoundTripToBuckets() {
        ItemStack item = item();
        var deposit = ContainerFluidTransfers.prepare(item, new ItemStack(Items.MILK_BUCKET), 0, true);
        assertNotNull(deposit);
        assertTrue(deposit.cursor().is(Items.BUCKET));
        assertEquals(1000, deposit.contents().fluid(0).getAmount());
        item.set(SafeBoxDataComponents.FLUID_CONTENTS.get(), deposit.contents());
        var withdraw = ContainerFluidTransfers.prepare(item, new ItemStack(Items.BUCKET), 0, false);
        assertNotNull(withdraw);
        assertTrue(withdraw.cursor().is(Items.MILK_BUCKET));
        assertTrue(withdraw.contents().isEmpty());
    }

    @Test void reducedOrDisabledSettingsKeepExistingFluidAvailableForWithdrawal() {
        ItemStack item = item();
        var handler = new ContainerFluidHandler(item);
        handler.fill(1, new FluidStack(Fluids.WATER, 4000), FluidAction.EXECUTE);
        install(1, 1000);
        assertEquals(2, handler.getTanks());
        assertEquals(4000, handler.getTankCapacity(1));
        assertEquals(0, handler.fill(1, new FluidStack(Fluids.WATER, 1), FluidAction.EXECUTE));
        assertEquals(1000, handler.drain(1, 1000, FluidAction.EXECUTE).getAmount());
        handler.drain(1, 3000, FluidAction.EXECUTE);
        assertEquals(1, handler.getTanks());
        assertEquals(1000, handler.getTankCapacity(0));
        handler.fill(0, new FluidStack(Fluids.LAVA, 1000), FluidAction.EXECUTE);
        InventoryContainerManager.definitions = Map.of(ID, new InventoryContainerDefinition(ID, "test.fluid", ID, 1, 1, false, java.util.Set.of(), java.util.Set.of()));
        assertEquals(0, handler.fill(0, new FluidStack(Fluids.LAVA, 1), FluidAction.EXECUTE));
        assertEquals(1000, handler.drain(0, 1000, FluidAction.EXECUTE).getAmount());
        assertEquals(0, handler.getTanks());
    }

    @Test void fluidsSurviveItemSerializationAndNestedContainerStorage() {
        ItemStack item = item();
        new ContainerFluidHandler(item).fill(0, new FluidStack(Fluids.LAVA, 2345), FluidAction.EXECUTE);
        ItemStack parent = new ItemStack(Items.BUNDLE);
        parent.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(item)));
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var restored = ItemStack.parseOptional(registries, (CompoundTag) parent.save(registries));
        var nested = restored.get(DataComponents.CONTAINER).getStackInSlot(0);
        assertTrue(ContainerFluidHandler.contents(nested).fluid(0).is(Fluids.LAVA));
        assertEquals(2345, ContainerFluidHandler.contents(nested).fluid(0).getAmount());
    }

    @Test void competingWindowsCannotOverwriteFluidChangesAndItemWritesPreserveFluids() {
        var inventory = new SimpleContainer(1);
        inventory.setItem(0, item());
        Slot slot = new Slot(inventory, 0, 0, 0);
        var first = new InventoryContainerBinding(slot);
        var second = new InventoryContainerBinding(slot);
        var plan = ContainerFluidTransfers.prepare(inventory.getItem(0), new ItemStack(Items.WATER_BUCKET), 0, true);
        assertNotNull(plan);
        assertTrue(first.writeFluids(plan.contents()));
        assertFalse(second.writeFluids(ContainerFluidContents.EMPTY));
        assertTrue(first.write(List.of(new ItemStack(Items.PAPER)), 1, 1));
        assertEquals(1000, ContainerFluidHandler.contents(inventory.getItem(0)).fluid(0).getAmount());
        assertTrue(inventory.getItem(0).get(DataComponents.CONTAINER).getStackInSlot(0).is(Items.PAPER));
    }
}
