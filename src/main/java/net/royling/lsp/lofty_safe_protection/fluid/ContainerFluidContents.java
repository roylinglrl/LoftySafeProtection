package net.royling.lsp.lofty_safe_protection.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

public record ContainerFluidContents(int capacity, List<SimpleFluidContent> tanks) {
    public static final ContainerFluidContents EMPTY = new ContainerFluidContents(0, List.of());
    private static final Codec<List<SimpleFluidContent>> TANKS = SimpleFluidContent.CODEC.listOf().validate(list ->
            list.size() <= FluidStorageSettings.MAX_SLOTS ? DataResult.success(list) : DataResult.error(() -> "Too many fluid slots"));
    public static final Codec<ContainerFluidContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("capacity").forGetter(ContainerFluidContents::capacity),
            TANKS.fieldOf("tanks").forGetter(ContainerFluidContents::tanks)
    ).apply(instance, ContainerFluidContents::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ContainerFluidContents> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ContainerFluidContents::capacity,
            SimpleFluidContent.STREAM_CODEC.apply(ByteBufCodecs.list(FluidStorageSettings.MAX_SLOTS)), ContainerFluidContents::tanks,
            ContainerFluidContents::new);

    public ContainerFluidContents {
        if (capacity < 0 || tanks.size() > FluidStorageSettings.MAX_SLOTS) throw new IllegalArgumentException("Invalid fluid contents");
        tanks = List.copyOf(tanks);
    }

    public boolean isEmpty() { return tanks.stream().allMatch(SimpleFluidContent::isEmpty); }
    public FluidStack fluid(int slot) { return slot >= 0 && slot < tanks.size() ? tanks.get(slot).copy() : FluidStack.EMPTY; }

    public ContainerFluidContents with(int slot, FluidStack fluid, FluidStorageSettings layout) {
        var updated = new ArrayList<>(tanks);
        while (updated.size() < layout.slots()) updated.add(SimpleFluidContent.EMPTY);
        updated.set(slot, SimpleFluidContent.copyOf(fluid));
        return new ContainerFluidContents(layout.capacity(), updated);
    }
}
