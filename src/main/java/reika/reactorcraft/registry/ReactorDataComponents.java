package reika.reactorcraft.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import reika.reactorcraft.ReactorCraft;

/**
 * 26.2 data components replacing the legacy {@code ItemStack.getDamageValue()} variant carriers
 * (see {@code PORTING.md} item-variant decision). Defined up-front for the whole content layer so
 * any file migrating off metadata variants — fuel rods, fluid canisters, nuclear waste isotopes —
 * has a stable component type to target instead of introducing a new damage-value carrier.
 */
public final class ReactorDataComponents {

    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ReactorCraft.MODID);

    /** Fluid held by an (otherwise identical) canister item, replacing the metadata canister scheme. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> CANISTER_FLUID =
            COMPONENTS.registerComponentType("canister_fluid", b -> b
                    .persistent(SimpleFluidContent.CODEC)
                    .networkSynchronized(SimpleFluidContent.STREAM_CODEC));

    /** Fuel-rod burnup, formerly the FUEL/PELLET/BREEDERFUEL metadata (0 = fresh). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> FUEL_BURNUP =
            COMPONENTS.registerComponentType("fuel_burnup", b -> b
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    /** Index into {@code WasteManager}, formerly the WASTE item metadata (~1000 isotopes). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> WASTE_ISOTOPE =
            COMPONENTS.registerComponentType("waste_isotope", b -> b
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    private ReactorDataComponents() {}
}
