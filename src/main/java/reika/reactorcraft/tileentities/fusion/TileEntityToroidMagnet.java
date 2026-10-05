/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.tileentities.fusion;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;

import reika.dragonapi.DragonAPI;
import reika.dragonapi.instantiable.HybridTank;
import reika.dragonapi.instantiable.StepTimer;
import reika.dragonapi.interfaces.blockentity.ChunkLoadingTile;
import reika.dragonapi.interfaces.blockentity.NonIFluidTank;
import reika.dragonapi.libraries.ReikaAABBHelper;
import reika.reactorcraft.auxiliary.FusionReactorToroidPart;
import reika.reactorcraft.auxiliary.MultiBlockTile;
import reika.reactorcraft.auxiliary.NeutronTile;
import reika.reactorcraft.base.TileEntityReactorBase;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.entities.EntityPlasma;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorOptions;
import reika.reactorcraft.registry.ReactorTiles;
import reika.rotarycraft.api.interfaces.Screwdriverable;
import reika.rotarycraft.api.interfaces.Shockable;
import reika.rotarycraft.base.blockentity.BlockEntityPiping;
import reika.rotarycraft.entities.EntityDischarge;
import reika.rotarycraft.registry.MachineRegistry;
import reika.rotarycraft.registry.RotaryFluids;

