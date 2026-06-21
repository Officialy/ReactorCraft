package reika.reactorcraft.client;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.registry.ReactorFluids;

/**
 * Binds a renderable model to every ReactorCraft fluid. 26.2 removed the texture/tint accessors from
 * {@link net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions}; still/flow
 * sprites and the tint colour are now carried by a {@link FluidModel.Unbaked} registered through
 * {@link RegisterFluidModelsEvent}. Every fluid reuses the vanilla water still/flow sprites (no
 * binary authoring needed) and is given a distinct constant {@code FluidTintSource} so each renders
 * in a colour faithful to its nature.
 */
@EventBusSubscriber(modid = ReactorCraft.MODID, value = Dist.CLIENT)
public final class ReactorFluidModels {

    private static final Material STILL = new Material(Identifier.fromNamespaceAndPath("minecraft", "block/water_still"));
    private static final Material FLOW = new Material(Identifier.fromNamespaceAndPath("minecraft", "block/water_flow"));

    private ReactorFluidModels() {}

    @SubscribeEvent
    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        for (ReactorFluids.TintedFluid tf : ReactorFluids.tintedFluids()) {
            FluidModel.Unbaked model = new FluidModel.Unbaked(STILL, FLOW, null, FluidTintSources.constant(tf.tint()));
            event.register(model, tf.fluid().get());
        }
    }
}
