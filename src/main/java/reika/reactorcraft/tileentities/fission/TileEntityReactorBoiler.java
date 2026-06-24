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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.instantiable.data.blockstruct.BlockArray;
import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.dragonapi.libraries.registry.ReikaParticleHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.auxiliary.SteamTile;
import reika.reactorcraft.base.TileEntityNuclearBoiler;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.entities.EntityNeutron.NeutronSpeed;
import reika.reactorcraft.entities.EntityNeutron.NeutronType;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.reactorcraft.registry.WorkingFluid;
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
			this.detonateAmmonia(world, pos);

		if (tank.getFluidLevel() >= WATER_PER_STEAM && temperature > 100 && this.canBoilTankLiquid()) {
			steam++;
			fluid = WorkingFluid.getWorkingFluid(tank.getActualFluid().getFluid());
			if (fluid == WorkingFluid.AMMONIA) {
				ReactorAchievements.AMMONIA.triggerAchievement(this.getPlacer());
			}
			tank.removeLiquid(WATER_PER_STEAM);
			temperature -= 5;
		}

		if (DragonAPI.debugtest) {
			tank.addLiquid(2500, Fluids.WATER);
			temperature = 120;
		}

		if (steam <= 0) {
			fluid = WorkingFluid.EMPTY;
		}

		this.transferSteam(world, pos);
	}

	private void detonateAmmonia(Level world, BlockPos pos) {
		ReactorAchievements.NH3EXPLODE.triggerAchievement(this.getPlacer());
		BlockArray pipes = new BlockArray();
		Block id = ReactorTiles.STEAMLINE.getBlock();
		pipes.recursiveAdd(world, pos.above(), id);
		for (int i = 0; i < pipes.getSize(); i++) {
			BlockPos c = pipes.getNthBlock(i);
			world.removeBlock(c, false);
			ReikaParticleHelper.EXPLODE.spawnAt(world, c.getX(), c.getY(), c.getZ());
			ReikaItemHelper.dropItem(world, c.getX(), c.getY(), c.getZ(), new ItemStack(Items.NETHER_BRICK));
		}
		world.removeBlock(pos, false);
		// MOD-PORT: scrap drop (RotaryCraft ItemStacks.scrap) restored when that item is ported.
		ReikaParticleHelper.EXPLODE.spawnAt(world, pos.getX(), pos.getY(), pos.getZ());
		ReikaSoundHelper.playSoundAtBlock(world, pos, SoundEvents.GENERIC_EXPLODE.value());
		boolean flag = false;
		int r = 8;
		for (int i = -r; i <= r; i++) {
			for (int j = -r; j <= r; j++) {
				for (int k = -r; k <= r; k++) {
					BlockPos p = pos.offset(i, j, k);
					if (world.getBlockState(p).is(Blocks.GLASS)) {
						world.destroyBlock(p, true);
						flag = true;
					}
				}
			}
		}
		if (flag)
			ReikaSoundHelper.playSoundAtBlock(world, pos, SoundEvents.GLASS_BREAK);
	}

	protected void transferSteam(Level world, BlockPos pos) {
		BlockPos above = pos.above();
		if (ReactorTiles.getTE(world, above) == ReactorTiles.BOILER) {
			TileEntityReactorBoiler te = (TileEntityReactorBoiler) world.getBlockEntity(above);
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
		if (WorkingFluid.getWorkingFluid(tank.getActualFluid().getFluid()) == null)
			return false;
		int Tamb = ReikaWorldHelper.getAmbientTemperatureAt(level, this.getBlockPos());
		if (temperature < Tamb + 50)
			return false;
		return fluid == WorkingFluid.EMPTY || tank.getActualFluid().getFluid().equals(fluid.getFluid());
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
		return WorkingFluid.getWorkingFluid(f) != null;
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
	protected void overheat(Level world, BlockPos pos) {
		world.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, Level.ExplosionInteraction.BLOCK);
		// MOD-PORT: scrap drops (RotaryCraft ItemStacks.scrap) restored when that item is ported.
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		NeutronType type = e.getNeutronType();
		if (!tank.isEmpty()) {
			if (tank.getActualFluid().getFluid() == ReactorFluids.getLegacyFluid("rc heavy water")) {
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
