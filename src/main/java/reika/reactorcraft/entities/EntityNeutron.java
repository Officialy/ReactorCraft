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
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import reika.dragonapi.base.ParticleEntity;
import reika.dragonapi.instantiable.data.immutable.BlockKey;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.reactorcraft.api.NeutronShield;
import reika.reactorcraft.auxiliary.NeutronBlock;
import reika.reactorcraft.auxiliary.NeutronTile;
import reika.reactorcraft.auxiliary.RadiationEffects;
import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;
import reika.reactorcraft.registry.RadiationShield;
import reika.reactorcraft.registry.ReactorEntities;
import reika.reactorcraft.registry.ReactorOptions;

public class EntityNeutron extends ParticleEntity {

	private NeutronType type;
	private NeutronSpeed speed;

	// MOD-PORT: Botania/ThaumicTinkerer "platform" transparency blocks — those mods are not in the
	// 26.2 build, so neutrons treat them as opaque. Re-resolve via the block registry if they ship.
	private static final Block botaniaPlatform = null; // MOD-PORT: Botania platform not in build
	private static final Block ttPlatform = null; // MOD-PORT: ThaumicTinkerer platform not in build

	public EntityNeutron(EntityType<? extends Entity> type, Level world) {
		super(type, world);
	}

	public EntityNeutron(Level world, BlockPos pos, Direction f, NeutronType type) {
		super(ReactorEntities.NEUTRON.get(), world, pos, f);
		this.type = type;
		speed = type.getCreationSpeed();
	}

	@Override
	public void applyEntityCollision(Entity e) {
		if (ReikaRandomHelper.doWithChance(12.5)) {
			if (e instanceof LivingEntity) {
				RadiationEffects.instance.applyPulseEffects((LivingEntity)e, RadiationIntensity.MODERATE);
				this.discard();
			}
		}
	}

	@Override
	protected boolean onEnterBlock(Level world, BlockPos pos) {
		BlockState bs = world.getBlockState(pos);
		Block id = bs.getBlock();

		if (!this.isNeutronTransparent(id)) {
			if (bs.hasBlockEntity()) {
				BlockEntity te = world.getBlockEntity(pos);
				if (te instanceof NeutronTile) {
					return ((NeutronTile)te).onNeutron(this, world, pos);
				}
				// CHROMA-PORT: WorldRift neutron teleport gated out (ChromatiCraft not in build).
			}

			if (id instanceof NeutronBlock) {
				if (((NeutronBlock)id).onNeutron(this, world, pos))
					return true;
			}
			else if (id instanceof NeutronShield) {
				NeutronShield ns = (NeutronShield)id;
				String type = this.getNeutronType().name();
				double c = Math.min(ns.getAbsorptionChance(type), RadiationShield.BEDINGOT.neutronAbsorbChance);
				if (ReikaRandomHelper.doWithChance(c)) {
					double c2 = Mth.clamp(ns.getRadiationSpawnMultiplier(world, pos, type), 0, 1);
					if (ReikaRandomHelper.doWithChance(c2)) {
						this.spawnRadiationChance(world, pos);
					}
					return true;
				}
			}

			// BLOCK-PORT: fluorite irradiation (legacy meta+8 glow on FLUORITE/FLUORITEORE) — wire to
			// the fluorite blockstate when ReactorBlocks/FluoriteTypes is ported.

			RadiationShield rs = RadiationShield.getFrom(new BlockKey(bs));
			if (rs != null && ReikaRandomHelper.doWithChance(rs.neutronAbsorbChance))
				return true;

			if (ReikaRandomHelper.doWithChance(speed.getIrradiatedAbsorptionChance())) {
				float res = bs.getBlock().getExplosionResistance();
				int lightOpacity = bs.canOcclude() ? 15 : 0;
				boolean flag = bs.canOcclude()
						? (this.random.nextBoolean() && res >= 12) || ReikaRandomHelper.getSafeRandomInt((int)(24 - res)) == 0
						: (15 - lightOpacity == 0 ? ReikaRandomHelper.getSafeRandomInt(lightOpacity) > 0 : this.random.nextInt(1000) == 0);
				if (flag) {
					this.spawnRadiationChance(world, pos);
					if (ReikaRandomHelper.doWithChance(20))
						RadiationEffects.instance.transformBlock(world, pos.getX(), pos.getY(), pos.getZ(), RadiationIntensity.MODERATE);
					return true;
				}
			}
			return false;
		}

		return this.random.nextInt(1000) == 0;
	}

