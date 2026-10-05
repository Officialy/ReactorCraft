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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import reika.dragonapi.instantiable.data.Proportionality;
import reika.dragonapi.libraries.mathsci.ReikaEngLibrary;
import reika.dragonapi.libraries.mathsci.ReikaThermoHelper;
import reika.dragonapi.libraries.rendering.ReikaColorAPI;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.auxiliary.ReactorTyped;
import reika.reactorcraft.base.TileEntityLine;
import reika.reactorcraft.base.TileEntityNuclearBoiler;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.rotarycraft.auxiliary.interfaces.HeatConduction;

public class TileEntityHeatPipe extends TileEntityLine {

	public TileEntityHeatPipe(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.HEATPIPE.get(), pos, state);
	}

	private static final double HEAT_CAPACITY = ReikaThermoHelper.COPPER_HEAT * ReikaEngLibrary.rhoiron;

	private double heatEnergy;

	private Proportionality<ReactorType> reactorTypes = new Proportionality<>();

	private float renderBrightness;
	private float lastBrightness;
	private float brightnessDeltaSinceUpdate;
	private int lastUpdateTime;

	@Override
	public Identifier getTexture() {
		return Identifier.fromNamespaceAndPath("reactorcraft", "block/heat_pipe");
	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.HEATPIPE;
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		super.updateEntity(world, pos);

		if (!world.isClientSide()) {
			this.balanceHeat(world, pos);

			if (this.getTicksExisted() % 32 == 0) {
				//this.ventHeat(world, pos); //TODO fix heat pipe heat loss
			}
		}
	}

	private void ventHeat(Level world, BlockPos pos) {
		double temp = getTemperatureForPipe(this, false);
		int Tamb = ReikaWorldHelper.getAmbientTemperatureAt(world, pos);
		if (temp >= Tamb) {
			temp -= (temp - Tamb) / 96D;
			heatEnergy = HEAT_CAPACITY * temp;
		}
		temperature = (int) temp;
	}

	public double getNetHeatEnergy() {
		return heatEnergy - ReikaWorldHelper.getAmbientTemperatureAt(level, this.getBlockPos()) * HEAT_CAPACITY;
	}

	public static double getNetTemperature(HeatConduction hc) {
		return hc.getTemperature() - hc.getAmbientTemperature();
	}

	public static double getNetHeat(HeatConduction hc) {
		return hc.heatEnergyPerDegree() * getNetTemperature(hc);
	}

	public static int getTemperatureForHeat(double heat, HeatConduction hc) {
		return (int) Math.max(1, heat / hc.heatEnergyPerDegree());
	}

	public static double getTemperatureForPipe(TileEntityHeatPipe tp, boolean net) {
		return net ? tp.getNetHeatEnergy() / HEAT_CAPACITY : tp.heatEnergy / HEAT_CAPACITY;
	}

	@Override
	protected void onFirstTick(Level world, BlockPos pos) {
		heatEnergy = ReikaWorldHelper.getAmbientTemperatureAt(world, pos) * HEAT_CAPACITY;
	}

	private void balanceHeat(Level world, BlockPos pos) {
		for (Direction dir : dirs) {
			BlockEntity te = this.getAdjacentBlockEntity(dir);
			if (te instanceof TileEntityHeatPipe tile) {
				this.balanceWith(tile);
			}
			// CHROMA-PORT: ChromatiCraft WorldRift cross-dimension heat transfer gated out (mod not in build).
			else if (te != null && this.canConnectToMachine(te.getBlockState().getBlock(), dir, te)) {
				HeatConduction hc = (HeatConduction) te;
				double theirheat = getNetHeat(hc);
				double ourheat = this.getNetHeatEnergy();
				double theirtemp = getNetTemperature(hc);
				double ourtemp = getTemperatureForPipe(this, true);
				boolean intake = theirheat > ourheat;
				boolean valid = intake ? hc.allowHeatExtraction() && ourtemp < theirtemp : hc.allowExternalHeating() && ourtemp > theirtemp;
				if (valid) {
					double diff = ourheat - theirheat; // >0 if applying heat
					diff /= 4;
					hc.setTemperature(getTemperatureForHeat(theirheat + diff, hc) + hc.getAmbientTemperature());
					heatEnergy -= diff;
					if (diff < 0) {
						ReactorType type = null;
						if (te instanceof ReactorTyped tb) {
							type = tb.getReactorType();
						}
						if (type != null)
							reactorTypes.addValue(type, -diff);
					}
					else if (diff > 0) {
						if (te instanceof TileEntityNuclearBoiler tb) {
							tb.setReactorTypes(reactorTypes);
						}
					}
				}
			}
		}
	}

	private void balanceWith(TileEntityHeatPipe ts) {
		if (ts.getTicksExisted() < 2)
			return;
		double diff = ts.heatEnergy - heatEnergy;
		if (diff <= 0)
			return;
		diff = diff / 2; //no loss over distance
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
		brightnessDeltaSinceUpdate += Math.abs(f - lastBrightness);
		renderBrightness = f;
		if (level != null && (brightnessDeltaSinceUpdate >= 0.2 || this.getTicksExisted() - lastUpdateTime > 40)) {
			this.triggerBlockUpdate();
			brightnessDeltaSinceUpdate = 0;
			lastUpdateTime = this.getTicksExisted();
		}
	}

	@Override
	protected boolean canConnectToMachine(Block id, Direction dir, BlockEntity te) {
		if (!(te instanceof HeatConduction h))
			return false;
        return h.allowExternalHeating() || h.allowHeatExtraction();
	}

	@Override
	public void onEntityCollided(Entity e) {
		if (temperature >= 100 && e.level() instanceof ServerLevel sl) {
			e.hurtServer(sl, sl.damageSources().inFire(), temperature / 100F);
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
		return Math.min(1, (temperature - 250) / 1500F);
	}

	public float getBrightness() {
		return renderBrightness;
	}

}
