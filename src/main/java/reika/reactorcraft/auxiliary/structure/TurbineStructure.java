package reika.reactorcraft.auxiliary.structure;

import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

import reika.dragonapi.instantiable.data.blockstruct.FilledBlockArray;
import reika.dragonapi.libraries.ReikaDirectionHelper;
import reika.reactorcraft.base.ReactorStructureBase;
import reika.reactorcraft.blocks.multi.BlockTurbineMulti;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorTiles;


public class TurbineStructure extends ReactorStructureBase {

	@Override
	public FilledBlockArray getArray(Level world, int x, int y, int z) {
		FilledBlockArray array = new FilledBlockArray(world);

		Direction left = ReikaDirectionHelper.getLeftBy90(dir);

		array.fillFrom(((BlockTurbineMulti)ReactorBlocks.TURBINEMULTI.getBlockInstance()).getBlueprint(), x, y, z, dir);

		for (int i = 0; i <= 8; i++) {
			int dx = x+dir.offsetX*i;
			int dz = z+dir.offsetZ*i+left.offsetZ*5;
			ReactorTiles r = i >= 7 ? ReactorTiles.STEAMLINE : ReactorTiles.BIGTURBINE;
			array.setBlock(dx, y+5, dz, r.getBlock(), r);
		}

		return array;
	}

}
