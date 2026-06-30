package reika.reactorcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import reika.reactorcraft.base.TileEntityReactorPiping;

/**
 * Fluid ducts (gas duct, magnetic pipe, waste pipe). Extends {@link BlockReactorMachineModelled} so the
 * datagen provider emits an EMPTY in-world model (no cube) and the connected pipe + fluid is drawn
 * entirely by {@link reika.reactorcraft.renders.ReactorPipeRenderer} from the BE's connection cache —
 * the legacy {@code DuctRenderer} approach, ported to the 26.2 SubmitNodeCollector pipeline.
 */
public class BlockReactorDuct extends BlockReactorMachineModelled {

    // Legacy BlockDuct.getCollisionBoundingBoxFromPool: each disconnected face is inset by 0.125,
    // leaving a 0.75-wide core that extends to the full block edge only toward connected neighbours
    // (matching the arm geometry ReactorPipeRenderer draws). The legacy Y-axis insets were transposed
    // (min used the UP flag, max used DOWN) -- that reads as a transcription bug, not intent, since
    // every other axis pairs min with its own negative-direction flag; this port uses the consistent
    // mapping so the box actually opens toward the side that is connected.
    private static final double INSET = 0.125;

    public BlockReactorDuct(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity te = level.getBlockEntity(pos);
        if (!(te instanceof TileEntityReactorPiping rp))
            return Shapes.block();
        double minX = rp.isConnectedDirectly(Direction.WEST) ? 0 : INSET;
        double maxX = rp.isConnectedDirectly(Direction.EAST) ? 1 : 1 - INSET;
        double minY = rp.isConnectedDirectly(Direction.DOWN) ? 0 : INSET;
        double maxY = rp.isConnectedDirectly(Direction.UP) ? 1 : 1 - INSET;
        double minZ = rp.isConnectedDirectly(Direction.NORTH) ? 0 : INSET;
        double maxZ = rp.isConnectedDirectly(Direction.SOUTH) ? 1 : 1 - INSET;
        return Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }
}
