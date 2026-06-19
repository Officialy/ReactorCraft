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

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.potion.Potion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;

import reika.dragonapi.base.ParticleEntity;
import reika.dragonapi.libraries.ReikaAABBHelper;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.world.ReikaWorldHelper;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.rotarycraft.api.interfaces.CustomFanEntity;

public class EntityPlasma extends ParticleEntity implements CustomFanEntity {

	private int targetX;
	private int targetZ;

	public int magnetOrdinal = -1;

	private int escapeTicks = 0;

	private String placerOfInjector;

	public EntityPlasma(Level world) {
		super(world);
	}

	public EntityPlasma(Level world, int x, int y, int z, String placer) {
		super(world, x, y, z);

		placerOfInjector = placer;
	}

	@Override
	public double getSpeed() {
		return 0.75;
	}

	@Override
	public boolean onEnterBlock(Level world, int x, int y, int z) {
		if (!world.isRemote) {
			if (ReikaWorldHelper.flammable(world, x, y, z))
				ReikaWorldHelper.ignite(world, x, y, z);
		}
		return false;
	}

	public void setTarget(int x, int z) {
		targetX = x;
		targetZ = z;
		double dx = targetX+0.5-posX;
		double dz = targetZ+0.5-posZ;
		double dd = ReikaMathLibrary.py3d(dx, 0, dz);
		double v = this.getSpeed();
		motionX = dx*v/dd;
		motionZ = dz*v/dd;
		//ReikaJavaLibrary.pConsole(motionX+":"+motionZ);
		velocityChanged = true;
	}

	private void checkFusion() {
		AABB box = ReikaAABBHelper.getEntityCenteredAABB(this, 1);
		List<EntityPlasma> li = worldObj.getEntitiesWithinAABB(EntityPlasma.class, box);
		if (li.size() >= this.getFusionThreshold() && !li.get(0).hasEscaped() && !li.get(li.size()-1).hasEscaped()) {
			EntityFusion fus = new EntityFusion(worldObj, posX, posY, posZ, placerOfInjector);
			worldObj.spawnEntityInWorld(fus);
			this.setDead();
		}
	}

	public int getFusionThreshold() {
		return 15+rand.nextInt(6);
	}

	@Override
	public void applyEntityCollision(Entity e) {
		int dmg = e instanceof LivingEntity && ((LivingEntity)e).isPotionActive(Potion.fireResistance) ? 4 : Integer.MAX_VALUE;
		e.attackEntityFrom(ReactorCraft.fusionDamage, dmg);
		if (e instanceof Player) {
			if (e.isDead || ((LivingEntity)e).getHealth() <= 0) {
				ReactorAchievements.PLASMADIE.triggerAchievement((Player)e);
			}
		}
	}

	@Override
	protected void onTick() {
		if (ticksExisted > 1200)
			;//this.setDead();
		if (!worldObj.isRemote && !this.hasEscapedSeverely() && rand.nextInt(this.hasEscaped() ? 48 : 12) == 0)
			this.checkFusion();
		motionY = 0;
		if (this.getSpawnLocation() != null)
			posY = this.getSpawnLocation().yCoord+0.5;

		if (ticksExisted > 300 && this.hasEscapedSeverely())
			this.setDead();

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
