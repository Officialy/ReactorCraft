/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.reactorcraft.auxiliary.RadiationEffects;
import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;

/**
 * 26.2 port: legacy {@code BlockFluidClassic} (Forge fluid block, gone) collapsed to a placed molten-corium
 * block. The original's flow/displacement logic was already commented out; the live behaviour is lethal
 * radiation contamination, kept here as a randomTick.
 */
public class BlockCoriumFlowing extends Block {

	public BlockCoriumFlowing(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
		if (ReikaRandomHelper.doWithChance(2))
			RadiationEffects.instance.contaminateArea(world, pos.getX(), pos.getY()+rand.nextInt(3), pos.getZ(), 8, 1, 0, false, RadiationIntensity.LETHAL);
	}

}
