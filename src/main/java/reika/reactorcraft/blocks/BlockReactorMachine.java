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

import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import reika.dragonapi.base.BlockEntityBase;
import reika.dragonapi.base.BlockTEBase;
import reika.dragonapi.interfaces.block.MachineRegistryBlock;
import reika.dragonapi.interfaces.blockentity.HasFluidResourceHandler;
import reika.dragonapi.interfaces.registry.TileEnum;
import reika.reactorcraft.auxiliary.ReactorStacks;
import reika.reactorcraft.base.TileEntityNuclearBoiler;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.TileEntityHeavyPump;
import reika.reactorcraft.tileentities.fission.TileEntityReactorBoiler;
import reika.reactorcraft.tileentities.fission.TileEntityWaterCell;
import reika.reactorcraft.tileentities.fission.TileEntityWaterCell.LiquidStates;
import reika.reactorcraft.tileentities.fission.breeder.TileEntitySodiumHeater;
import reika.reactorcraft.tileentities.fusion.TileEntityToroidMagnet;
import reika.reactorcraft.tileentities.powergen.TileEntityTurbineCore;
import reika.reactorcraft.tileentities.processing.TileEntityCentrifuge;
import reika.reactorcraft.tileentities.processing.TileEntityElectrolyzer;
import reika.reactorcraft.tileentities.processing.TileEntitySynthesizer;
import reika.reactorcraft.tileentities.processing.TileEntityUProcessor;
import reika.rotarycraft.auxiliary.RotaryAux;
import reika.rotarycraft.registry.RotaryItems;

