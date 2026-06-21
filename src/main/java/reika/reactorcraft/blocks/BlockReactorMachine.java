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
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import reika.dragonapi.base.BlockEntityBase;
import reika.dragonapi.base.BlockTEBase;
import reika.dragonapi.interfaces.block.MachineRegistryBlock;
import reika.dragonapi.interfaces.registry.TileEnum;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorTiles;

public class BlockReactorMachine extends BlockTEBase implements MachineRegistryBlock {

    public boolean hasVerticalPlacement = false;

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public BlockReactorMachine(BlockBehaviour.Properties properties) {
        super(properties.strength(4, 15));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        if (hasVerticalPlacement) {
            return this.defaultBlockState().setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
        }
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public final TileEnum getMachine(BlockGetter level, BlockPos pos) {
        return ReactorTiles.getMachine((Level) level, pos);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        ReactorTiles tile = ReactorTiles.getMachineMapping(state.getBlock());
        if (tile == null)
            return null;
        return tile.createBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide())
            return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof TileEntityReactorBase rc) {
                rc.updateEntity(lvl, pos);
            } else if (be instanceof BlockEntityBase base) {
                base.updateEntity(lvl, pos);
            }
        };
    }
}
