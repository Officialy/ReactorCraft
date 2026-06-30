package reika.reactorcraft.blocks;

import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Fluid ducts (gas duct, magnetic pipe, waste pipe). Extends {@link BlockReactorMachineModelled} so the
 * datagen provider emits an EMPTY in-world model (no cube) and the connected pipe + fluid is drawn
 * entirely by {@link reika.reactorcraft.renders.ReactorPipeRenderer} from the BE's connection cache —
 * the legacy {@code DuctRenderer} approach, ported to the 26.2 SubmitNodeCollector pipeline.
 */
public class BlockReactorDuct extends BlockReactorMachineModelled {

    public BlockReactorDuct(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
