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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import reika.reactorcraft.auxiliary.ReactorStacks;
import reika.reactorcraft.registry.FluoriteTypes;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorItems;

/**
 * The tritium lamp: crafted dark, then charged with a tritium canister (right-click) to glow at
 * full brightness. One block per fluorite colour.
 *
 * TODO: the legacy lamp burned out after ~98 Minecraft years (TileEntityTritiumLamp.LIFESPAN) and
 * reverted to its unlit state; the timer TE is not ported (the lifespan is effectively eternal in
 * real play).
 */
public class BlockTritiumLamp extends Block {

	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	private final FluoriteTypes color;

	public BlockTritiumLamp(BlockBehaviour.Properties properties, FluoriteTypes color) {
		super(properties);
		this.color = color;
		this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
	}

	public FluoriteTypes getColor() {
		return color;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!state.getValue(LIT) && ReactorStacks.isCanisterOf(stack, ReactorFluids.TRITIUM.get())) {
			if (!level.isClientSide()) {
				level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL);
				if (!player.getAbilities().instabuild)
					player.setItemInHand(hand, ReactorItems.CANISTER_REF.getStackOf());
			}
			return InteractionResult.SUCCESS;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}

}
