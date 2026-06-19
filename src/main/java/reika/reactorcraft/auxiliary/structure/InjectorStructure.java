package reika.reactorcraft.auxiliary.structure;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.ForgeDirection;

import reika.dragonapi.instantiable.data.blockstruct.FilledBlockArray;
import reika.dragonapi.libraries.ReikaDirectionHelper;
import reika.reactorcraft.base.ReactorStructureBase;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorTiles;


public class InjectorStructure extends ReactorStructureBase {

	@Override
	public FilledBlockArray getArray(Level world, int x, int y, int z) {
		FilledBlockArray array = new FilledBlockArray(world);

		Block b = ReactorBlocks.INJECTORMULTI.getBlockInstance();
		ForgeDirection left = ReikaDirectionHelper.getLeftBy90(dir);

		for (int i = 0; i <= 4; i++) {
			array.setBlock(x+dir.offsetX*i+left.offsetX, y+3, z+dir.offsetZ*i+left.offsetZ, b, 4);
			array.setBlock(x+dir.offsetX*i-left.offsetX, y+3, z+dir.offsetZ*i-left.offsetZ, b, 4);
		}
		for (int i = 5; i <= 6; i++) {
			array.setBlock(x+dir.offsetX*i+left.offsetX, y+2, z+dir.offsetZ*i+left.offsetZ, b, 4);
			array.setBlock(x+dir.offsetX*i-left.offsetX, y+2, z+dir.offsetZ*i-left.offsetZ, b, 4);
		}
		for (int i = 7; i <= 8; i++) {
			array.setBlock(x+dir.offsetX*i+left.offsetX, y+1, z+dir.offsetZ*i+left.offsetZ, b, 4);
			array.setBlock(x+dir.offsetX*i-left.offsetX, y+1, z+dir.offsetZ*i-left.offsetZ, b, 4);
		}

		for (int i = 0; i <= 8; i++) {
			array.setBlock(x+dir.offsetX*i+left.offsetX, y-1, z+dir.offsetZ*i+left.offsetZ, b, 1);
			array.setBlock(x+dir.offsetX*i-left.offsetX, y-1, z+dir.offsetZ*i-left.offsetZ, b, 1);
		}

		for (int k = 0; k <= 2; k++) {
			array.setBlock(x+left.offsetX, y+k, z+left.offsetZ, b, 6);
			array.setBlock(x-left.offsetX, y+k, z-left.offsetZ, b, 6);
		}
		array.setBlock(x+left.offsetX+dir.offsetX*8, y, z+left.offsetZ+dir.offsetZ*8, b, 6);
		array.setBlock(x-left.offsetX+dir.offsetX*8, y, z-left.offsetZ+dir.offsetZ*8, b, 6);

		for (int i = 0; i <= 4; i++) {
			array.setBlock(x+dir.offsetX*i, y+3, z+dir.offsetZ*i, b, 3);
		}
		for (int i = 5; i <= 6; i++) {
			array.setBlock(x+dir.offsetX*i, y+2, z+dir.offsetZ*i, b, 3);
		}
		for (int i = 7; i <= 8; i++) {
			array.setBlock(x+dir.offsetX*i, y+1, z+dir.offsetZ*i, b, 3);
		}

		for (int i = 0; i <= 8; i++) {
			array.setBlock(x+dir.offsetX*i, y-1, z+dir.offsetZ*i, b, 0);
		}

		for (int i = 1; i <= 1; i++) {
			array.setBlock(x+dir.offsetX*i+left.offsetX, y, z+dir.offsetZ*i+left.offsetZ, b, 2);
			array.setBlock(x+dir.offsetX*i-left.offsetX, y, z+dir.offsetZ*i-left.offsetZ, b, 2);
		}
		for (int i = 3; i <= 7; i++) {
			array.setBlock(x+dir.offsetX*i+left.offsetX, y, z+dir.offsetZ*i+left.offsetZ, b, 2);
			array.setBlock(x+dir.offsetX*i-left.offsetX, y, z+dir.offsetZ*i-left.offsetZ, b, 2);
		}
		for (int i = 1; i <= 6; i++) {
			array.setBlock(x+dir.offsetX*i+left.offsetX, y+1, z+dir.offsetZ*i+left.offsetZ, b, 2);
			array.setBlock(x+dir.offsetX*i-left.offsetX, y+1, z+dir.offsetZ*i-left.offsetZ, b, 2);
		}
		for (int i = 1; i <= 4; i++) {
			array.setBlock(x+dir.offsetX*i+left.offsetX, y+2, z+dir.offsetZ*i+left.offsetZ, b, 2);
			array.setBlock(x+dir.offsetX*i-left.offsetX, y+2, z+dir.offsetZ*i-left.offsetZ, b, 2);
		}
		for (int i = 0; i <= 2; i++) {
			array.setBlock(x, y+i, z, b, 5);
		}

		for (int i = 1; i <= 1; i++) {
			array.setBlock(x+dir.offsetX*i, y, z+dir.offsetZ*i, b, 7);
		}
		for (int i = 1; i <= 6; i++) {
			array.setBlock(x+dir.offsetX*i, y+1, z+dir.offsetZ*i, b, 7);
		}
		for (int i = 1; i <= 4; i++) {
			array.setBlock(x+dir.offsetX*i, y+2, z+dir.offsetZ*i, b, 7);
		}

		for (int i = 3; i <= 8; i++) {
			array.setBlock(x+dir.offsetX*i, y, z+dir.offsetZ*i, ReactorTiles.MAGNETPIPE.getBlock(), ReactorTiles.MAGNETPIPE.getBlockMetadata());
		}
		array.setBlock(x+dir.offsetX*2, y, z+dir.offsetZ*2, ReactorTiles.INJECTOR.getBlock(), ReactorTiles.INJECTOR.getBlockMetadata());

		return array;
	}

}
