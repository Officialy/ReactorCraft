package reika.reactorcraft.blocks;

import net.minecraft.world.level.block.state.BlockBehaviour;

/** Modelled reactor machine — BER registration deferred; same TE/block behaviour as {@link BlockReactorMachine}. */
public class BlockReactorMachineModelled extends BlockReactorMachine {

    public BlockReactorMachineModelled(BlockBehaviour.Properties properties) {
        super(properties.noOcclusion());
    }
}
