package reika.reactorcraft.blocks;

import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockReactorLine extends BlockReactorMachine {

    public BlockReactorLine(BlockBehaviour.Properties properties) {
        super(properties.noOcclusion());
    }
}
