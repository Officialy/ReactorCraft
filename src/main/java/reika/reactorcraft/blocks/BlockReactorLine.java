package reika.reactorcraft.blocks;

import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Lines (steam line, heat pipe). Extends {@link BlockReactorMachineModelled} so datagen emits an EMPTY
 * in-world model (no cube) and the connected pipe is drawn entirely by
 * {@link reika.reactorcraft.renders.ReactorLineRenderer} from the BE's connection cache — the legacy
 * {@code RenderWaterLine} approach, ported to the 26.2 SubmitNodeCollector pipeline.
 */
public class BlockReactorLine extends BlockReactorMachineModelled {

    public BlockReactorLine(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
