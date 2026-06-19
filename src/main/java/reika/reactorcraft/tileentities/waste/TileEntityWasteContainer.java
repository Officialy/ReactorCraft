/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.waste;

import net.minecraft.world.level.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.ForgeDirection;

import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.dragonapi.libraries.mathsci.Isotopes;
import reika.dragonapi.libraries.mathsci.ReikaNuclearHelper;
import reika.dragonapi.libraries.mathsci.ReikaThermoHelper;
import reika.dragonapi.libraries.world.ReikaWorldHelper;
import reika.reactorcraft.auxiliary.Feedable;
import reika.reactorcraft.auxiliary.NeutronTile;
import reika.reactorcraft.auxiliary.RadiationEffects;
import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;
import reika.reactorcraft.base.TileEntityWasteUnit;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.auxiliary.interfaces.TemperatureTE;

public class TileEntityWasteContainer extends TileEntityWasteUnit implements TemperatureTE, Feedable, NeutronTile {

	public static final int WIDTH = 9;
	public static final int HEIGHT = 3;

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.WASTECONTAINER;
	}

	@Override
	public void updateEntity(Level world, int x, int y, int z, int meta) {
		thermalTicker.update();

		if (thermalTicker.checkCap()) {
			int waste = this.countWaste();
			temperature += waste*ReikaNuclearHelper.getWasteDecayHeat();
			this.updateTemperature(world, x, y, z, meta);
		}

		if (!world.isRemote)
			this.decayWaste();

		if (!world.isRemote)
			this.feed();

		//this.fill();
	}

	@Override
	protected boolean accountForOutGameTime() {
		return false;
	}

	private void distributeHeat(Level world, int x, int y, int z) {
		int Tamb = ReikaWorldHelper.getAmbientTemperatureAt(world, x, y, z);
		//ReikaJavaLibrary.pConsole(temperature);
		if (temperature > Tamb) {
			ForgeDirection side = ReikaWorldHelper.checkForAdjSourceBlock(world, x, y, z, Material.water);
			if (side != null) {
				temperature -= ReikaThermoHelper.getTemperatureIncrease(1, 15000, ReikaThermoHelper.WATER_BLOCK_HEAT);
				//ReikaJavaLibrary.pConsole(temperature);
				if (temperature > 100)
					ReikaWorldHelper.changeAdjBlock(world, x, y, z, side, Blocks.air, 0);
				else
					ReikaWorldHelper.changeAdjBlock(world, x, y, z, side, Blocks.flowing_water, 6);
			}
		}
		//ReikaJavaLibrary.pConsole(temperature);
		if (temperature < Tamb)
			temperature = Tamb;
		if (temperature > this.getMaxTemperature()) {
			this.overheat(world, x, y, z);
		}
		else if (temperature > this.getMaxTemperature()/2 && rand.nextInt(6) == 0) {
			world.spawnParticle("smoke", x+rand.nextDouble(), y+1, z+rand.nextDouble(), 0, 0, 0);
			ReikaSoundHelper.playSoundAtBlock(world, x, y, z, "random.fizz");
		}
		else if (temperature > this.getMaxTemperature()/4 && rand.nextInt(20) == 0) {
			world.spawnParticle("smoke", x+rand.nextDouble(), y+1, z+rand.nextDouble(), 0, 0, 0);
			ReikaSoundHelper.playSoundAtBlock(world, x, y, z, "random.fizz");
		}
	}

	@Override
	protected void animateWithTick(Level world, int x, int y, int z) {

	}

	@Override
	public boolean canRemoveItem(int i, ItemStack itemstack) {
		return this.isLongLivedWaste(itemstack);
	}

	@Override
	public int getSizeInventory() {
		return WIDTH*HEIGHT;
	}

	@Override
	public int getTemperature() {
		return temperature;
	}

	@Override
	public void addTemperature(int T) {
		temperature += T;
	}

	public int getMaxTemperature() {
		return 600;
	}

	public void onMeltdown(Level world, int x, int y, int z) {
		world.createExplosion(null, x+0.5, y+0.5, z+0.5, 9, true);
		RadiationEffects.instance.contaminateArea(world, x, y, z, 9, 4, 1.5, true, RadiationIntensity.LETHAL);
		ReactorAchievements.WASTELEAK.triggerAchievement(this.getPlacer());
	}

	@Override
	public void updateTemperature(Level world, int x, int y, int z, int meta) {
		this.distributeHeat(world, x, y, z);
	}

	@Override
	public int getThermalDamage() {
		return temperature/100;
	}

	@Override
	public void overheat(Level world, int x, int y, int z) {
		if (!world.isRemote)
			this.onMeltdown(world, x, y, z);
	}

	@Override
	public boolean leaksRadiation() {
		return true;
	}

	@Override
	public boolean isValidIsotope(Isotopes i) {
		return !this.isLongLivedWaste(i);
	}

	public boolean feed() {
		Level world = worldObj;
		int x = xCoord;
		int y = yCoord;
		int z = zCoord;
		Block id = world.getBlock(x, y-1, z);
		int meta = world.getBlockMetadata(x, y-1, z);
		BlockEntity tile = this.getAdjacentTileEntity(ForgeDirection.DOWN);
		if (tile instanceof TileEntityWasteContainer) {
			if (((Feedable)tile).feedIn(inv[inv.length-1])) {
				for (int i = inv.length-1; i > 0; i--)
					inv[i] = inv[i-1];

				id = world.getBlock(x, y+1, z);
				meta = world.getBlockMetadata(x, y+1, z);
				tile = this.getAdjacentTileEntity(ForgeDirection.UP);
				if (tile instanceof TileEntityWasteContainer) {
					inv[0] = ((Feedable) tile).feedOut();
				}
				else
					inv[0] = null;
			}
		}
		this.collapseInventory();
		return false;
	}

	private void collapseInventory() {
		for (int i = 0; i < inv.length; i++) {
			for (int k = inv.length-1; k > 0; k--) {
				if (inv[k] == null) {
					inv[k] = inv[k-1];
					inv[k-1] = null;
				}
			}
		}
	}

	@Override
	public boolean feedIn(ItemStack is) {
		if (is == null)
			return true;
		if (!this.isItemValidForSlot(0, is))
			return false;
		if (inv[0] == null) {
			inv[0] = is.copy();
			return true;
		}
		return false;
	}

	@Override
	public ItemStack feedOut() {
		if (inv[inv.length-1] == null)
			return null;
		else {
			ItemStack is = inv[inv.length-1].copy();
			inv[inv.length-1] = null;
			return is;
		}
	}

	public void setTemperature(int temp) {
		temperature = temp;
	}

	@Override
	public boolean allowExternalHeating() {
		return false;
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, int x, int y, int z) {
		return false;
	}

	@Override
	protected boolean canBeAccelerated() {
		return false;
	}

	@Override
	protected double getBaseDecayRate() {
		return 1;
	}

}
