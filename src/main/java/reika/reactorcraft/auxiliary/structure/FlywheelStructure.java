package reika.reactorcraft.auxiliary.structure;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

import reika.dragonapi.instantiable.data.blockstruct.FilledBlockArray;
import reika.dragonapi.libraries.ReikaDirectionHelper;
import reika.reactorcraft.base.ReactorStructureBase;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorTiles;


public class FlywheelStructure extends ReactorStructureBase {

	@Override
	public FilledBlockArray getArray(Level world, int x, int y, int z) {
		FilledBlockArray array = new FilledBlockArray(world);
		int midX = x;
		int midY = y;
		int midZ = z;
		array.setBlock(midX, midY, midZ, ReactorTiles.FLYWHEEL.getBlock(), ReactorTiles.FLYWHEEL);
		Direction left = ReikaDirectionHelper.getLeftBy90(dir);
		Block b = ReactorBlocks.FLYWHEELMULTI.getBlockInstance();

		for (int i = 1; i <= 2; i++) {
			int dx = midX+left.offsetX*i;
			int dz = midZ+left.offsetZ*i;
			int m = i == 1 ? 0 : 2;
			array.setBlock(dx, midY, dz, b, m);

			dx = midX-left.offsetX*i;
			dz = midZ-left.offsetZ*i;
			array.setBlock(dx, midY, dz, b, m);
			array.setBlock(midX, midY-i, midZ, b, m);
			array.setBlock(midX, midY+i, midZ, b, m);
		}

		int dx = midX+left.offsetX;
		int dz = midZ+left.offsetZ;
		array.setBlock(dx, midY+1, dz, b, 1);
		array.setBlock(dx, midY-1, dz, b, 1);

		dx = midX-left.offsetX;
		dz = midZ-left.offsetZ;
		array.setBlock(dx, midY+1, dz, b, 1);
		array.setBlock(dx, midY-1, dz, b, 1);

		dx = midX+left.offsetX;
		dz = midZ+left.offsetZ;
		array.setBlock(dx, midY+2, dz, b, 2);
		array.setBlock(dx, midY-2, dz, b, 2);

		dx = midX-left.offsetX;
		dz = midZ-left.offsetZ;
		array.setBlock(dx, midY+2, dz, b, 2);
		array.setBlock(dx, midY-2, dz, b, 2);

		dx = midX+left.offsetX*2;
		dz = midZ+left.offsetZ*2;
		array.setBlock(dx, midY+1, dz, b, 2);
		array.setBlock(dx, midY-1, dz, b, 2);

		dx = midX-left.offsetX*2;
		dz = midZ-left.offsetZ*2;
		array.setBlock(dx, midY+1, dz, b, 2);
		array.setBlock(dx, midY-1, dz, b, 2);
		return array;
	}

}