public class TileEntityToroidMagnet extends TileEntityReactorBase implements Screwdriverable, Shockable, MultiBlockTile, FusionReactorToroidPart,
ChunkLoadingTile, NeutronTile, NonIFluidTank {

	public TileEntityToroidMagnet(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.MAGNET.get(), pos, state);
	}

	//0 is +x(E), rotates to -z(N)
	private Aim aim = Aim.N;

	private int alpha = 512;

	protected boolean hasSolenoid = false;

	private int charge = 0;

	private final StepTimer chargeTimer = new StepTimer(20);
	private final StepTimer reCheckTimer = new StepTimer(20);

	private static final int RATE = ReactorOptions.getToroidChargeRate();

	private final HybridTank tank = new HybridTank("toroid", 8000);

	private boolean hasNext;

	private boolean isActive;
	private int lastPlasma;

	public boolean hasMultiBlock() {
		return true;
	}

	public void setHasMultiBlock(boolean has) {

	}

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.MAGNET;
	}

	@Override
	protected void onFirstTick(Level world, BlockPos pos) {
		if (!hasSolenoid) {
			this.checkSurroundingMagnetsAndCopySolenoidState();
		}
		hasNext = this.checkCompleteness(world, pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		if (alpha > 0)
			alpha -= 8;

		if (DragonAPI.debugtest) {
			tank.addLiquid(1000, RotaryFluids.LIQUID_NITROGEN.get());
			charge = 250000;
		}

		AABB box = ReikaAABBHelper.getBlockAABB(pos);
		List<EntityPlasma> li = world.getEntitiesOfClass(EntityPlasma.class, box);
		int[] tg = this.getTarget();
		for (EntityPlasma e : li) {
			if (this.canAffect(e)) {
				e.resetEscapeTimer();
				e.setTarget(tg[0], tg[2]);
				e.magnetOrdinal = this.getOrdinal();
				tank.removeLiquid(10);

				this.setActive();
			}
			else {
				ReactorAchievements.ESCAPE.triggerAchievement(this.getPlacer());
			}
		}

		MachineRegistry m = MachineRegistry.getMachine(world, new BlockPos(x, y+2, z));
		if (m != null && m.isStandardPipe()) {
			BlockEntity te = world.getBlockEntity(new BlockPos(x, y+2, z));
			int amt = Math.min(tank.getRemainingSpace(), ((BlockEntityPiping)te).getFluidLevel());
			if (amt > 0) {
				if (ReactorFluids.getLegacyFluid("rc liquid nitrogen").equals(((BlockEntityPiping)te).getAttributes())) {
					tank.addLiquid(amt, ReactorFluids.getLegacyFluid("rc liquid nitrogen"));
					((BlockEntityPiping)te).removeLiquid(amt);
				}
			}
		}

		chargeTimer.update();
		if (chargeTimer.getTick()%RATE == 0)
			this.distributeCharge(world, x, y, z);
		if (chargeTimer.checkCap()) {
			this.updateCharge(world, x, y, z);
		}

		if (hasSolenoid) {
			reCheckTimer.update();
			if (reCheckTimer.checkCap() && !world.isClientSide()) {
				hasNext = this.checkCompleteness(world, x, y, z);
			}
		}

		if (lastPlasma > 0) {
			lastPlasma--;
			if (lastPlasma == 0) {
				this.setInactive();
			}
		}
	}

	private void setActive() {
		boolean last = isActive;
		isActive = true;
		lastPlasma = 20;
		if (!last) {
			level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
			if (ReactorOptions.CHUNKLOADING.getState()) {
				// CHUNKLOAD-PORT: ChunkManager.instance.loadChunks(this); — DragonAPI chunkloading manager
				// is not ported yet (RC has it commented out too); re-enable when it lands.
			}
		}
	}

	private void setInactive() {
		boolean last = isActive;
		isActive = false;
		if (last) {
			level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
			// CHUNKLOAD-PORT: ChunkManager.instance.unloadChunks(this);
		}
	}

	private boolean checkCompleteness(Level world, int x, int y, int z) {
		FusionReactorToroidPart te = this.getNextPart(world, x, y, z);
		int i = 60;
		while (te != null && te != this && i >= 0) {
			te = te.getNextPart(world, x, y, z);
			i--;
		}
		if (te != this)
			if (te instanceof TileEntityToroidMagnet)
				((TileEntityToroidMagnet)te).hasNext = false;
		return te == this;
	}

	public FusionReactorToroidPart getNextPart(Level world, int x, int y, int z) {
		Aim a = this.getAim();
		int dx = this.getBlockPos().getX()+a.xOffset;
		int dz = this.getBlockPos().getZ()+a.zOffset;
		BlockEntity te = level.getBlockEntity(new BlockPos(dx, y, dz));
		return te instanceof FusionReactorToroidPart ? (FusionReactorToroidPart)te : null;
	}

	public int getCoolant() {
		return tank.getFluidLevel();
	}

	public int getCharge() {
		return charge;
	}

	private void checkSurroundingMagnetsAndCopySolenoidState() {
		Aim a = this.getAim();
		BlockPos pos = this.getBlockPos();
		int dx = pos.getX()+a.xOffset;
		int dz = pos.getZ()+a.zOffset;
		int y0 = pos.getY();
		ReactorTiles r = ReactorTiles.getTE(level, new BlockPos(dx, y0, dz));
		if (r == ReactorTiles.MAGNET) {
			TileEntityToroidMagnet te = (TileEntityToroidMagnet)level.getBlockEntity(new BlockPos(dx, y0, dz));
			hasSolenoid = te.hasSolenoid;
			te.checkCompleteness(level, pos.getX(), y0, pos.getZ());
		}
		else if (r == ReactorTiles.INJECTOR) {
			dx += a.xOffset;
			dz += a.zOffset;
			BlockEntity te = level.getBlockEntity(new BlockPos(dx, y0, dz));
			while (te instanceof TileEntityFusionInjector) {
				dx += a.xOffset;
				dz += a.zOffset;
				te = level.getBlockEntity(new BlockPos(dx, y0, dz));
			}
			if (te instanceof TileEntityToroidMagnet) {
				hasSolenoid = ((TileEntityToroidMagnet)te).hasSolenoid;
			}
		}
	}

	private boolean distributeCharge(Level world, int x, int y, int z) {
		Aim a = this.getAim();
		int dx = x+a.xOffset;
		int dz = z+a.zOffset;
		ReactorTiles r = ReactorTiles.getTE(world, new BlockPos(dx, y, dz));
		if (r == ReactorTiles.MAGNET) {
			TileEntityToroidMagnet te = (TileEntityToroidMagnet)world.getBlockEntity(new BlockPos(dx, y, dz));
			int dC = charge-te.charge;
			if (dC > 0) {
				te.charge += dC/4;
				charge -= dC/4;
				int tx = te.getBlockPos().getX(), ty = te.getBlockPos().getY(), tz = te.getBlockPos().getZ();
				EntityDischarge e1 = new EntityDischarge(world, x+0.5, y+2.25, z+0.5, charge, tx+0.5, ty+2.25, tz+0.5);
				EntityDischarge e2 = new EntityDischarge(world, x+0.5, y-1.25, z+0.5, charge, tx+0.5, ty-1.25, tz+0.5);

				float ang = this.getAngle();
				float ang2 = te.getAngle();
				double fx = 1.75*Math.sin(Math.toRadians(ang));
				double fz = 1.75*Math.cos(Math.toRadians(ang));
				double fx2 = 1.75*Math.sin(Math.toRadians(ang2));
				double fz2 = 1.75*Math.cos(Math.toRadians(ang2));
				EntityDischarge e3 = new EntityDischarge(world, x+0.5+fx, y+0.5, z+0.5+fz, charge, tx+0.5+fx2, ty+0.5, tz+0.5+fz2);
				EntityDischarge e4 = new EntityDischarge(world, x+0.5-fx, y+0.5, z+0.5-fz, charge, tx+0.5-fx2, ty+0.5, tz+0.5-fz2);
				if (!world.isClientSide() && this.shouldSpawnSparks(world)) {
					world.addFreshEntity(e1);
					world.addFreshEntity(e2);
					world.addFreshEntity(e3);
					world.addFreshEntity(e4);
				}
			}
		}
		else if (r == ReactorTiles.INJECTOR) {
			dx += a.xOffset;
			dz += a.zOffset;
			TileEntityToroidMagnet te = (TileEntityToroidMagnet)world.getBlockEntity(new BlockPos(dx, y, dz));
			if (te != null) {
				int dC = charge-te.charge;
				if (dC > 0) {
					te.charge += dC/4;
					charge -= dC/4;
					int tx = te.getBlockPos().getX(), ty = te.getBlockPos().getY(), tz = te.getBlockPos().getZ();
					EntityDischarge e1 = new EntityDischarge(world, x+0.5, y+2, z+0.5, charge, tx+0.5, ty+2, tz+0.5);
					EntityDischarge e2 = new EntityDischarge(world, x+0.5, y-1, z+0.5, charge, tx+0.5, ty-1, tz+0.5);
					if (!world.isClientSide() && this.shouldSpawnSparks(world)) {
						world.addFreshEntity(e1);
						world.addFreshEntity(e2);
					}
				}
			}
		}
		return false;
	}

	private boolean shouldSpawnSparks(Level world) {
		return rand.nextBoolean();
	}

	private void updateCharge(Level world, int x, int y, int z) {
		if (charge <= 1)
			charge = 0;
		else
			charge *= 0.8;
	}

	private boolean canAffect(EntityPlasma e) {
		if (!hasNext)
			return false;
		if (!hasSolenoid)
			return false;
		if (charge <= 2500/*1000*/)
			return false;
		if (tank.isEmpty())
			return false;
		int o = this.getOrdinal();
		int p = e.magnetOrdinal;
		if (p == -1)
			return o%8 == 0;
		if (o > 30) {
			return p > 28 || p < 1;
		}
		if (p > 30) {
			return o > 28 || o < 1;
		}
		return Math.abs(p-o) <= 2;
	}

	public int[] getTarget() {
		int[] tg = new int[3];
		BlockPos pos = this.getBlockPos();
		tg[0] = pos.getX()+this.getAim().xOffset;
		tg[2] = pos.getZ()+this.getAim().zOffset;
		tg[1] = pos.getY();
		return tg;
	}

	@Override
	protected void animateWithTick(Level world, BlockPos pos) {

	}

	@Override
	protected void readSyncTag(CompoundTag NBT) {
		super.readSyncTag(NBT);

		aim = this.getAim(NBT.getIntOr("aim", 0));
		hasSolenoid = NBT.getBooleanOr("solenoid", false);

		charge = NBT.getIntOr("chg", 0);

		alpha = NBT.getIntOr("alp", 0);

		tank.readFromNBT(NBT);

		hasNext = NBT.getBooleanOr("next", false);

		isActive = NBT.getBooleanOr("active", false);
		lastPlasma = NBT.getIntOr("lastplasma", 0);
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT) {
		super.writeSyncTag(NBT);

		NBT.putInt("aim", this.getAim().ordinal());
		NBT.putBoolean("solenoid", hasSolenoid);

		NBT.putInt("alp", alpha);

		NBT.putInt("chg", charge);

		tank.writeToNBT(NBT);

		NBT.putBoolean("next", hasNext);

		NBT.putBoolean("active", isActive);
		NBT.putInt("lastplasma", lastPlasma);
	}

	public Aim getAim() {
		return aim != null ? aim : Aim.N;
	}

	private Aim getAim(int o) {
		return (o > 0 && o < Aim.list.length) ? Aim.list[o] : Aim.N;
	}

	@Override
	public boolean onShiftRightClick(Level world, BlockPos pos, Direction side) {
		alpha = 512;
		this.decrementAim();
		return true;
	}

	@Override
	public boolean onRightClick(Level world, BlockPos pos, Direction side) {
		this.refreshAlpha();
		this.incrementAim();
		return true;
	}

	public float getAngle() {
		return this.getAim().angle;
	}

	public int getOrdinal() {
		return this.getAim().ordinal();
	}

	public int getPreviousOrdinal() {
		int o = this.getOrdinal();
		return o > 0 ? o : Aim.list.length-1;
	}

	public void refreshAlpha() {
		alpha = 512;
	}

	public int getAlpha() {
		return alpha;
	}

	private void incrementAim() {
		int o = this.getAim().ordinal();
		if (o == Aim.list.length-1) {
			aim = Aim.list[0];
		}
		else {
			aim = Aim.list[o+1];
		}

		if (!hasSolenoid) {
			this.checkSurroundingMagnetsAndCopySolenoidState();
		}
	}

	private void decrementAim() {
		int o = this.getAim().ordinal();
		if (o == 0) {
			aim = Aim.list[Aim.list.length-1];
		}
		else {
			aim = Aim.list[o-1];
		}

		if (!hasSolenoid) {
			this.checkSurroundingMagnetsAndCopySolenoidState();
		}
	}

	// 1.21.5: BlockEntity.getRenderBoundingBox was removed; kept as a helper for the renderer's bounds.
	public AABB getRenderBoundingBox() {
		return ReikaAABBHelper.getBlockAABB(this.getBlockPos()).inflate(3, 3, 3);
	}

	public enum Aim {
		N(0,			2, 0),
		NNW1(11.3F,		2, -1),
		NNW2(24,		2, -1),
		NNW3(36.9F,		2, -2),
		NW(45,			2, -2),
		WNW1(53.1F,		1, -2),
		WNW2(66,		1, -2),
		WNW3(78.7F,		0, -2),
		W(90,			0, -2),
		WSW1(101.3F,	-1, -2),
		WSW2(114,		-1, -2),
		WSW3(126.9F,	-2, -2),
		SW(135,			-2, -2),
		SSW1(143.1F,	-2, -1),
		SSW2(156,		-2, -1),
		SSW3(168.7F,	-2, 0),
		S(180,			-2, 0),
		SSE1(191.3F,	-2, 1),
		SSE2(204,		-2, 1),
		SSE3(216.9F,	-2, 2),
		SE(225,			-2, 2),
		ESE1(233.1F,	-1, 2),
		ESE2(246,		-1, 2),
		ESE3(258.7F,	0, 2),
		E(270,			0, 2),
		ENE1(281.3F,	1, 2),
		ENE2(294,		1, 2),
		ENE3(306.9F,	2, 2),
		NE(315,			2, 2),
		NNE1(323.1F,	2, 1),
		NNE2(336,		2, 1),
		NNE3(348.7F,	2, 0);

		public final float angle;
		public final int xOffset;
		public final int zOffset;

		public static final Aim[] list = values();

		Aim(float a, int x, int z) {
			angle = a;
			xOffset = x;
			zOffset = z;
		}

		public Aim getNext() {
			return this.ordinal() < list.length-1 ? list[this.ordinal()+1] : list[0];
		}

		public Aim getPrev() {
			return this.ordinal() > 0 ? list[this.ordinal()-1] : list[list.length-1];
		}

		public boolean isCardinal() {
			return this.ordinal()%8 == 0;
		}
	}

	@Override
	public void onDischarge(int charge, double range) {
		this.charge += charge;
	}

	@Override
	public int getMinDischarge() {
		return 8192;
	}

	@Override
	public BlockPos getAimPos() {
		return this.getBlockPos();
	}

	public void setAim(Aim a) {
		aim = a != null ? a : aim;
	}

	@Override
	public void breakBlock() {
		// CHUNKLOAD-PORT: ChunkManager.instance.unloadChunks(this);
	}

	@Override
	public Collection<ChunkPos> getChunksToLoad() {
		Set<ChunkPos> set = new HashSet();
		int cx = this.getBlockPos().getX() >> 4;
		int cz = this.getBlockPos().getZ() >> 4;
		for (int i = -2; i <= 2; i++) {
			for (int k = -2; k <= 2; k++) {
				set.add(new ChunkPos(cx+i, cz+k));
			}
		}
		return set;
	}

	public boolean isActive() {
		return isActive;
	}

	@Override
	public boolean canDischargeLongRange() {
		return true;
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		return false;
	}

	@Override
	public boolean allowAutomation() {
		return true;
	}

	@Override
	public int addFluid(Fluid fluid, int amount, boolean doFill) {
		if (fluid != RotaryFluids.LIQUID_NITROGEN.get())
			return 0;
		int add = tank.canTakeIn(fluid, 1) ? Math.min(tank.getRemainingSpace(), amount) : 0;
		if (doFill && add > 0) {
			tank.addLiquid(add, fluid);
		}
		return add;
	}
}
