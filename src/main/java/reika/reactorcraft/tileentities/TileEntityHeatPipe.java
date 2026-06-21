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

import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

// CHROMA-PORT: import reika.chromaticraft.api.interfaces.WorldRift;
import reika.dragonapi.instantiable.data.Proportionality;
import reika.dragonapi.libraries.mathsci.ReikaEngLibrary;
import reika.dragonapi.libraries.mathsci.ReikaThermoHelper;
import reika.dragonapi.libraries.rendering.ReikaColorAPI;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.auxiliary.ReactorTyped;
import reika.reactorcraft.base.TileEntityLine;
import reika.reactorcraft.base.TileEntityNuclearBoiler;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.rotarycraft.RotaryCraft;
import reika.rotarycraft.auxiliary.interfaces.HeatConduction;


public class TileEntityHeatPipe extends TileEntityLine {
	public TileEntityHeatPipe(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.HEATPIPE.get(), pos, state);
	}


	private static final double HEAT_CAPACITY = ReikaThermoHelper.COPPER_HEAT*ReikaEngLibrary.rhoiron;

	private double heatEnergy;

	private Proportionality<ReactorType> reactorTypes = new Proportionality();

	private float renderBrightness;
	private float lastBrightness;
	private float brightnessDeltaSinceUpdate;
	private int lastUpdateTime;

	@Override
	public IIcon getTexture() {
		return Blocks.snow.getIcon(0, 0);
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.HEATPIPE;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		super.updateEntity(world, pos);

		if (!world.isClientSide()) {
			this.balanceHeat(world, x, y, z);

			if (this.getTicksExisted()%32 == 0) {
				//this.ventHeat(world, x, y, z); //TODO fix heat pipe heat loss
			}
		}
	}

	private void ventHeat(Level world, int x, int y, int z) {
		double temp = getTemperatureForPipe(this, false);
		int Tamb = ReikaWorldHelper.getAmbientTemperatureAt(world, x, y, z);
		if (temp >= Tamb) {
			temp -= (temp-Tamb)/96D;
			heatEnergy = HEAT_CAPACITY*temp;
		}
		temperature = (int)temp;
	}

	public double getNetHeatEnergy() {
		return heatEnergy-ReikaWorldHelper.getAmbientTemperatureAt(level, xCoord, yCoord, zCoord)*HEAT_CAPACITY;
	}

	public static double getNetTemperature(HeatConduction hc) {
		return hc.getTemperature()-hc.getAmbientTemperature();
	}

	public static double getNetHeat(HeatConduction hc) {
		return hc.heatEnergyPerDegree()*getNetTemperature(hc);
	}

	public static int getTemperatureForHeat(double heat, HeatConduction hc) {
		return (int)Math.max(1, heat/hc.heatEnergyPerDegree());
	}

	public static double getTemperatureForPipe(TileEntityHeatPipe tp, boolean net) {
		return net ? tp.getNetHeatEnergy()/HEAT_CAPACITY : tp.heatEnergy/HEAT_CAPACITY;
	}

	@Override
	protected void onFirstTick(Level world, int x, int y, int z) {
		heatEnergy = ReikaWorldHelper.getAmbientTemperatureAt(world, x, y, z)*HEAT_CAPACITY;
	}

	private void balanceHeat(Level world, int x, int y, int z) {
		for (int i = 0; i < 6; i++) {
			BlockEntity te = this.getAdjacentTileEntity(dirs[i]);
			if (te instanceof TileEntityHeatPipe) {
				TileEntityHeatPipe tile = (TileEntityHeatPipe)te;
				this.balanceWith(tile);
			}
			else if (te instanceof WorldRift) {
				WorldRift wr = (WorldRift)te;
				BlockEntity tile = wr.getTileEntityFrom(dirs[i]);
				if (tile instanceof TileEntityHeatPipe) {
					TileEntityHeatPipe ts = (TileEntityHeatPipe)tile;
					this.balanceWith(ts);
				}
			}
			else if (te != null && this.canConnectToMachine(te.getBlockType(), te, dirs[i], te)) {
				HeatConduction hc = (HeatConduction)te;
				double theirheat = this.getNetHeat(hc);
				double ourheat = this.getNetHeatEnergy();
				double theirtemp = this.getNetTemperature(hc);
				double ourtemp = this.getTemperatureForPipe(this, true);
				boolean intake = theirheat > ourheat;
				boolean valid = intake ? hc.allowHeatExtraction() && ourtemp < theirtemp : hc.allowExternalHeating() && ourtemp > theirtemp;
				//ReikaJavaLibrary.pConsole(our+" vs "+heat+" > "+valid, Dist.DEDICATED_SERVER);
				//ReikaJavaLibrary.pConsole("our "+ourheat+", their "+theirheat+" (Ts = "+theirtemp+", "+ourtemp+")", Dist.DEDICATED_SERVER, valid && !intake);
				if (valid) {
					double diff = ourheat-theirheat; // >0 if applying heat
					diff /= 4;
					int put = this.getTemperatureForHeat(diff, hc);
					//ReikaJavaLibrary.pConsole("Adding "+put+" to "+hc, Dist.DEDICATED_SERVER, !intake);
					hc.setTemperature(put+hc.getAmbientTemperature());
					heatEnergy -= diff;
					if (diff < 0) {
						ReactorType type = ItemStack.EMPTY;
						if (te instanceof ReactorTyped) {
							ReactorTyped tb = (ReactorTyped)te;
							type = tb.getReactorType();
						}
						if (type != null)
							reactorTypes.addValue(type, diff);
					}
					else if (diff > 0) {
						if (te instanceof TileEntityNuclearBoiler) {
							TileEntityNuclearBoiler tb = (TileEntityNuclearBoiler)te;
							tb.setReactorTypes(reactorTypes);
						}
					}
					/*
					if (diff > 0)
						ReikaJavaLibrary.pConsole("Taking "+diff+" heat from pipe to put into "+te+" at new temp "+put+" ["+ourtemp+" -> "+theirtemp+"]");
					else
						ReikaJavaLibrary.pConsole("Taking "+(-diff)+" heat from "+te+" to put into pipe @ heat "+heatEnergy+" ["+theirtemp+" -> "+ourtemp+"]");
					 */
				}
			}
		}
	}

	private void balanceWith(TileEntityHeatPipe ts) {
		if (ts.getTicksExisted() < 2)
			return;
		double diff = ts.heatEnergy-heatEnergy;
		if (diff <= 0)
			return;
		diff = diff/2; //no loss over distance
		ts.heatEnergy -= diff;
		heatEnergy += diff;
		reactorTypes = ts.reactorTypes;
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		heatEnergy = NBT.getDoubleOr("heat", 0D);
		this.updateBrightness();
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		NBT.putDouble("heat", heatEnergy);
	}

	private void updateBrightness() {
		float f = this.computeBrightness();
		brightnessDeltaSinceUpdate += Math.abs(f-lastBrightness);
		renderBrightness = f;
		if (level != null && (brightnessDeltaSinceUpdate >= 0.2 || this.getTicksExisted()-lastUpdateTime > 40)) {
			this.triggerBlockUpdate();
			brightnessDeltaSinceUpdate = 0;
			lastUpdateTime = this.getTicksExisted();
		}
	}

	@Override
	protected boolean canConnectToMachine(Block id, int meta, Direction dir, BlockEntity te) {
		if (!(te instanceof HeatConduction))
			return false;
		HeatConduction h = (HeatConduction)te;
		return h.allowExternalHeating() || h.allowHeatExtraction();
	}

	@Override
	public void onEntityCollided(Entity e) {
		if (temperature >= 100) {
			RotaryCraft.heatDamage.lastMachine = this;
			e.attackEntityFrom(RotaryCraft.heatDamage, temperature/100);
		}
	}

	public int getRenderColor() {
		int base = 0xFFA64D;
		float f = this.getBrightness();
		return f <= 0 ? base : ReikaColorAPI.mixColors(0xff3030, base, f);
	}

	private float computeBrightness() {
		if (temperature < 250)
			return 0;
		return Math.min(1, (temperature-250)/1500F);
	}

	public float getBrightness() {
		return renderBrightness;
	}

}
