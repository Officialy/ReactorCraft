package reika.reactorcraft.registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import reika.reactorcraft.auxiliary.CanisterContents;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import reika.reactorcraft.ReactorCraft;

/** Typed ReactorCraft item state. Legacy damage carriers are migrated without changing their values.
 */
public final class ReactorDataComponents {

    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ReactorCraft.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> FUEL_BURNUP =
            COMPONENTS.registerComponentType("fuel_burnup", b -> b.persistent(com.mojang.serialization.Codec.intRange(0, 99)).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MAGNET_CHARGE =
            COMPONENTS.registerComponentType("magnet_charge", b -> b.persistent(com.mojang.serialization.Codec.intRange(0, 7)).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> WASTE_IDENTITY =
            COMPONENTS.registerComponentType("waste_identity", b -> b.persistent(com.mojang.serialization.Codec.STRING).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8));

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
