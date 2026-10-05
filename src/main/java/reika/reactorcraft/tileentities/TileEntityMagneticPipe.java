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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;

// CHROMA-PORT: import reika.chromaticraft.api.interfaces.WorldRift;
import reika.dragonapi.instantiable.StepTimer;
import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.dragonapi.libraries.registry.ReikaParticleHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.auxiliary.NeutronTile;
import reika.reactorcraft.base.TileEntityReactorPiping;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlockEntities;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.fusion.TileEntityFusionHeater;
import reika.reactorcraft.tileentities.fusion.TileEntityFusionInjector;
import reika.rotarycraft.api.interfaces.Shockable;
import reika.rotarycraft.entities.EntityDischarge;

public class TileEntityMagneticPipe extends TileEntityReactorPiping implements Shockable, NeutronTile {

	public TileEntityMagneticPipe(BlockPos pos, BlockState state) {
		super(ReactorBlockEntities.MAGNETPIPE.get(), pos, state);
	}

	private int charge;

	private final StepTimer chargeTimer = new StepTimer(20);

	@Override
	public ReactorTiles getTile() {
		return ReactorTiles.MAGNETPIPE;
	}

	@Override
	public boolean isConnectedToNonSelf(Direction dir) {
		return false;
	}

	@Override
	public boolean isValidFluid(Fluid f) {
		return f != null && f.equals(ReactorFluids.getLegacyFluid("rc fusion plasma"));
	}

	@Override
	protected void onIntake(BlockEntity te) {

	}

	@Override
	public void updateEntity(Level world, BlockPos pos) {
		super.updateEntity(world, pos);

		this.distributeCharge(world, pos);
		this.updateCharge(world, pos);

		if (charge <= 0 && !world.isClientSide()) {
			charge = 0;
			if (fluid != null && fluid.getFluidType().getTemperature() > 5000) {
				world.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
				ReikaSoundHelper.playSoundAtBlock(world, pos, SoundEvents.FIRE_EXTINGUISH);
				ReikaParticleHelper.LAVA.spawnAroundBlock(world, pos, 5);
				ReactorAchievements.MELTPIPE.triggerAchievement(this.getPlacer());
			}
		}
	}

	private void distributeCharge(Level world, BlockPos pos) {
		for (int i = 0; i < 6; i++) {
			Direction dir = dirs[i];
			BlockPos p = pos.relative(dir);
			// CHROMA-PORT: ChromatiCraft WorldRift cross-dimension charge transfer gated out (mod not in build).
			ReactorTiles r = ReactorTiles.getTE(world, p);
			if (r == ReactorTiles.MAGNETPIPE) {
				TileEntityMagneticPipe tile = (TileEntityMagneticPipe)world.getBlockEntity(p);
				int dq = charge - tile.charge;
				if (dq > 0) {
					tile.charge += dq/4;
					charge -= dq/4;
				}
			}
		}
	}

	private void updateCharge(Level world, BlockPos pos) {
		Direction dir = ReikaWorldHelper.checkForAdjMaterial(world, pos, MapColor.WATER);
		if (dir != null) {
			BlockPos tp = pos.relative(dir);
			EntityDischarge e = new EntityDischarge(world, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, charge, tp.getX()+0.5, tp.getY()+0.5, tp.getZ()+0.5);
			if (!world.isClientSide())
				world.addFreshEntity(e);
			charge = 0;
		}
		else {
			if (charge > 1)
				charge *= 0.99;
			else
				charge = 0;
		}
	}

	@Override
	protected void writeSyncTag(CompoundTag NBT)
	{
		super.writeSyncTag(NBT);

		NBT.putInt("chg", charge);
	}

	@Override
	protected void readSyncTag(CompoundTag NBT)
	{
		super.readSyncTag(NBT);

		charge = NBT.getIntOr("chg", 0);
	}

	@Override
	public void onDischarge(int charge, double range) {
		this.charge += Math.pow(charge, 1.1);
	}

	@Override
	public int getMinDischarge() {
		return 256;
	}

	@Override
	public BlockPos getAimPos() {
		return this.getBlockPos();
	}

	public int getCharge() {
		return charge;
	}

	private boolean isPlasmaAcceptingBlock(BlockEntity te) {
		return te instanceof TileEntityMagneticPipe || te instanceof TileEntityFusionHeater || te instanceof TileEntityFusionInjector;
	}

	@Override
	protected boolean isInteractableTile(BlockEntity te) {
		// CHROMA-PORT: WorldRift acceptance gated out (ChromatiCraft not in build).
		return this.isPlasmaAcceptingBlock(te) && super.isInteractableTile(te);
	}

	@Override
	public boolean canDischargeLongRange() {
		return true;
	}

	@Override
	public boolean onNeutron(EntityNeutron e, Level world, BlockPos pos) {
		return false;
	}

}
