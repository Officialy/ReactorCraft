/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.renders;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import reika.reactorcraft.models.ModelBigTurbine;
import reika.reactorcraft.registry.ReactorModelLayers;

/** High-pressure tiers share the turbine cap, wear and immutable submission path. */
public class RenderBigTurbine extends RenderTurbine {
    public RenderBigTurbine(BlockEntityRendererProvider.Context context) {
        super(Identifier.parse("reactorcraft:textures/tileentity/bigturbine.png"),
                ReactorModelLayers.BIG_TURBINE_STAGES.length, stage -> {
                    var model = new ModelBigTurbine(context.bakeLayer(ReactorModelLayers.BIG_TURBINE_STAGES[stage]), stage);
                    return model::renderAll;
                });
    }
}
