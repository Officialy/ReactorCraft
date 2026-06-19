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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import reika.dragonapi.base.InertEntity;
import reika.reactorcraft.entities.EntityNeutron.NeutronType;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorEntities;
import reika.reactorcraft.registry.ReactorSounds;

public class EntityFusion extends InertEntity {

	public EntityFusion(EntityType<? extends Entity> type, Level world) {
		super(type, world);
	}

	public EntityFusion(Level world, double x, double y, double z, String creator) {
		super(ReactorEntities.FUSION.get(), world);
		this.setPos(x, y, z);
		BlockPos pos = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
		for (int i = 0; i < 3; i++)
			this.spawnNeutrons(world, pos);
		ReactorSounds.FUSION.playSound(world, x, y, z, 1, 1);

		if (creator != null && !creator.isEmpty()) {
			MinecraftServer server = world.getServer();
			if (server != null)
				ReactorAchievements.FUSION.triggerAchievement(server.getPlayerList().getPlayerByName(creator));
		}
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {

	}

	private void spawnNeutrons(Level world, BlockPos pos) {
		EntityNeutron e = new EntityNeutron(world, pos, this.getRandomDirection(), NeutronType.FUSION);
		if (!world.isClientSide())
			world.addFreshEntity(e);
	}

	public Direction getRandomDirection() {
		return Direction.values()[2+this.random.nextInt(4)];
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {

	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {

	}

	@Override
	public void tick() {
		super.tick();
		if (tickCount > 5)
			this.discard();
	}

}
