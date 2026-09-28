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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.reactorcraft.auxiliary.RadiationEffects;
import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;
import reika.reactorcraft.registry.ReactorEntities;

public final class EntityNuclearWaste extends ItemEntity {

	public static final int RANGE = 6;
	public static final int RADIATION_INTERVAL = 10*60*20;
	public static final int RADIATION_DELAY = 3*60*20;
	private int timer = 0;

	public EntityNuclearWaste(EntityType<? extends ItemEntity> type, Level world) {
		super(type, world);
	}

	public EntityNuclearWaste(Level world, double x, double y, double z, ItemStack is) {
		super(ReactorEntities.NUCLEARWASTE.get(), world);
		this.setPos(x, y, z);
		this.setItem(is);
		this.setUnlimitedLifetime();
	}

	@Override
	public void tick() {
		super.tick();
		this.applyRadiation();
		if (this.getY() < 0) {
			Vec3 mot = this.getDeltaMovement();
			if (!this.level().isClientSide())
				this.syncVelocity = true;
			this.setDeltaMovement(mot.x, Math.abs(mot.y), mot.z);
			this.setPos(this.getX(), Math.max(this.getY(), 0), this.getZ());

			if (timer%256 == 0) {
				AABB box = this.getBoundingBox().inflate(RANGE);
				List<EntityRadiation> li = this.level().getEntitiesOfClass(EntityRadiation.class, box);
				if (li.size() < 100) {
					int ix = Mth.floor(this.getX());
					int iy = Mth.floor(this.getY());
					int iz = Mth.floor(this.getZ());
					RadiationEffects.instance.contaminateArea(this.level(), ix, iy, iz, RANGE*4, 2, 0, false, RadiationIntensity.HIGHLEVEL);
				}
			}
		}
		timer++;
	}


	private void applyRadiation() {
		Level world = this.level();
		double x = this.getX();
		double y = this.getY();
		double z = this.getZ();
		AABB box = new AABB(x, y, z, x, y, z).inflate(RANGE, RANGE, RANGE);
		List<LivingEntity> inbox = world.getEntitiesOfClass(LivingEntity.class, box);
		for (LivingEntity e : inbox) {
			double dd = ReikaMathLibrary.py3d(e.getX()-x, e.getY()-y, e.getZ()-z);
			if (dd <= RANGE) {
				RadiationEffects.instance.applyEffects(e, RadiationIntensity.HIGHLEVEL);
			}
		}

		int ix = Mth.floor(x);
		int iy = Mth.floor(y);
		int iz = Mth.floor(z);

		//Contaminate the area slightly every 10 min left in the world, after the first 3 minutes
		if ((timer-RADIATION_DELAY)%RADIATION_INTERVAL == 0 && timer >= RADIATION_DELAY) {
			List<EntityRadiation> near = world.getEntitiesOfClass(EntityRadiation.class, box);
			if (near.size() < 32) {
				RadiationEffects.instance.contaminateArea(world, ix, iy, iz, RANGE*4, 2, 0, false, RadiationIntensity.HIGHLEVEL); //no LOS to simulate groundwater/air particulates
			}
		}
	}

}
