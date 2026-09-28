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
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import reika.dragonapi.base.ParticleEntity;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorEntities;
import reika.rotarycraft.api.interfaces.CustomFanEntity;

public class EntityPlasma extends ParticleEntity implements CustomFanEntity {

	private int targetX;
	private int targetZ;

	public int magnetOrdinal = -1;

	private int escapeTicks = 0;

	private String placerOfInjector;

	public EntityPlasma(EntityType<? extends Entity> type, Level world) {
		super(type, world);
	}

	public EntityPlasma(Level world, int x, int y, int z, String placer) {
		super(ReactorEntities.PLASMA.get(), world, new BlockPos(x, y, z));
		placerOfInjector = placer;
	}

	@Override
	public double getSpeed() {
		return 0.75;
	}

	@Override
	public boolean onEnterBlock(Level world, BlockPos pos) {
		if (!world.isClientSide()) {
			if (ReikaWorldHelper.flammable(world, pos))
				ReikaWorldHelper.ignite(world, pos);
		}
		return false;
	}

	public void setTarget(int x, int z) {
		targetX = x;
		targetZ = z;
		double dx = targetX+0.5-this.getX();
		double dz = targetZ+0.5-this.getZ();
		double dd = ReikaMathLibrary.py3d(dx, 0, dz);
		double v = this.getSpeed();
		this.setDeltaMovement(dx*v/dd, this.getDeltaMovement().y, dz*v/dd);
		this.syncVelocity = true;
	}

	private void checkFusion() {
		AABB box = this.getBoundingBox().inflate(1);
		List<EntityPlasma> li = this.level().getEntitiesOfClass(EntityPlasma.class, box);
		if (li.size() >= this.getFusionThreshold() && !li.get(0).hasEscaped() && !li.get(li.size()-1).hasEscaped()) {
			EntityFusion fus = new EntityFusion(this.level(), this.getX(), this.getY(), this.getZ(), placerOfInjector);
			this.level().addFreshEntity(fus);
			this.discard();
		}
	}

	public int getFusionThreshold() {
		return 15+this.random.nextInt(6);
	}

	@Override
	public void applyEntityCollision(Entity e) {
		float dmg = e instanceof LivingEntity && ((LivingEntity)e).hasEffect(MobEffects.FIRE_RESISTANCE) ? 4 : Integer.MAX_VALUE;
		// MOD-PORT: legacy ReactorCraft.fusionDamage custom DamageSource -> vanilla hot source.
		if (e.level() instanceof ServerLevel sl)
			e.hurtServer(sl, sl.damageSources().inFire(), dmg);
		if (e instanceof Player) {
			if (!e.isAlive() || ((LivingEntity)e).getHealth() <= 0) {
				ReactorAchievements.PLASMADIE.triggerAchievement((Player)e);
			}
		}
	}

	@Override
	protected void onTick() {
		if (!this.level().isClientSide() && !this.hasEscapedSeverely() && this.random.nextInt(this.hasEscaped() ? 48 : 12) == 0)
			this.checkFusion();
		Vec3 mot = this.getDeltaMovement();
		this.setDeltaMovement(mot.x, 0, mot.z);
		if (this.getSpawnLocation() != null)
			this.setPos(this.getX(), this.getSpawnLocation().pos.getY()+0.5, this.getZ());

		if (tickCount > 300 && this.hasEscapedSeverely())
			this.discard();

		escapeTicks++;
	}

	@Override
	public double getHitboxSize() {
		return 0.5;
	}

	@Override
	public boolean despawnOverTime() {
		return false;
	}

	@Override
	public long getBlowPower() {
		return 16777216;
	}

	@Override
	public double getMaxDeflection() {
		return 0.5;
	}

	public void resetEscapeTimer() {
		escapeTicks = 0;
	}

	public boolean hasEscaped() {
		return escapeTicks >= 6; //was 4
	}

	public boolean hasEscapedSeverely() {
		return escapeTicks >= 12; //was 8
	}

	@Override
	public boolean canInteractWithSpawnLocation() {
		return false;
	}

	@Override
	public boolean despawnOverDistance() {
		return true;
	}

	@Override
	protected double getDespawnDistance() {
		return 100;
	}

	@Override
	public double getRenderRangeSquared() {
		return Double.POSITIVE_INFINITY;
	}

}
