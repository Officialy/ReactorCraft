package reika.reactorcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Lines (steam line, heat pipe). Extends {@link BlockReactorMachineModelled} so datagen emits an EMPTY
 * in-world model (no cube) and the connected pipe is drawn entirely by
 * {@link reika.reactorcraft.renders.ReactorLineRenderer} from the BE's connection cache — the legacy
 * {@code RenderWaterLine} approach, ported to the 26.2 SubmitNodeCollector pipeline.
 */
public class BlockReactorLine extends BlockReactorMachineModelled {

    // Legacy BlockSteamLine.getCollisionBoundingBoxFromPool: full block contracted by 0.25 on every
    // side, regardless of connection state (the rendered line itself is a thin rod through the middle).
    private static final VoxelShape SHAPE = Shapes.box(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);

    public BlockReactorLine(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
