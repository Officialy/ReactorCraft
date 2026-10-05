package reika.reactorcraft.registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import reika.reactorcraft.auxiliary.CanisterContents;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import reika.reactorcraft.ReactorCraft;

/**
 * ReactorCraft item data components. Fuel-rod burnup and waste isotope still ride
 * {@code ItemStack.getDamageValue()} (see the fuel/waste TEs); the fluid canister carries a full
 * {@link CanisterContents}, and the CPU remote stores a dimension-aware location.
 */
public final class ReactorDataComponents {

    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ReactorCraft.MODID);

    /** Fluid held by an (otherwise identical) canister item. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CanisterContents>> CANISTER_FLUID =
            COMPONENTS.registerComponentType("canister_fluid", b -> b
                    .persistent(CanisterContents.CODEC)
                    .networkSynchronized(CanisterContents.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<net.minecraft.core.GlobalPos>> REMOTE_CPU =
            COMPONENTS.registerComponentType("remote_cpu", b -> b
                    .persistent(net.minecraft.core.GlobalPos.CODEC)
                    .networkSynchronized(net.minecraft.core.GlobalPos.STREAM_CODEC));

    private ReactorDataComponents() {}
}
