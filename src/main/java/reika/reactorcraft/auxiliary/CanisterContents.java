package reika.reactorcraft.auxiliary;

import java.util.Optional;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

/** Immutable canister contents that also decode during 26.3's early recipe/advancement reload. */
public record CanisterContents(Optional<FluidStackTemplate> fluid) {
    public static final CanisterContents EMPTY = new CanisterContents(Optional.empty());
    // Preserve SimpleFluidContent's saved {id, amount, components} / empty {} representation.
    public static final Codec<CanisterContents> CODEC = ExtraCodecs.optionalEmptyMap(FluidStackTemplate.CODEC)
            .xmap(CanisterContents::new, CanisterContents::fluid);
    public static final StreamCodec<RegistryFriendlyByteBuf, CanisterContents> STREAM_CODEC =
            ByteBufCodecs.optional(FluidStackTemplate.STREAM_CODEC).map(CanisterContents::new, CanisterContents::fluid);

    public static CanisterContents of(Fluid fluid, int amount) {
        return fluid == Fluids.EMPTY || amount <= 0 ? EMPTY
                : new CanisterContents(Optional.of(new FluidStackTemplate(fluid, amount)));
    }

    public static CanisterContents copyOf(FluidStack stack) {
        return stack.isEmpty() ? EMPTY
                : new CanisterContents(Optional.of(FluidStackTemplate.fromNonEmptyStack(stack)));
    }

    public boolean isEmpty() { return fluid.isEmpty(); }
    public Fluid getFluid() { return fluid.map(template -> template.fluid().value()).orElse(Fluids.EMPTY); }
    public int getAmount() { return fluid.map(FluidStackTemplate::amount).orElse(0); }
    public FluidStack copy() { return fluid.map(FluidStackTemplate::create).orElse(FluidStack.EMPTY); }
}
