/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.base;

import java.util.ArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;

import reika.dragonapi.base.BlockMultiBlock;

public abstract class BlockReCMultiBlock extends BlockMultiBlock<Boolean> {

	public BlockReCMultiBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	public final ArrayList<String> getMessages(Level world, BlockPos pos, Direction side) {
		BlockEntity te = this.getTileEntityForPosition(world, pos.getX(), pos.getY(), pos.getZ());
		return te instanceof TileEntityReactorBase
				? ((TileEntityReactorBase) te).getMessages(world, pos, side)
				: new ArrayList<>();
	}

}