	private boolean isNeutronTransparent(Block id) {
		return id == Blocks.AIR || id == botaniaPlatform || id == ttPlatform;
	}

	private void spawnRadiationChance(Level world, BlockPos pos) {
		if (ReikaRandomHelper.doWithChance(2)) {
			AABB box = new AABB(pos).inflate(8, 8, 8);
			List<EntityRadiation> inbox = world.getEntitiesOfClass(EntityRadiation.class, box);
			if (inbox.size() < 3)
				RadiationEffects.instance.contaminateArea(world, pos.getX(), pos.getY(), pos.getZ(), 1, 1, 0, false, RadiationIntensity.LOWLEVEL);
		}
	}

	@Override
	public double getSpeed() {
		return 0.75;
	}

	@Override
	protected void onTick() {

	}

	public void moderate() {
		speed = NeutronSpeed.THERMAL;
	}

	@Override
	public double getHitboxSize() {
		return 0.1;
	}

	@Override
	public boolean despawnOverTime() {
		return true;
	}

	@Override
	public void writeSpawnData(RegistryFriendlyByteBuf data) {
		data.writeInt(type.ordinal());
	}

	@Override
	public void readSpawnData(RegistryFriendlyByteBuf data) {
		type = NeutronType.neutronList[data.readInt()];
		speed = type.getCreationSpeed();
	}

	public NeutronType getNeutronType() {
		return type != null ? type : NeutronType.NULL;
	}

	public NeutronSpeed getNeutronSpeed() {
		return speed;
	}

	public static enum NeutronType {
		NULL(),
		DECAY(),
		FISSION(),
		BREEDER(),
		FUSION(),
		WASTE(),
		THORIUM();

		public static final NeutronType[] neutronList = values();

		public int getBoilerAbsorptionChance() {
			return this == BREEDER || this == THORIUM ? 80 : 0;
		}

		public int getSodiumBoilerAbsorptionChance() {
			return this != BREEDER && this != DECAY ? 90 : 0;
		}

		public boolean canTriggerFuelConversion() {
			return this == BREEDER;
		}

		public boolean dealsDamage() {
			return this != NULL;
		}

		public boolean stoppedByWater() {
			return this != FUSION;
		}

		public boolean canIrradiateMaterials() {
			return this == FISSION || this == FUSION || this == BREEDER || this == THORIUM;
		}

		public boolean isFissionType() {
			return this == DECAY || this == FISSION || this == BREEDER || this == THORIUM;
		}

		public boolean canTriggerFission() {
			return this.isFissionType() || (this == WASTE && ReikaRandomHelper.doWithChance(40));
		}

		public NeutronSpeed getCreationSpeed() {
			if (!ReactorOptions.FASTNEUTRONS.getState())
				return NeutronSpeed.THERMAL;
			switch(this) {
				case BREEDER:
				case FISSION:
				case THORIUM:
					return NeutronSpeed.FAST;
				default:
					return NeutronSpeed.THERMAL;
			}
		}
	}

	public static enum NeutronSpeed {
		THERMAL(),
		FAST();

		public static final NeutronSpeed[] speedList = values();

		public float getInteractionMultiplier() {
			if (this == THERMAL)
				return 1;
			if (this == FAST)
				return 0.6F;
			return 0;
		}

		public double getIrradiatedAbsorptionChance() {
			if (this == THERMAL)
				return 100;
			if (this == FAST)
				return 40;
			return 100;
		}

		public float getWasteConversionMultiplier() {
			if (this == THERMAL)
				return 1;
			if (this == FAST)
				return 2.2F;
			return 0;
		}
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		type = NeutronType.neutronList[input.getIntOr("ntype", 0)];
		speed = NeutronSpeed.speedList[input.getIntOr("nspeed", 0)];
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.putInt("ntype", this.getNeutronType().ordinal());
		output.putInt("nspeed", this.getNeutronSpeed().ordinal());
	}

	@Override
	public boolean canInteractWithSpawnLocation() {
		return false;
	}

	@Override
	public boolean despawnOverDistance() {
		return false;
	}

	@Override
	public double getRenderRangeSquared() {
		return 4096D;
	}

}
