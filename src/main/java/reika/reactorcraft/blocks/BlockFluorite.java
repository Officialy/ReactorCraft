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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import reika.dragonapi.libraries.registry.ReikaParticleHelper;
import reika.reactorcraft.registry.FluoriteTypes;

/** One decorative block per colour; neutron excitation is temporary and never changes its drops. */
public class BlockFluorite extends Block {
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");
    private final FluoriteTypes color;

    public BlockFluorite(Properties properties, FluoriteTypes color) {
        super(properties);
        this.color = color;
        registerDefaultState(stateDefinition.any().setValue(ACTIVATED, false));
    }

    public FluoriteTypes getColor() { return color; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVATED);
    }

    public void activate(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(this) && !state.getValue(ACTIVATED))
            level.setBlock(pos, state.setValue(ACTIVATED, true), UPDATE_ALL);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(ACTIVATED))
            level.setBlock(pos, state.setValue(ACTIVATED, false), UPDATE_ALL);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        randomTick(state, level, pos, random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(ACTIVATED)) {
            double r = color == FluoriteTypes.WHITE ? 4 : color.red / 255D;
            double g = color == FluoriteTypes.WHITE ? 4 : color.green / 255D;
            double b = color == FluoriteTypes.WHITE ? 4 : color.blue / 255D;
            ReikaParticleHelper.spawnColoredParticlesWithOutset(level, pos, r, g, b, 4, 0.125);
        }
        // COLORLIGHT-PORT: coloured-light integrations use getColor(); vanilla emits level 15.
    }

    public boolean isActivated(BlockGetter level, int x, int y, int z) {
        return level.getBlockState(new BlockPos(x, y, z)).getValue(ACTIVATED);
    }

    public FluoriteTypes getColorType(BlockGetter level, int x, int y, int z) { return color; }
}
