package reika.reactorcraft.blocks;

import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockReactorDuct extends BlockReactorMachine {

    public BlockReactorDuct(BlockBehaviour.Properties properties) {
        super(properties.noOcclusion());
    }
}
