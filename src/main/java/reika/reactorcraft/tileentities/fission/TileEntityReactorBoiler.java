/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.fission;
import net.minecraft.core.BlockPos;

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.world.level.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidRegistry;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.instantiable.data.blockstruct.BlockArray;
import reika.dragonapi.instantiable.data.immutable.Coordinate;
import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.dragonapi.libraries.registry.ReikaParticleHelper;
import reika.dragonapi.libraries.rendering.ReikaRenderHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.auxiliary.SteamTile;
import reika.reactorcraft.base.TileEntityNuclearBoiler;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.entities.EntityNeutron.NeutronSpeed;
import reika.reactorcraft.entities.EntityNeutron.NeutronType;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.reactorcraft.registry.WorkingFluid;
import reika.rotarycraft.auxiliary.ItemStacks;
import reika.rotarycraft.registry.MachineRegistry;


public class TileEntityReactorBoiler extends TileEntityNuclearBoiler implements SteamTile {
	public TileEntityReactorBoiler(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.BOILER.get(), pos, state);
	}


	public static final int WATER_PER_STEAM = 200;
	public static final int DETTEMP = 650;

	private WorkingFluid fluid = WorkingFluid.EMPTY;

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.BOILER;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		super.updateEntity(world, pos);
		if (temperature >= DETTEMP && fluid == WorkingFluid.AMMONIA)
			this.detonateAmmonia(world, x, y, z);

		if (tank.getFluidLevel() >= WATER_PER_STEAM && temperature > 100 && this.canBoilTankLiquid()) {
			steam++;
			fluid = WorkingFluid.getWorkingFluid(tank.getActualFluid());
			if (fluid == WorkingFluid.AMMONIA) {
				ReactorAchievements.AMMONIA.triggerAchievement(this.getPlacer());
			}
			tank.removeLiquid(WATER_PER_STEAM);
			temperature -= 5;
		}

		if (DragonAPI.debugtest) {
			tank.addLiquid(2500, FluidRegistry.WATER);
			temperature = 120;
		}

		if (steam <= 0) {
			fluid = WorkingFluid.EMPTY;
		}

		//ReikaJavaLibrary.pConsole(y+":"+steam+":"+temperature+":"+fluid.name()+":"+tank, Dist.DEDICATED_SERVER);

		//ReikaJavaLibrary.pConsole("T: "+temperature+"    W: "+tank.getFluidLevel()+"    S: "+steam, Dist.DEDICATED_SERVER);

		this.transferSteam(world, x, y, z);
	}

	private void detonateAmmonia(Level world, int x, int y, int z) {
		ReactorAchievements.NH3EXPLODE.triggerAchievement(this.getPlacer());
		BlockArray pipes = new BlockArray();
		Block id = ReactorTiles.STEAMLINE.getBlock();
		int meta = ReactorTiles.STEAMLINE;
		pipes.recursiveAddWithMetadata(world, x, y+1, z, id, meta);
		for (int i = 0; i < pipes.getSize(); i++) {
			Coordinate c = pipes.getNthBlock(i);
			c.setBlock(world, Blocks.air);
			ReikaParticleHelper.EXPLODE.spawnAt(world, c.xCoord, c.yCoord, c.zCoord);
			ReikaItemHelper.dropItem(world, c.xCoord, c.yCoord, c.zCoord, new ItemStack(Items.netherbrick));
		}
		world.removeBlock(x, y, z);
		ReikaItemHelper.dropItem(world, x, y, z, ReikaItemHelper.getSizedItemStack(ItemStacks.scrap, 8+rand.nextInt(18)));
		ReikaParticleHelper.EXPLODE.spawnAt(world, x, y, z);
		ReikaSoundHelper.playSoundAtBlock(world, x, y, z, "random.explode", 1.2F, 1);
		boolean flag = false;
		int r = 8;
		for (int i = -r; i <= r; i++) {
			for (int j = -r; j <= r; j++) {
				for (int k = -r; k <= r; k++) {
					Block id2 = world.getBlock(x+i, y+j, z+k);
					int meta2 = world.getBlockMetadata(x+i, y+j, z+k);
					if (id2 != Blocks.air && id2.getMaterial() == Material.glass) {
						id2.dropBlockAsItem(world, x+i, y+j, z+k, meta2, 0);
						world.removeBlock(x+i, y+j, z+k);
						if (FMLEnvironment.dist == Dist.CLIENT)
							ReikaRenderHelper.spawnDropParticles(world, x, y, z, id2, meta2);
						flag = true;
					}
				}
			}
		}
		if (flag)
			ReikaSoundHelper.playSoundAtBlock(world, x, y, z, "random.glass");
	}

	protected void transferSteam(Level world, int x, int y, int z) {
		ReactorTiles r = ReactorTiles.getTE(world, x, y+1, z);
		if (r == ReactorTiles.BOILER) {
			TileEntityReactorBoiler te = (TileEntityReactorBoiler)world.getBlockEntity(x, y+1, z);
			if (steam > 0 && fluid != WorkingFluid.EMPTY) {
				if (te.fluid == WorkingFluid.EMPTY || te.fluid == fluid) {
					te.fluid = fluid;
					te.steam += steam;
					steam = 0;
				}
			}
		}
	}

	private boolean canBoilTankLiquid() {
		if (WorkingFluid.getWorkingFluid(tank.getActualFluid()) == null)
			return false;
		int Tamb = ReikaWorldHelper.getAmbientTemperatureAt(level, xCoord, yCoord, zCoord);
		if (temperature < Tamb+50)
			return false;
		return fluid == WorkingFluid.EMPTY || tank.getActualFluid().equals(fluid.getFluid());
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m.isStandardPipe();
	}

	@Override
	public int getCapacity() {
		return 12000;
	}

	@Override
	public boolean isValidFluid(Fluid f) {
		return WorkingFluid.getWorkingFluid(f) != ItemStack.EMPTY;
	}

	@Override
	public int getMaxTemperature() {
		return 2000;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		fluid = WorkingFluid.getFromNBT(NBT);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		fluid.saveToNBT(NBT);
	}

	public WorkingFluid getWorkingFluid() {
		return fluid;
	}

	@Override
	public Fluid getInputFluid() {
		return null;
	}

	@Override
	protected void overheat(Level world, int x, int y, int z) {
		world.explode(/*PORT*/null, x+0.5, y+0.5, z+0.5, 8, true);
		for (int i = 0; i < 4; i++)
			ReikaItemHelper.dropItem(world, x+rand.nextDouble(), y+rand.nextDouble(), z+rand.nextDouble(), ItemStacks.scrap);
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		NeutronType type = e.getType();
		if (!tank.isEmpty()) {
			if (tank.getActualFluid() == ReactorFluids.getLegacyFluid("rc heavy water")) {
				e.moderate();
			}
			else {
				if (e.getNeutronSpeed() == NeutronSpeed.FAST && rand.nextInt(10) == 0)
					return true;
			}
			return ReikaRandomHelper.doWithChance(type.getBoilerAbsorptionChance());
		}
		return false;
	}

	@Override
	public int getSteam() {
		return steam;
	}

	@Override
	public ReactorType getDefaultReactorType() {
		return ReactorType.FISSION;
	}

}
