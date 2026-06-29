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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.BlockHitResult;

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

    /**
     * Opens the machine's GUI on right-click. Mirrors the legacy {@code onBlockActivated} GUI-open path
     * ({@code ep.openGui(...)} → {@code ServerPlayer.openMenu(provider, pos)}). Gated on
     * {@link TileEntityReactorBase#hasGui()} so GUI-less machines pass through.
     *
     * TODO(OFF-51 follow-up): the legacy {@code BlockReactorTile.onBlockActivated} also handled
     * bucket/canister fluid interactions (water/heavy-water/sodium cells, boiler/synthesizer/processor
     * filling, turbine lubricant). Those item interactions are not yet ported and belong in
     * {@link #useItemOn}.
     */
    private InteractionResult openMenuIfPresent(Level level, BlockPos pos, Player player) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof TileEntityReactorBase rc && rc.hasGui()) {
            if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                sp.openMenu(rc, pos);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return openMenuIfPresent(level, pos, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult r = openMenuIfPresent(level, pos, player);
        return r == InteractionResult.SUCCESS ? InteractionResult.SUCCESS : super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide())
            return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof BlockEntityBase base) {
                // Drive the BlockEntityBase lifecycle FIRST (no-arg updateEntity): this fires onFirstTick
                // on tick 0 and advances ticksExisted/sync. Without it, onFirstTick never runs — which
                // left TileEntityCPU.layout null (crash) and reactor cores never initialising. RotaryCraft
                // machines call super.updateEntity() at the top of their 2-arg method for the same reason;
                // doing it here covers every ReactorCraft TE in one place.
                base.updateEntity();
                base.updateEntity(lvl, pos);
            }
        };
    }
}