public class BlockReactorMachine extends BlockTEBase implements MachineRegistryBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public BlockReactorMachine(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
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
     */
    private InteractionResult openMenuIfPresent(Level level, BlockPos pos, Player player) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof TileEntityReactorBase rc && rc.hasGui()) {
            if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                if (rc instanceof reika.reactorcraft.tileentities.fission.TileEntityCPU cpu)
                    sp.openMenu(cpu, data -> reika.reactorcraft.container.MenuCPU.writeOpeningData(data, cpu));
                else
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
        InteractionResult fluid = handleFluidContainer(stack, level, pos, player, hand);
        if (fluid != null)
            return fluid;
        InteractionResult r = openMenuIfPresent(level, pos, player);
        return r == InteractionResult.SUCCESS ? InteractionResult.SUCCESS : super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    /** Swap the held item for {@code out} unless the player is in creative. */
    private static void give(Player player, InteractionHand hand, ItemStack out) {
        if (!player.getAbilities().instabuild)
            player.setItemInHand(hand, out);
    }

    private static boolean transferContainer(HasFluidResourceHandler machine, Fluid fluid,
            boolean fillingMachine, Level level, Player player, InteractionHand hand, ItemStack result) {
        ResourceHandler<FluidResource> handler = machine.getFluidHandler(null);
        if (handler == null) return false;
        FluidResource resource = FluidResource.of(fluid);
        if (level.isClientSide()) return true;
        try (Transaction transaction = Transaction.openRoot()) {
            int moved = fillingMachine ? handler.insert(resource, 1000, transaction)
                    : handler.extract(resource, 1000, transaction);
            if (moved != 1000) return false;
            transaction.commit();
        }
        give(player, hand, result);
        return true;
    }

    /**
     * The bucket/canister machine interactions: coolant cell fill/drain, boiler and sodium heater
     * filling, synthesizer/electrolyzer water, heavy-pump withdrawal, processor/centrifuge canister
     * exchange, turbine lubricant, and the toroid-magnet aim refresh. Returns null when the click is
     * not a fluid-container interaction (falls through to the GUI).
     */
    private InteractionResult handleFluidContainer(ItemStack is, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown())
            return null;
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof TileEntityToroidMagnet magnet && RotaryAux.isHoldingScrewdriver(player)) {
            if (!level.isClientSide())
                magnet.refreshAlpha();
            return null;
        }

        if (is.isEmpty() || is.getCount() != 1)
            return null;

        if (be instanceof TileEntityWaterCell cell) {
            switch (cell.getLiquidState()) {
                case EMPTY -> {
                    if (is.getItem() == Items.WATER_BUCKET) {
                        if (!level.isClientSide()) {
                            cell.setLiquidState(LiquidStates.WATER);
                            give(player, hand, new ItemStack(Items.BUCKET));
                        }
                        return InteractionResult.SUCCESS;
                    }
                    if (is.getItem() == ReactorItems.HEAVY_BUCKET.get()) {
                        if (!level.isClientSide()) {
                            cell.setLiquidState(LiquidStates.HEAVY);
                            give(player, hand, new ItemStack(Items.BUCKET));
                        }
                        return InteractionResult.SUCCESS;
                    }
                    if (ReactorStacks.isCanisterOf(is, ReactorFluids.SODIUM.get())) {
                        if (!level.isClientSide()) {
                            cell.setLiquidState(LiquidStates.SODIUM);
                            give(player, hand, ReactorItems.CANISTER_REF.getStackOf());
                        }
                        return InteractionResult.SUCCESS;
                    }
                    if (ReactorStacks.isCanisterOf(is, ReactorFluids.LIFBE.get())) {
                        if (!level.isClientSide()) {
                            cell.setLiquidState(LiquidStates.LITHIUM);
                            give(player, hand, ReactorItems.CANISTER_REF.getStackOf());
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
                case WATER -> {
                    if (is.getItem() == Items.BUCKET) {
                        if (!level.isClientSide()) {
                            cell.setLiquidState(LiquidStates.EMPTY);
                            player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
                case HEAVY -> {
                    if (is.getItem() == Items.BUCKET) {
                        if (!level.isClientSide()) {
                            cell.setLiquidState(LiquidStates.EMPTY);
                            player.setItemInHand(hand, new ItemStack(ReactorItems.HEAVY_BUCKET.get()));
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
                case SODIUM -> {
                    if (ReactorStacks.isEmptyCanister(is)) {
                        if (!level.isClientSide()) {
                            cell.setLiquidState(LiquidStates.EMPTY);
                            player.setItemInHand(hand, ReactorStacks.canisterOf(ReactorFluids.SODIUM.get()));
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
                case LITHIUM -> {
                    if (ReactorStacks.isEmptyCanister(is)) {
                        if (!level.isClientSide()) {
                            cell.setLiquidState(LiquidStates.EMPTY);
                            player.setItemInHand(hand, ReactorStacks.canisterOf(ReactorFluids.LIFBE.get()));
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return null;
        }

        if (be instanceof TileEntitySynthesizer synth && is.getItem() == Items.WATER_BUCKET) {
            transferContainer(synth, Fluids.WATER, true, level, player, hand, new ItemStack(Items.BUCKET));
            return InteractionResult.SUCCESS;
        }

        if (be instanceof TileEntityElectrolyzer lyzer && is.getItem() == ReactorItems.HEAVY_BUCKET.get()) {
            transferContainer(lyzer, ReactorFluids.HEAVY_WATER.get(), true,
                    level, player, hand, new ItemStack(Items.BUCKET));
            return InteractionResult.SUCCESS;
        }

        if (be instanceof TileEntityHeavyPump pump && pump.hasABucket()) {
            if (is.getItem() == Items.BUCKET && pump.getFluid() == ReactorFluids.HEAVY_WATER.get()) {
                transferContainer(pump, ReactorFluids.HEAVY_WATER.get(), false,
                        level, player, hand, new ItemStack(ReactorItems.HEAVY_BUCKET.get()));
                return InteractionResult.SUCCESS;
            }
            if (ReactorStacks.isEmptyCanister(is) && pump.getFluid() == ReactorFluids.LITHIUM.get()) {
                transferContainer(pump, ReactorFluids.LITHIUM.get(), false,
                        level, player, hand, ReactorStacks.canisterOf(ReactorFluids.LITHIUM.get()));
                return InteractionResult.SUCCESS;
            }
            return null;
        }

        if (be instanceof TileEntitySodiumHeater heater && ReactorStacks.isCanisterOf(is, ReactorFluids.SODIUM.get())) {
            transferContainer(heater, ReactorFluids.SODIUM.get(), true,
                    level, player, hand, ReactorItems.CANISTER_REF.getStackOf());
            return InteractionResult.SUCCESS;
        }

        if (be instanceof TileEntityReactorBoiler boiler) {
            if (is.getItem() == Items.WATER_BUCKET) {
                transferContainer(boiler, Fluids.WATER, true,
                        level, player, hand, new ItemStack(Items.BUCKET));
                return InteractionResult.SUCCESS;
            }
            if (ReactorStacks.isCanisterOf(is, ReactorFluids.AMMONIA.get())) {
                transferContainer(boiler, ReactorFluids.AMMONIA.get(), true,
                        level, player, hand, ReactorItems.CANISTER_REF.getStackOf());
                return InteractionResult.SUCCESS;
            }
            return null;
        }

        if (be instanceof TileEntityUProcessor proc && is.getItem() == ReactorItems.CANISTER.get()) {
            if (ReactorStacks.isEmptyCanister(is) && proc.getOutput() >= 1000) {
                transferContainer(proc, proc.getOutputFluid(), false,
                        level, player, hand, ReactorStacks.canisterOf(proc.getOutputFluid()));
            } else if (ReactorStacks.isCanisterOf(is, ReactorFluids.HF.get())) {
                transferContainer(proc, ReactorFluids.HF.get(), true,
                        level, player, hand, ReactorItems.CANISTER_REF.getStackOf());
            }
            return InteractionResult.SUCCESS;
        }

        if (be instanceof TileEntityCentrifuge fuge && is.getItem() == ReactorItems.CANISTER.get()) {
            if (ReactorStacks.isEmptyCanister(is) && fuge.getUF6() >= 1000) {
                transferContainer(fuge, ReactorFluids.UF6.get(), false,
                        level, player, hand, ReactorStacks.canisterOf(ReactorFluids.UF6.get()));
            } else if (ReactorStacks.isCanisterOf(is, ReactorFluids.UF6.get())) {
                transferContainer(fuge, ReactorFluids.UF6.get(), true,
                        level, player, hand, ReactorItems.CANISTER_REF.getStackOf());
            }
            return InteractionResult.SUCCESS;
        }

        if (be instanceof TileEntityTurbineCore turbine && is.getItem() == RotaryItems.LUBE_BUCKET.get()) {
            transferContainer(turbine, ReactorFluids.getLegacyFluid("rc lubricant"), true,
                    level, player, hand, new ItemStack(Items.BUCKET));
            return InteractionResult.SUCCESS;
        }

        return null;
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
