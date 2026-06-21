/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities;

import net.minecraft.world.level.block.state.BlockState;
import reika.reactorcraft.registry.ReactorBlockEntities;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

import reika.dragonapi.instantiable.StepTimer;
import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.auxiliary.interfaces.sodiumsolarupgrades.SodiumSolarReceiver;
import reika.rotarycraft.auxiliary.interfaces.TemperatureTE;
import reika.rotarycraft.tileentities.production.TileEntitySolar;


public class TileEntitySolarTop extends TileEntityReactorBase implements TemperatureTE, SodiumSolarReceiver {
	public TileEntitySolarTop(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.SOLARTOP.get(), pos, state);
	}


	public static final int MAXTEMP = 1800;

	private final StepTimer tempTimer = new StepTimer(5);

	@Override
	public boolean isActive() {
		return ReactorTiles.getTE(level, xCoord, yCoord+1, zCoord) == this.getTile() && this.getAdjacentTileEntity(Direction.DOWN) instanceof TileEntitySolar;
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.SOLARTOP;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		if (this.isActive()) {
			//tempTimer.update();
			if (!world.isClientSide() && this.getTicksExisted()%8 == 0) {
				int Tamb = ReikaWorldHelper.getAmbientTemperatureAt(world, x, y, z);
				int dT = Tamb-temperature;
				if (dT != 0) {
					int d = 16;
					int diff = (1+dT/d);
					if (Math.abs(diff) <= 1)
						diff = dT/Math.abs(dT);
					temperature += diff;
				}
			}
		}
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	public int getTemperature() {
		if (this.isActive())
			return temperature;
		BlockEntity te = this.getAdjacentTileEntity(Direction.DOWN);
		if (te instanceof TileEntitySolarTop)
			return ((TileEntitySolarTop)te).getTemperature();
		else
			return ReikaWorldHelper.getAmbientTemperatureAt(level, xCoord, yCoord, zCoord);
	}

	@Override
	public void setTemperature(int temp) {
		temperature = temp;
	}

	@Override
	public int getMaxTemperature() {
		return MAXTEMP;
	}

	@Override
	public void updateTemperature(Level world, int x, int y, int z, int meta) {

	}

	@Override
	public void addTemperature(int temp) {
		temperature += temp;
	}

	@Override
	public int getThermalDamage() {
		return 10;
	}

	@Override
	public void overheat(Level world, int x, int y, int z) {
		ReikaSoundHelper.playSoundAtBlock(world, x, y, z, "random.fizz");
		world.setBlock(x, y, z, Blocks.lava);
	}

	@Override
	public boolean allowExternalHeating() {
		return false;
	}

	@Override
	public void tick(int mirrorCount, float totalBrightness) {
		if (!level.isRemote) {
			temperature += (0.0625*2*mirrorCount*totalBrightness);
			temperature = Math.min(temperature, MAXTEMP);
		}
	}

}
