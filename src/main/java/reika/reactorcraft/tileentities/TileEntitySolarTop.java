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

import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import reika.dragonapi.instantiable.StepTimer;
import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.dragonapi.libraries.world.ReikaWorldHelper;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.auxiliary.interfaces.sodiumsolarupgrades.SodiumSolarReceiver;
import reika.rotarycraft.auxiliary.interfaces.TemperatureTE;
import reika.rotarycraft.tileentities.production.TileEntitySolar;


public class TileEntitySolarTop extends TileEntityReactorBase implements TemperatureTE, SodiumSolarReceiver {

	public static final int MAXTEMP = 1800;

	private final StepTimer tempTimer = new StepTimer(5);

	@Override
	public boolean isActive() {
		return ReactorTiles.getTE(worldObj, xCoord, yCoord+1, zCoord) == this.getTile() && this.getAdjacentTileEntity(ForgeDirection.DOWN) instanceof TileEntitySolar;
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.SOLARTOP;
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		if (this.isActive()) {
			//tempTimer.update();
			if (!world.isRemote && this.getTicksExisted()%8 == 0) {
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
	protected void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	public int getTemperature() {
		if (this.isActive())
			return temperature;
		TileEntity te = this.getAdjacentTileEntity(ForgeDirection.DOWN);
		if (te instanceof TileEntitySolarTop)
			return ((TileEntitySolarTop)te).getTemperature();
		else
			return ReikaWorldHelper.getAmbientTemperatureAt(worldObj, xCoord, yCoord, zCoord);
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
	public void updateTemperature(World world, int x, int y, int z, int meta) {

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
	public void overheat(World world, int x, int y, int z) {
		ReikaSoundHelper.playSoundAtBlock(world, x, y, z, "random.fizz");
		world.setBlock(x, y, z, Blocks.lava);
	}

	@Override
	public boolean allowExternalHeating() {
		return false;
	}

	@Override
	public void tick(int mirrorCount, float totalBrightness) {
		if (!worldObj.isRemote) {
			temperature += (0.0625*2*mirrorCount*totalBrightness);
			temperature = Math.min(temperature, MAXTEMP);
		}
	}

}
