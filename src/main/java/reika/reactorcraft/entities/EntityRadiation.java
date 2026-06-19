/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.entities;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

import reika.dragonapi.base.InertEntity;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.reactorcraft.auxiliary.RadiationEffects;
import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;
import reika.reactorcraft.registry.ReactorEntities;

public class EntityRadiation extends InertEntity implements IEntityWithComplexSpawn {

	private int effectRange;
	private RadiationIntensity intensity;

	public boolean requireLOS = false;

	public EntityRadiation(EntityType<? extends Entity> type, Level world) {
		super(type, world);
	}

	public EntityRadiation(Level world, int range, RadiationIntensity ri) {
		super(ReactorEntities.RADIATION.get(), world);
		effectRange = range;
		intensity = ri;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {

	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		effectRange = input.getIntOr("effrange", 0);
		intensity = RadiationIntensity.radiationList[input.getIntOr("intensity", 0)];
		requireLOS = input.getBooleanOr("los", false);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.putInt("effrange", effectRange);
		output.putInt("intensity", intensity.ordinal());
		output.putBoolean("los", requireLOS);
	}

	@Override
	public void tick() {
		this.baseTick();
		this.applyRadiation();

		if (effectRange <= 0)
			this.discard();

		if (this.decays()) {
			if (this.random.nextInt(360000) == 0) {
				this.clean();
			}
		}
		if (this.rainCleanable()) {
			if (this.level().isRaining() && this.random.nextInt(36000) == 0 && this.level().isRainingAt(this.blockPosition())) {
				this.clean();
			}
		}
	}

	protected boolean decays() {
		return true;
	}

	protected boolean rainCleanable() {
		return true;
	}

	protected void applyRadiation() {
		Level world = this.level();
		double x = this.getX();
		double y = this.getY();
		double z = this.getZ();
		AABB box = new AABB(x, y, z, x, y, z).inflate(effectRange, effectRange, effectRange);
		List<LivingEntity> inbox = world.getEntitiesOfClass(LivingEntity.class, box);
		for (LivingEntity e : inbox) {
			double dd = ReikaMathLibrary.py3d(e.getX()-x, e.getY()-y, e.getZ()-z);
			if (dd <= effectRange) {
				RadiationEffects.instance.applyEffects(e, intensity);
			}
		}

		int dx = ReikaRandomHelper.getRandomPlusMinus(Mth.floor(x), effectRange);
		int dy = ReikaRandomHelper.getRandomPlusMinus(Mth.floor(y), effectRange);
		int dz = ReikaRandomHelper.getRandomPlusMinus(Mth.floor(z), effectRange);
		RadiationEffects.instance.transformBlock(world, dx, dy, dz, intensity);
	}

	public void clean() {
		if (effectRange > 0)
			effectRange--;
		else
			this.discard();
	}

	public int getRange() {
		return effectRange;
	}

	@Override
	public void writeSpawnData(RegistryFriendlyByteBuf data) {
		data.writeInt(effectRange);
		data.writeInt(intensity.ordinal());
	}

	@Override
	public void readSpawnData(RegistryFriendlyByteBuf data) {
		effectRange = data.readInt();
		intensity = RadiationIntensity.radiationList[data.readInt()];
	}

	@Override
	public boolean hurtServer(ServerLevel world, DamageSource src, float par2) {
		if (src.is(DamageTypeTags.IS_EXPLOSION)) {
			RadiationEffects.instance.contaminateArea(this.level(), this.getBlockX(), this.getBlockY(), this.getBlockZ(), effectRange, 0.65F, 0.5, true, intensity);
			this.discard();
			return true;
		}
		return super.hurtServer(world, src, par2);
	}

}
