/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.base;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import reika.dragonapi.base.BlockEntityBase;
import reika.dragonapi.instantiable.StepTimer;
import reika.dragonapi.instantiable.data.Proportionality;
import reika.dragonapi.libraries.level.ReikaBlockHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.dragonapi.libraries.mathsci.ReikaEngLibrary;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.mathsci.ReikaThermoHelper;
import reika.dragonapi.modinteract.lua.LuaMethod;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.ReactorTyped;
import reika.reactorcraft.auxiliary.Temperatured;
import reika.reactorcraft.auxiliary.TemperaturedReactorTyped;
import reika.reactorcraft.auxiliary.TypedReactorCoreTE;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.registry.ReactorType;
import reika.reactorcraft.tileentities.TileEntityHeatPipe;
import reika.reactorcraft.tileentities.TileEntityReactorGenerator;
import reika.reactorcraft.tileentities.fission.TileEntityReactorBoiler;
import reika.reactorcraft.tileentities.fusion.TileEntitySolenoidMagnet;
import reika.reactorcraft.tileentities.powergen.TileEntitySteamLine;
import reika.reactorcraft.tileentities.powergen.TileEntityTurbineCore;
import reika.rotarycraft.api.interfaces.ThermalMachine;
import reika.rotarycraft.api.interfaces.Transducerable;
import reika.rotarycraft.api.power.ShaftPowerReceiver;
import reika.rotarycraft.auxiliary.Variables;
import reika.rotarycraft.auxiliary.interfaces.TemperatureTE;

public abstract class TileEntityReactorBase extends BlockEntityBase implements Transducerable {

	protected StepTimer thermalTicker = new StepTimer(20);

	protected int temperature;
	public float phi;

	private final HashMap<Integer, LuaMethod> luaMethods = new HashMap();
	private final HashMap<String, LuaMethod> methodNames = new HashMap();

	public TileEntityReactorBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public boolean allowTickAcceleration() {
		return this.getTile().allowTickAcceleration();
	}

	@Override
	protected boolean shouldRunUpdateCode() {
		return !ReactorCraft.instance.isLocked() && this.isTickingTE();
	}

	protected boolean isTickingTE() {
		return true;
	}

	public abstract ReactorTiles getTile();

	@Override
	public Block getBlockEntityBlockID() {
		return this.getTile().getBlockState().getBlock();
	}

