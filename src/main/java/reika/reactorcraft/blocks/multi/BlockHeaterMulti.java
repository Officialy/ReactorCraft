/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.blocks.multi;

import java.util.Set;

import net.minecraft.world.level.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

import reika.dragonapi.instantiable.data.blockstruct.BlockArray;
import reika.dragonapi.instantiable.data.blockstruct.filledblockarray.BlockMatchFailCallback;
import reika.dragonapi.instantiable.data.blockstruct.StructuredBlockArray;
import reika.dragonapi.instantiable.data.immutable.BlockKey;
import reika.dragonapi.instantiable.data.immutable.Coordinate;
import reika.dragonapi.interfaces.block.SemiTransparent;
import reika.dragonapi.libraries.java.ReikaJavaLibrary;
import reika.reactorcraft.base.BlockReCMultiBlock;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.fusion.TileEntityFusionHeater;
import reika.rotarycraft.registry.MachineRegistry;

public class BlockHeaterMulti extends BlockReCMultiBlock implements SemiTransparent {

	public BlockHeaterMulti(Material par2Material) {
		super(par2Material);
	}

	@Override
	public int getNumberVariants() {
		return 5;
	}

	@Override
	public Boolean checkForFullMultiBlock(World world, int x, int y, int z, Direction dir, BlockMatchFailCallback call) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		Set<BlockKey> set = ReikaJavaLibrary.getSet(new BlockKey(this), new BlockKey(ReactorTiles.HEATER));
		blocks.recursiveAddMultipleWithBounds(world, x, y, z, set, x-6, y-6, z-6, x+6, y+6, z+6);
		if (!this.checkCorners(world, x, y, z, blocks, call))
			return false;
		if (!this.checkEdges(world, x, y, z, blocks, call))
			return false;
		if (!this.checkCore(world, x, y, z, blocks, call))
			return false;
		if (!this.checkFaces(world, x, y, z, blocks, call))
			return false;
		if (!this.checkPipe(world, x, y, z, blocks, call))
			return false;
		return true;
	}

	private boolean checkPipe(World world, int x, int y, int z, StructuredBlockArray blocks, BlockMatchFailCallback call) {
		int mx = blocks.getMinX();
		int my = blocks.getMinY();
		int mz = blocks.getMinZ();
		for (int i = 3; i <= 6; i++) {
			if (ReactorTiles.getTE(world, mx+2, my+i, mz+2) != ReactorTiles.MAGNETPIPE) {
				if (call != null)
					call.onBlockFailure(world, mx+2, my+i, mz+2, new BlockKey(ReactorTiles.MAGNETPIPE));
				return false;
			}
		}
		return true;
	}

	private boolean checkCore(World world, int x, int y, int z, StructuredBlockArray blocks, BlockMatchFailCallback call) {
		int total = 0;
		int lens = 0;
		for (int i = 1; i < 4; i++) {
			for (int j = 1; j < 4; j++) {
				for (int k = 1; k < 4; k++) {
					//ReikaJavaLibrary.pConsole(i+":"+j+":"+k);
					if (i == 2 && k == 2 && j == 3) {
						int dx = blocks.getMinX()+i;
						int dy = blocks.getMinY()+j;
						int dz = blocks.getMinZ()+k;
						if (ReactorTiles.getTE(blocks.world, dx, dy, dz) != ReactorTiles.MAGNETPIPE) {
							if (call != null)
								call.onBlockFailure(world, dx, dy, dz, new BlockKey(ReactorTiles.MAGNETPIPE));
							return false;
						}
					}
					else {
						BlockKey block = blocks.getBlockKeyRelativeToMinXYZ(i, j, k);
						int dx = blocks.getMinX()+i;
						int dy = blocks.getMinY()+j;
						int dz = blocks.getMinZ()+k;
						if (block == null) {
							MachineRegistry m = MachineRegistry.getMachine(blocks.world, dx, dy, dz);
							if (m == null || !m.isStandardPipe()) {
								if (call != null)
									call.onBlockFailure(world, dx, dy, dz, new BlockKey(MachineRegistry.PIPE));
								return false;
							}
						}
						else {
							Block id = block.blockID;
							int meta = block.metadata;
							if (i == 2 && j == 2 && k == 2) {
								if (id != ReactorTiles.HEATER.getBlock() || meta != ReactorTiles.HEATER) {
									if (call != null)
										call.onBlockFailure(world, dx, dy, dz, new BlockKey(ReactorTiles.HEATER));
									return false;
								}
							}
							else {
								if (id == this) {
									if (meta > 1) {
										if (call != null)
											call.onBlockFailure(world, dx, dy, dz, new BlockKey(this, 1));
										return false;
									}
									else
										total++;
									if (meta == 0)
										lens++;
								}
								else if (MachineRegistry.getMachineFromIDandMetadata(id, meta) != null && MachineRegistry.getMachineFromIDandMetadata(id, meta).isStandardPipe()) {
									if (j != 2) {
										if (call != null)
											call.onBlockFailure(world, dx, dy, dz, new BlockKey(this));
										return false;
									}
								}
								else {
									if (call != null)
										call.onBlockFailure(world, dx, dy, dz, new BlockKey(this));
									return false;
								}
							}
						}
					}
				}
			}
		}
		return total >= 22 && lens == 1;
	}

	private boolean checkFaces(World world, int x, int y, int z, StructuredBlockArray blocks, BlockMatchFailCallback call) {
		for (int i = 1; i < 4; i++) {
			for (int k = 1; k < 4; k++) {

				if (i == 2 && k == 2) {
					int dx = blocks.getMinX()+i;
					int dy = blocks.getMinY()+4;
					int dz = blocks.getMinZ()+k;
					if (ReactorTiles.getTE(blocks.world, dx, dy, dz) != ReactorTiles.MAGNETPIPE) {
						if (call != null)
							call.onBlockFailure(world, dx, dy, dz, new BlockKey(ReactorTiles.MAGNETPIPE));
						return false;
					}
				}
				else {
					BlockKey block = blocks.getBlockKeyRelativeToMinXYZ(i, 0, k);
					if (block == null || block.blockID != this || block.metadata != 4) {
						if (call != null)
							call.onBlockFailure(world, blocks.getMinX()+i, blocks.getMinY()+0, blocks.getMinZ()+k, new BlockKey(this, 4));
						return false;
					}

					block = blocks.getBlockKeyRelativeToMinXYZ(i, 4, k);
					if (block == null || block.blockID != this || block.metadata != 1) {
						if (call != null)
							call.onBlockFailure(world, blocks.getMinX()+i, blocks.getMinY()+4, blocks.getMinZ()+k, new BlockKey(this, 1));
						return false;
					}

					block = blocks.getBlockKeyRelativeToMinXYZ(i, 5, k);
					int meta2 = (i == 2 || k == 2) ? 3 : 2;
					if (block == null || block.blockID != this || block.metadata != meta2) {
						if (call != null)
							call.onBlockFailure(world, blocks.getMinX()+i, blocks.getMinY()+5, blocks.getMinZ()+k, new BlockKey(this, meta2));
						return false;
					}

					block = blocks.getBlockKeyRelativeToMinXYZ(i, k, 0);
					if (block == null || block.blockID != this || block.metadata != 4) {
						if (call != null)
							call.onBlockFailure(world, blocks.getMinX()+i, blocks.getMinY()+k, blocks.getMinZ()+0, new BlockKey(this, 4));
						return false;
					}

					block = blocks.getBlockKeyRelativeToMinXYZ(i, k, 4);
					if (block == null || block.blockID != this || block.metadata != 4) {
						if (call != null)
							call.onBlockFailure(world, blocks.getMinX()+i, blocks.getMinY()+k, blocks.getMinZ()+4, new BlockKey(this, 4));
						return false;
					}

					block = blocks.getBlockKeyRelativeToMinXYZ(0, k, i);
					if (block == null || block.blockID != this || block.metadata != 4) {
						if (call != null)
							call.onBlockFailure(world, blocks.getMinX()+0, blocks.getMinY()+k, blocks.getMinZ()+i, new BlockKey(this, 4));
						return false;
					}

					block = blocks.getBlockKeyRelativeToMinXYZ(4, k, i);
					if (block == null || block.blockID != this || block.metadata != 4) {
						if (call != null)
							call.onBlockFailure(world, blocks.getMinX()+4, blocks.getMinY()+k, blocks.getMinZ()+i, new BlockKey(this, 4));
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean checkEdges(World world, int x, int y, int z, StructuredBlockArray blocks, BlockMatchFailCallback call) {
		for (int i = 1; i < 4; i++) {
			BlockKey block = blocks.getBlockKeyRelativeToMinXYZ(i, 0, 0);
			if (block == null || block.blockID != this || block.metadata != 3) {
				if (call != null)
					call.onBlockFailure(world, blocks.getMinX()+i, blocks.getMinY()+0, blocks.getMinZ()+0, new BlockKey(this, 3));
				return false;
			}

			block = blocks.getBlockKeyRelativeToMinXYZ(0, i, 0);
			if (block == null || block.blockID != this || block.metadata != 3) {
				if (call != null)
					call.onBlockFailure(world, blocks.getMinX()+0, blocks.getMinY()+i, blocks.getMinZ()+0, new BlockKey(this, 3));
				return false;
			}

			block = blocks.getBlockKeyRelativeToMinXYZ(0, 0, i);
			if (block == null || block.blockID != this || block.metadata != 3) {
				if (call != null)
					call.onBlockFailure(world, blocks.getMinX()+0, blocks.getMinY()+0, blocks.getMinZ()+i, new BlockKey(this, 3));
				return false;
			}

			block = blocks.getBlockKeyRelativeToMinXYZ(i, 0, 4);
			if (block == null || block.blockID != this || block.metadata != 3) {
				if (call != null)
					call.onBlockFailure(world, blocks.getMinX()+i, blocks.getMinY()+0, blocks.getMinZ()+4, new BlockKey(this, 3));
				return false;
			}

			block = blocks.getBlockKeyRelativeToMinXYZ(4, 0, i);
			if (block == null || block.blockID != this || block.metadata != 3) {
				if (call != null)
					call.onBlockFailure(world, blocks.getMinX()+4, blocks.getMinY()+0, blocks.getMinZ()+i, new BlockKey(this, 3));
				return false;
			}

			block = blocks.getBlockKeyRelativeToMinXYZ(i, 4, 4);
			if (block == null || block.blockID != this || block.metadata != 3) {
				if (call != null)
					call.onBlockFailure(world, blocks.getMinX()+i, blocks.getMinY()+4, blocks.getMinZ()+4, new BlockKey(this, 3));
				return false;
			}

			block = blocks.getBlockKeyRelativeToMinXYZ(4, 4, i);
			if (block == null || block.blockID != this || block.metadata != 3) {
				if (call != null)
					call.onBlockFailure(world, blocks.getMinX()+4, blocks.getMinY()+4, blocks.getMinZ()+i, new BlockKey(this, 3));
				return false;
			}

			block = blocks.getBlockKeyRelativeToMinXYZ(i, 4, 0);
			if (block == null || block.blockID != this || block.metadata != 3) {
				if (call != null)
					call.onBlockFailure(world, blocks.getMinX()+i, blocks.getMinY()+4, blocks.getMinZ()+0, new BlockKey(this, 3));
				return false;
			}

			block = blocks.getBlockKeyRelativeToMinXYZ(0, 4, i);
			if (block == null || block.blockID != this || block.metadata != 3) {
				if (call != null)
					call.onBlockFailure(world, blocks.getMinX()+0, blocks.getMinY()+4, blocks.getMinZ()+i, new BlockKey(this, 3));
				return false;
			}

			block = blocks.getBlockKeyRelativeToMinXYZ(4, i, 0);
			if (block == null || block.blockID != this || block.metadata != 3) {
				if (call != null)
					call.onBlockFailure(world, blocks.getMinX()+4, blocks.getMinY()+i, blocks.getMinZ()+0, new BlockKey(this, 3));
				return false;
			}

			block = blocks.getBlockKeyRelativeToMinXYZ(0, i, 4);
			if (block == null || block.blockID != this || block.metadata != 3) {
				if (call != null)
					call.onBlockFailure(world, blocks.getMinX()+0, blocks.getMinY()+i, blocks.getMinZ()+4, new BlockKey(this, 3));
				return false;
			}

			block = blocks.getBlockKeyRelativeToMinXYZ(4, i, 4);
			if (block == null || block.blockID != this || block.metadata != 3) {
				if (call != null)
					call.onBlockFailure(world, blocks.getMinX()+4, blocks.getMinY()+i, blocks.getMinZ()+0, new BlockKey(this, 4));
				return false;
			}
		}
		return true;
	}

	private boolean checkCorners(World world, int x, int y, int z, StructuredBlockArray blocks, BlockMatchFailCallback call) {
		BlockKey block = blocks.getBlockKeyRelativeToMinXYZ(0, 0, 0);
		//ReikaJavaLibrary.pConsole(block.getMinX()+", "+block.getMinY()+", "+block.getMinZ());
		if (block == null || block.blockID != this || block.metadata != 2) {
			if (call != null)
				call.onBlockFailure(world, blocks.getMinX()+0, blocks.getMinY()+0, blocks.getMinZ()+0, new BlockKey(this, 2));
			return false;
		}
		block = blocks.getBlockKeyRelativeToMinXYZ(4, 0, 0);
		if (block == null || block.blockID != this || block.metadata != 2) {
			if (call != null)
				call.onBlockFailure(world, blocks.getMinX()+4, blocks.getMinY()+0, blocks.getMinZ()+0, new BlockKey(this, 2));
			return false;
		}
		block = blocks.getBlockKeyRelativeToMinXYZ(0, 0, 4);
		if (block == null || block.blockID != this || block.metadata != 2) {
			if (call != null)
				call.onBlockFailure(world, blocks.getMinX()+0, blocks.getMinY()+0, blocks.getMinZ()+4, new BlockKey(this, 2));
			return false;
		}
		block = blocks.getBlockKeyRelativeToMinXYZ(4, 0, 4);
		if (block == null || block.blockID != this || block.metadata != 2) {
			if (call != null)
				call.onBlockFailure(world, blocks.getMinX()+4, blocks.getMinY()+0, blocks.getMinZ()+4, new BlockKey(this, 2));
			return false;
		}
		block = blocks.getBlockKeyRelativeToMinXYZ(0, 4, 0);
		if (block == null || block.blockID != this || block.metadata != 2) {
			if (call != null)
				call.onBlockFailure(world, blocks.getMinX()+0, blocks.getMinY()+4, blocks.getMinZ()+0, new BlockKey(this, 2));
			return false;
		}
		block = blocks.getBlockKeyRelativeToMinXYZ(4, 4, 0);
		if (block == null || block.blockID != this || block.metadata != 2) {
			if (call != null)
				call.onBlockFailure(world, blocks.getMinX()+4, blocks.getMinY()+4, blocks.getMinZ()+0, new BlockKey(this, 2));
			return false;
		}
		block = blocks.getBlockKeyRelativeToMinXYZ(0, 4, 4);
		if (block == null || block.blockID != this || block.metadata != 2) {
			if (call != null)
				call.onBlockFailure(world, blocks.getMinX()+0, blocks.getMinY()+4, blocks.getMinZ()+4, new BlockKey(this, 2));
			return false;
		}
		block = blocks.getBlockKeyRelativeToMinXYZ(4, 4, 4);
		if (block == null || block.blockID != this || block.metadata != 2) {
			if (call != null)
				call.onBlockFailure(world, blocks.getMinX()+4, blocks.getMinY()+4, blocks.getMinZ()+4, new BlockKey(this, 2));
			return false;
		}

		return true;
	}

	@Override
	public void onCreateFullMultiBlock(World world, int x, int y, int z, Boolean complete) {
		BlockArray blocks = new BlockArray();
		blocks.recursiveAddWithBounds(world, x, y, z, this, x-6, y-6, z-6, x+6, y+6, z+6);
		for (int i = 0; i < blocks.getSize(); i++) {
			Coordinate c = blocks.getNthBlock(i);
			int meta = c.getBlockMetadata(world);
			if (meta < 8) {
				world.setBlockMetadataWithNotify(c.xCoord, c.yCoord, c.zCoord, meta+8, 3);
			}
			if (meta == 0) {
				for (int k = 2; k < 6; k++) {
					Direction dir = dirs[k];
					int dx = c.xCoord+dir.offsetX;
					int dy = c.yCoord+dir.offsetY;
					int dz = c.zCoord+dir.offsetZ;
					//ReikaJavaLibrary.pConsole(world.getBlock(dx, dy, dz)+":"+world.getBlockMetadata(dx, dy, dz)+" from "+Arrays.toString(xyz));
					if (ReactorTiles.getTE(world, dx, dy, dz) == ReactorTiles.HEATER) {
						TileEntityFusionHeater te = (TileEntityFusionHeater)world.getBlockEntity(dx, dy, dz);
						te.setHasMultiBlock(true);
					}
				}
			}
		}
	}

	@Override
	public void breakMultiBlock(World world, int x, int y, int z) {
		BlockArray blocks = new BlockArray();
		blocks.recursiveAddWithBounds(world, x, y, z, this, x-6, y-6, z-6, x+6, y+6, z+6);
		for (int i = 0; i < blocks.getSize(); i++) {
			Coordinate c = blocks.getNthBlock(i);
			int meta = c.getBlockMetadata(world);
			world.setBlockMetadataWithNotify(c.xCoord, c.yCoord, c.zCoord, meta&7, 3);
			if (meta == 8) {
				for (int k = 2; k < 6; k++) {
					Direction dir = dirs[k];
					int dx = c.xCoord+dir.offsetX;
					int dy = c.yCoord+dir.offsetY;
					int dz = c.zCoord+dir.offsetZ;
					//ReikaJavaLibrary.pConsole(world.getBlock(dx, dy, dz)+":"+world.getBlockMetadata(dx, dy, dz)+" from "+Arrays.toString(xyz));
					if (ReactorTiles.getTE(world, dx, dy, dz) == ReactorTiles.HEATER) {
						TileEntityFusionHeater te = (TileEntityFusionHeater)world.getBlockEntity(dx, dy, dz);
						te.setHasMultiBlock(false);
					}
				}
			}
		}
	}

	@Override
	protected String getIconBaseName() {
		return "heater";
	}

	@Override
	public boolean isOpaque(int meta) {
		return meta != 0 && meta != 8;
	}

	@Override
	public int getTextureIndex(BlockGetter world, int x, int y, int z, int side, int meta) {
		if ((meta&7) <= 1)
			return meta&7;
		if (meta == 12)
			return 10;
		if (meta == 2)
			return 11;
		if (meta == 3)
			return 12;
		if (meta == 4)
			return 10;
		if (meta == 10) {
			switch(side) {
				case 0:
					if (world.getBlock(x+1, y, z+1) == this && world.getBlockMetadata(x+1, y, z+1) == 12)
						return 2;
					if (world.getBlock(x-1, y, z+1) == this && world.getBlockMetadata(x-1, y, z+1) == 12)
						return 3;
					if (world.getBlock(x+1, y, z-1) == this && world.getBlockMetadata(x+1, y, z-1) == 12)
						return 5;
					if (world.getBlock(x-1, y, z-1) == this && world.getBlockMetadata(x-1, y, z-1) == 12)
						return 4;
					break;
				case 1:
					if (world.getBlock(x+1, y, z+1) == this && world.getBlockMetadata(x+1, y, z+1) == 9)
						return 2;
					if (world.getBlock(x-1, y, z+1) == this && world.getBlockMetadata(x-1, y, z+1) == 9)
						return 3;
					if (world.getBlock(x+1, y, z-1) == this && world.getBlockMetadata(x+1, y, z-1) == 9)
						return 5;
					if (world.getBlock(x-1, y, z-1) == this && world.getBlockMetadata(x-1, y, z-1) == 9)
						return 4;

					if (world.getBlock(x, y-1, z) == this && world.getBlockMetadata(x, y-1, z) == 9) {
						Block did = ReactorTiles.MAGNETPIPE.getBlock();
						int dmeta = ReactorTiles.MAGNETPIPE;
						if (world.getBlock(x+1, y, z+1) == did && world.getBlockMetadata(x+1, y, z+1) == dmeta && world.getBlock(x, y, z+1) == this)
							return 2;
						if (world.getBlock(x-1, y, z+1) == did && world.getBlockMetadata(x-1, y, z+1) == dmeta && world.getBlock(x, y, z+1) == this)
							return 3;
						if (world.getBlock(x+1, y, z-1) == did && world.getBlockMetadata(x+1, y, z-1) == dmeta && world.getBlock(x, y, z-1) == this)
							return 5;
						if (world.getBlock(x-1, y, z-1) == did && world.getBlockMetadata(x-1, y, z-1) == dmeta && world.getBlock(x, y, z-1) == this)
							return 4;
					}
					break;
				case 2:
					if (world.getBlock(x+1, y+1, z) == this && world.getBlockMetadata(x+1, y+1, z) == 12)
						return 4;
					if (world.getBlock(x-1, y+1, z) == this && world.getBlockMetadata(x-1, y+1, z) == 12)
						return 5;
					if (world.getBlock(x+1, y-1, z) == this && world.getBlockMetadata(x+1, y-1, z) == 12)
						return 3;
					if (world.getBlock(x-1, y-1, z) == this && world.getBlockMetadata(x-1, y-1, z) == 12)
						return 2;

					if (world.getBlock(x, y-1, z) == this && world.getBlockMetadata(x, y-1, z) == 9) {
						Block did = ReactorTiles.MAGNETPIPE.getBlock();
						int dmeta = ReactorTiles.MAGNETPIPE;
						if (world.getBlock(x+1, y, z+1) == did && world.getBlockMetadata(x+1, y, z+1) == dmeta && world.getBlock(x, y, z+1) == this)
							return 3;
						if (world.getBlock(x-1, y, z+1) == did && world.getBlockMetadata(x-1, y, z+1) == dmeta && world.getBlock(x, y, z+1) == this)
							return 2;
					}
					break;
				case 3:
					if (world.getBlock(x+1, y+1, z) == this && world.getBlockMetadata(x+1, y+1, z) == 12)
						return 5;
					if (world.getBlock(x-1, y+1, z) == this && world.getBlockMetadata(x-1, y+1, z) == 12)
						return 4;
					if (world.getBlock(x+1, y-1, z) == this && world.getBlockMetadata(x+1, y-1, z) == 12)
						return 2;
					if (world.getBlock(x-1, y-1, z) == this && world.getBlockMetadata(x-1, y-1, z) == 12)
						return 3;

					if (world.getBlock(x, y-1, z) == this && world.getBlockMetadata(x, y-1, z) == 9) {
						Block did = ReactorTiles.MAGNETPIPE.getBlock();
						int dmeta = ReactorTiles.MAGNETPIPE;
						if (world.getBlock(x+1, y, z-1) == did && world.getBlockMetadata(x+1, y, z-1) == dmeta && world.getBlock(x, y, z-1) == this)
							return 2;
						if (world.getBlock(x-1, y, z-1) == did && world.getBlockMetadata(x-1, y, z-1) == dmeta && world.getBlock(x, y, z-1) == this)
							return 3;
					}
					break;
				case 4:
					if (world.getBlock(x, y+1, z+1) == this && world.getBlockMetadata(x, y+1, z+1) == 12)
						return 5;
					if (world.getBlock(x, y+1, z-1) == this && world.getBlockMetadata(x, y+1, z-1) == 12)
						return 4;
					if (world.getBlock(x, y-1, z+1) == this && world.getBlockMetadata(x, y-1, z+1) == 12)
						return 2;
					if (world.getBlock(x, y-1, z-1) == this && world.getBlockMetadata(x, y-1, z-1) == 12)
						return 3;

					if (world.getBlock(x, y-1, z) == this && world.getBlockMetadata(x, y-1, z) == 9) {
						Block did = ReactorTiles.MAGNETPIPE.getBlock();
						int dmeta = ReactorTiles.MAGNETPIPE;
						if (world.getBlock(x+1, y, z+1) == did && world.getBlockMetadata(x+1, y, z+1) == dmeta && world.getBlock(x, y, z+1) == this)
							return 2;
						if (world.getBlock(x+1, y, z-1) == did && world.getBlockMetadata(x+1, y, z-1) == dmeta && world.getBlock(x, y, z-1) == this)
							return 3;
					}
					break;
				case 5:
					if (world.getBlock(x, y+1, z+1) == this && world.getBlockMetadata(x, y+1, z+1) == 12)
						return 4;
					if (world.getBlock(x, y+1, z-1) == this && world.getBlockMetadata(x, y+1, z-1) == 12)
						return 5;
					if (world.getBlock(x, y-1, z+1) == this && world.getBlockMetadata(x, y-1, z+1) == 12)
						return 3;
					if (world.getBlock(x, y-1, z-1) == this && world.getBlockMetadata(x, y-1, z-1) == 12)
						return 2;

					if (world.getBlock(x, y-1, z) == this && world.getBlockMetadata(x, y-1, z) == 9) {
						Block did = ReactorTiles.MAGNETPIPE.getBlock();
						int dmeta = ReactorTiles.MAGNETPIPE;
						if (world.getBlock(x-1, y, z+1) == did && world.getBlockMetadata(x-1, y, z+1) == dmeta && world.getBlock(x, y, z+1) == this)
							return 3;
						if (world.getBlock(x-1, y, z-1) == did && world.getBlockMetadata(x-1, y, z-1) == dmeta && world.getBlock(x, y, z-1) == this)
							return 2;
					}
					break;
			}
		}
		if (meta == 11) {
			switch(side) {
				case 0:
					if (world.getBlock(x+1, y, z) == this && world.getBlockMetadata(x+1, y, z) == 12)
						return 8;
					if (world.getBlock(x-1, y, z) == this && world.getBlockMetadata(x-1, y, z) == 12)
						return 7;
					if (world.getBlock(x, y, z+1) == this && world.getBlockMetadata(x, y, z+1) == 12)
						return 9;
					if (world.getBlock(x, y, z-1) == this && world.getBlockMetadata(x, y, z-1) == 12)
						return 6;
					break;
				case 1:
					if (world.getBlock(x+1, y, z) == this && world.getBlockMetadata(x+1, y, z) == 9)
						return 8;
					if (world.getBlock(x-1, y, z) == this && world.getBlockMetadata(x-1, y, z) == 9)
						return 7;
					if (world.getBlock(x, y, z+1) == this && world.getBlockMetadata(x, y, z+1) == 9)
						return 9;
					if (world.getBlock(x, y, z-1) == this && world.getBlockMetadata(x, y, z-1) == 9)
						return 6;

					if (world.getBlock(x, y-1, z) == this && world.getBlockMetadata(x, y-1, z) == 9) {
						Block did = ReactorTiles.MAGNETPIPE.getBlock();
						int dmeta = ReactorTiles.MAGNETPIPE;
						if (world.getBlock(x+1, y, z) == did && world.getBlockMetadata(x+1, y, z) == dmeta && world.getBlock(x-1, y, z) == Blocks.air)
							return 8;
						if (world.getBlock(x-1, y, z) == did && world.getBlockMetadata(x-1, y, z) == dmeta && world.getBlock(x+1, y, z) == Blocks.air)
							return 7;
						if (world.getBlock(x, y, z+1) == did && world.getBlockMetadata(x, y, z+1) == dmeta && world.getBlock(x, y, z-1) == Blocks.air)
							return 9;
						if (world.getBlock(x, y, z-1) == did && world.getBlockMetadata(x, y, z-1) == dmeta && world.getBlock(x, y, z+1) == Blocks.air)
							return 6;
					}
					break;
				case 2:
					if (world.getBlock(x+1, y, z) == this && world.getBlockMetadata(x+1, y, z) == 12)
						return 7;
					if (world.getBlock(x-1, y, z) == this && world.getBlockMetadata(x-1, y, z) == 12)
						return 8;
					if (world.getBlock(x, y+1, z) == this && world.getBlockMetadata(x, y+1, z) == 12)
						return 6;
					if (world.getBlock(x, y-1, z) == this && world.getBlockMetadata(x, y-1, z) == 12)
						return 9;

					if (world.getBlock(x, y-1, z) == this && world.getBlockMetadata(x, y-1, z) == 9) {
						Block did = ReactorTiles.MAGNETPIPE.getBlock();
						int dmeta = ReactorTiles.MAGNETPIPE;
						if (world.getBlock(x, y, z+1) == did && world.getBlockMetadata(x, y, z+1) == dmeta)
							return 9;
					}
					break;
				case 3:
					if (world.getBlock(x+1, y, z) == this && world.getBlockMetadata(x+1, y, z) == 12)
						return 8;
					if (world.getBlock(x-1, y, z) == this && world.getBlockMetadata(x-1, y, z) == 12)
						return 7;
					if (world.getBlock(x, y+1, z) == this && world.getBlockMetadata(x, y+1, z) == 12)
						return 6;
					if (world.getBlock(x, y-1, z) == this && world.getBlockMetadata(x, y-1, z) == 12)
						return 9;

					if (world.getBlock(x, y-1, z) == this && world.getBlockMetadata(x, y-1, z) == 9) {
						Block did = ReactorTiles.MAGNETPIPE.getBlock();
						int dmeta = ReactorTiles.MAGNETPIPE;
						if (world.getBlock(x, y, z-1) == did && world.getBlockMetadata(x, y, z-1) == dmeta)
							return 9;
					}
					break;
				case 4:
					if (world.getBlock(x, y, z+1) == this && world.getBlockMetadata(x, y, z+1) == 12)
						return 8;
					if (world.getBlock(x, y, z-1) == this && world.getBlockMetadata(x, y, z-1) == 12)
						return 7;
					if (world.getBlock(x, y+1, z) == this && world.getBlockMetadata(x, y+1, z) == 12)
						return 6;
					if (world.getBlock(x, y-1, z) == this && world.getBlockMetadata(x, y-1, z) == 12)
						return 9;

					if (world.getBlock(x, y-1, z) == this && world.getBlockMetadata(x, y-1, z) == 9) {
						Block did = ReactorTiles.MAGNETPIPE.getBlock();
						int dmeta = ReactorTiles.MAGNETPIPE;
						if (world.getBlock(x+1, y, z) == did && world.getBlockMetadata(x+1, y, z) == dmeta)
							return 9;
					}
					break;
				case 5:
					if (world.getBlock(x, y, z+1) == this && world.getBlockMetadata(x, y, z+1) == 12)
						return 7;
					if (world.getBlock(x, y, z-1) == this && world.getBlockMetadata(x, y, z-1) == 12)
						return 8;
					if (world.getBlock(x, y+1, z) == this && world.getBlockMetadata(x, y+1, z) == 12)
						return 6;
					if (world.getBlock(x, y-1, z) == this && world.getBlockMetadata(x, y-1, z) == 12)
						return 9;

					if (world.getBlock(x, y-1, z) == this && world.getBlockMetadata(x, y-1, z) == 9) {
						Block did = ReactorTiles.MAGNETPIPE.getBlock();
						int dmeta = ReactorTiles.MAGNETPIPE;
						if (world.getBlock(x-1, y, z) == did && world.getBlockMetadata(x-1, y, z) == dmeta)
							return 9;
					}
					break;
			}
		}
		return this.getItemTextureIndex(meta, side);
	}

	@Override
	public int getItemTextureIndex(int meta, int side) {
		meta = meta&7;
		if (meta == 2)
			return 11;
		if (meta == 3)
			return 12;
		if (meta == 4)
			return 10;
		return meta <= 1 ? meta : 11;
	}

	@Override
	public boolean canTriggerMultiBlockCheck(World world, int x, int y, int z, int meta) {
		return true;
	}

	@Override
	public int getNumberTextures() {
		return 13;
	}

	@Override
	protected TileEntity getTileEntityForPosition(World world, int x, int y, int z) {
		StructuredBlockArray blocks = new StructuredBlockArray(world);
		Set<BlockKey> li = ReikaJavaLibrary.getSet(new BlockKey(this), new BlockKey(ReactorTiles.HEATER));
		blocks.recursiveAddMultipleWithBounds(world, x, y, z, li, x-6, y-6, z-6, x+6, y+6, z+6);
		int mx = blocks.getMidX();
		int my = blocks.getMidY()-1;
		int mz = blocks.getMidZ();
		return ReactorTiles.getTE(world, mx, my, mz) == ReactorTiles.HEATER ? world.getBlockEntity(mx, my, mz) : null;
	}

}
