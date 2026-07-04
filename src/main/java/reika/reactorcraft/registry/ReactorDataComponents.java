package reika.reactorcraft.registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import reika.reactorcraft.ReactorCraft;

/**
 * ReactorCraft item data components. Fuel-rod burnup and waste isotope still ride
 * {@code ItemStack.getDamageValue()} (see the fuel/waste TEs); only the fluid canister needs a
 * component, since it carries a full {@link SimpleFluidContent} rather than an int.
 */
public final class ReactorDataComponents {

    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ReactorCraft.MODID);

    /** Fluid held by an (otherwise identical) canister item. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> CANISTER_FLUID =
            COMPONENTS.registerComponentType("canister_fluid", b -> b
                    .persistent(SimpleFluidContent.CODEC)
                    .networkSynchronized(SimpleFluidContent.STREAM_CODEC));

    private ReactorDataComponents() {}
}