	public int getTextureState(Direction side) {
		return 0;
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);
		NBT.putInt("temp", temperature);
	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);
		temperature = NBT.getIntOr("temp", 0);
	}

	protected void updateTemperature(Level world, BlockPos pos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		float af = 1+1.5F*Mth.clamp((temperature-100)/500F, 0, 1);
		int Tamb = ReikaWorldHelper.getAmbientTemperatureAt(world, pos, af);

		if (world.dimension() != Level.NETHER)
			Tamb = Math.min(Tamb, 95);

		int dT = Tamb-temperature;
		if (dT != 0) {
			int d = ReikaWorldHelper.isExposedToAir(world, x, y, z) ? 32 : 64;
			int diff = (1+dT/d);
			if (diff <= 1)
				diff = dT/Math.abs(dT);
			temperature += diff;
		}

		ReikaWorldHelper.temperatureEnvironment(world, pos, Math.min(temperature, 1000));

		if (this instanceof TileEntityReactorBoiler && temperature >= 300 && Tamb > 100) {
			if (!((TileEntityReactorBoiler)this).tank.isEmpty()) {
				world.removeBlock(pos, false);
				world.explode(null, x+0.5, y+0.5, z+0.5, 3F, Level.ExplosionInteraction.BLOCK);
			}
		}

		for (int i = 0; i < 6; i++) {
			Direction dir = dirs[i];
			BlockPos dpos = pos.relative(dir);
			ReactorTiles r = ReactorTiles.getTE(world, dpos);
			if (r != null) {
				TileEntityReactorBase te = (TileEntityReactorBase)world.getBlockEntity(dpos);
				if (te instanceof Temperatured) {
					int Tamb_loc = ReikaWorldHelper.getAmbientTemperatureAt(world, dpos);

					Temperatured tr = (Temperatured)te;
					boolean flag = true;
					if (tr instanceof TileEntityNuclearCore)
						flag = true;
					if (flag) {
						int T = tr.getTemperature();
						dT = (T-temperature)-Math.max(0, (Tamb-Tamb_loc)); //if Tamb here is > Tamb there, subtract that difference to avoid exploits
						float f = te.getHeatThroughput(this);
						dT *= f;
						if (dT > 0) {
							int d = this.getHeatFraction(te);
							int newT = T-dT/d;
							float e = te.getHeatEfficiency(this);
							double add = dT/d*e;
							temperature += add;
							tr.setTemperature(newT);
							if (this instanceof TileEntityReactorBoiler && !(tr instanceof TileEntityReactorBoiler) && tr instanceof TypedReactorCoreTE) {
								((TileEntityReactorBoiler)this).setReactorType(((TypedReactorCoreTE)tr).getReactorType(), add);
							}
							else if (this instanceof TileEntityNuclearBoiler && tr.getClass() == this.getClass()) {
								((TileEntityNuclearBoiler)this).setReactorType(((TileEntityNuclearBoiler)tr).getReactorType(), add);
							}
						}
					}
				}
			}
		}
	}

	private int getHeatFraction(TileEntityReactorBase other) {
		return other instanceof TemperaturedReactorTyped ? this.getHeatConductionFraction((TemperaturedReactorTyped)other) : 4;
	}

	private float getHeatThroughput(TileEntityReactorBase other) {
		return other instanceof TemperaturedReactorTyped ? this.getHeatConductionThroughput((TemperaturedReactorTyped)other) : 1;
	}

	private float getHeatEfficiency(TileEntityReactorBase other) {
		return other instanceof TemperaturedReactorTyped ? this.getHeatConductionEfficiency((TemperaturedReactorTyped)other) : 0;
	}

	/** For transferring heat FROM that reactor block. */
	protected int getHeatConductionFraction(TemperaturedReactorTyped other) {
		return 4;
	}

	/** For transferring heat TO that reactor block. */
	protected float getHeatConductionThroughput(TemperaturedReactorTyped other) {
		return 1;
	}

	/** For transferring heat TO that reactor block. */
	protected float getHeatConductionEfficiency(TemperaturedReactorTyped other) {
		ReactorTiles r0 = other.getTile();
		if (r0 == ReactorTiles.CONTROL || r0 == ReactorTiles.CPU)
			return this.getControlCPUHeatEfficiency();
		ReactorType r1 = this instanceof ReactorTyped ? ((ReactorTyped)this).getReactorType() : null;
		ReactorType r2 = other.getReactorType();
		if (r1 == r2)
			return 1;
		if (r1 == null || r2 == null) //one tile is not even a reactor
			return 0;
		return r1.getTypeMismatchHeatEfficiency();
	}

	protected float getControlCPUHeatEfficiency() {
		return 1;
	}

	protected float getTypeMismatchEfficiency() {
		return 0.5F;
	}

	public Direction getRandomDirection(boolean allowVertical) {
		int r = allowVertical ? rand.nextInt(6) : 2+rand.nextInt(4);
		return dirs[r];
	}

	@Override
	public final ArrayList<String> getMessages(Level world, BlockPos pos, Direction side) {
		ArrayList<String> li = new ArrayList();
		if (this instanceof Temperatured) {
			String s = String.format("%s %s: %dC", this.getTEName(), Variables.TEMPERATURE, ((Temperatured)this).getTemperature());
			li.add(s);
		}
		else if (this instanceof ThermalMachine) {
			String s = String.format("%s %s: %dC", this.getTEName(), Variables.TEMPERATURE, ((ThermalMachine)this).getTemperature());
			//li.add(s);
		}
		else if (this instanceof TemperatureTE) {
			String s = String.format("%s %s: %dC", this.getTEName(), Variables.TEMPERATURE, ((TemperatureTE)this).getTemperature());
			li.add(s);
		}
		if (this instanceof TileEntityReactorPiping) {
			TileEntityReactorPiping rp = (TileEntityReactorPiping)this;
			if (rp.getFluidLevel() <= 0) {
				String s = String.format("%s is empty.", this.getTEName());
				li.add(s);
			}
			else {
				String s = String.format("%s contains %d mB of %s", this.getTEName(), rp.getFluidLevel(), rp.getFluidType().getFluidType().getDescription().getString());
				li.add(s);
			}
		}
		if (this instanceof TileEntitySolenoidMagnet) {
			ShaftPowerReceiver sp = (ShaftPowerReceiver)this;
			String pre = ReikaEngLibrary.getSIPrefix(sp.getPower());
			double base = ReikaMathLibrary.getThousandBase(sp.getPower());
			li.add(String.format("%s receiving %.3f %sW @ %d rad/s.", sp.getName(), base, pre, sp.getOmega()));
		}
		if (this instanceof TileEntityReactorGenerator) {
			TileEntityReactorGenerator sp = (TileEntityReactorGenerator)this;
			li.add(sp.getGeneratedOutputForDisplay());
		}
		if (this instanceof TileEntityTurbineCore) {
			TileEntityTurbineCore sp = (TileEntityTurbineCore)this;
			long power = sp.getPower();
			String pre = ReikaEngLibrary.getSIPrefix(power);
			double base = ReikaMathLibrary.getThousandBase(power);
			li.add(String.format("%s producing %.3f %sW @ %d rad/s.", sp.getName(), base, pre, sp.getOmega()));
			li.add(String.format("Lubricant level %d mB per block.", sp.getLubricant()));
		}
		if (this instanceof TileEntitySteamLine) {
			TileEntitySteamLine sl = (TileEntitySteamLine)this;
			String s = String.format("%s contains %d m^3 of steam.", this.getTEName(), sl.getSteam());
			li.add(s);
			Proportionality<ReactorType> types = sl.getSourceReactorType();
			if (!types.isEmpty()) {
				li.add( "Reactor source types: ");
				for (ReactorType r : types.getElements()) {
					double frac = types.getFraction(r);
					li.add("  "+r+": "+frac*100+"%%");
				}
			}
		}
		if (this instanceof TileEntityHeatPipe) {
			TileEntityHeatPipe hp = (TileEntityHeatPipe)this;
			double e = hp.getNetHeatEnergy();
			String s = String.format("%s contains %.3f%sJ of heat energy.", this.getTEName(), ReikaMathLibrary.getThousandBase(e), ReikaEngLibrary.getSIPrefix(e));
			li.add(s);
		}
		return li;
	}

	@Override
	public int getRedstoneOverride() {
		return 0;
	}

	public boolean allowExternalHeating() {
		if (this instanceof ReactorTyped) {
			ReactorType r = ((ReactorTyped)this).getReactorType();
			return r != ReactorType.HTGR && r != ReactorType.FUSION;
		}
		return true;
	}

	public boolean allowHeatExtraction() {
		return true;
	}

	public boolean canBeCooledWithFins() {
		return false;
	}

	public double heatEnergyPerDegree() {
		double base = ReikaThermoHelper.STEEL_HEAT*ReikaBlockHelper.getBlockVolume(level, getBlockPos())*ReikaEngLibrary.rhoiron;
		if (this.getTile().isReactorCore() || this.getTile() == ReactorTiles.EXCHANGER)
			base *= 50;
		return base;
	}
}
