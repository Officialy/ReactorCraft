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

import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;

// CHROMA-PORT: import reika.chromaticraft.api.interfaces.WorldRift;
import reika.dragonapi.ModList;
import reika.dragonapi.base.ParticleEntity;
import reika.dragonapi.instantiable.BasicTeleporter;
import reika.dragonapi.instantiable.data.immutable.WorldLocation;
import reika.dragonapi.libraries.ReikaAABBHelper;
import reika.dragonapi.libraries.ReikaEntityHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.reactorcraft.api.NeutronShield;
import reika.reactorcraft.auxiliary.NeutronBlock;
import reika.reactorcraft.auxiliary.NeutronTile;
import reika.reactorcraft.auxiliary.RadiationEffects;
import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;
import reika.reactorcraft.registry.FluoriteTypes;
import reika.reactorcraft.registry.RadiationShield;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorOptions;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;

public class EntityNeutron extends ParticleEntity implements IEntityAdditionalSpawnData {

	private NeutronType type;
	private NeutronSpeed speed;

	private static Block botaniaPlatform;
	private static Block ttPlatform;

	public static void initTransparencyBlocks() {
		botaniaPlatform = ModList.BOTANIA.isLoaded() ? GameRegistry.findBlock(ModList.BOTANIA.modLabel, "platform") : null;
		ttPlatform = ModList.THAUMICTINKER.isLoaded() ? GameRegistry.findBlock(ModList.THAUMICTINKER.modLabel, "platform") : null;
	}

	public EntityNeutron(Level world, int x, int y, int z, ForgeDirection f, NeutronType type) {
		super(world, x, y, z, f);
		height = 1;
		this.type = type;
		speed = type.getCreationSpeed();
		if (speed == null)
			Thread.dumpStack();
	}

	public EntityNeutron(Level world) {
		super(world);
	}

	@Override
	public void applyEntityCollision(Entity e) {
		if (ReikaRandomHelper.doWithChance(12.5)) {
			if (e instanceof LivingEntity) {
				RadiationEffects.instance.applyPulseEffects((LivingEntity)e, RadiationIntensity.MODERATE);
				this.setDead();
			}
		}
	}

	@Override
	protected boolean onEnterBlock(Level world, int x, int y, int z) {
		Block id = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);

		if (!this.isNeutronTransparent(id)) {
			if (id.hasTileEntity(meta)) {
				BlockEntity te = world.getTileEntity(x, y, z);
				if (te instanceof NeutronTile) {
					return ((NeutronTile)te).onNeutron(this, world, x, y, z);
				}
				else if (te instanceof WorldRift) {
					WorldLocation tgt = ((WorldRift)te).getLinkTarget();
					if (tgt != null) {
						//this.setPosition(tgt.xCoord+0.5, tgt.yCoord+0.5, tgt.zCoord+0.5);
						if (rand.nextInt(2) == 0) {
							//this.setDead(); //negates need for shielding
						}
						else {
							this.setLocationAndAngles(tgt.xCoord+0.5, tgt.yCoord+0.5, tgt.zCoord+0.5, 0, 0);
							if (tgt.dimensionID != worldObj.provider.dimensionId && !worldObj.isRemote)
								ReikaEntityHelper.transferEntityToDimension(this, tgt.dimensionID, new BasicTeleporter((WorldServer)tgt.getWorld()));
						}
					}
				}
			}

			if (id instanceof NeutronBlock) {
				if (((NeutronBlock)id).onNeutron(this, world, x, y, z))
					return true;
			}
			else if (id instanceof NeutronShield) {
				NeutronShield ns = (NeutronShield)id;
				String type = this.getType().name();
				double c = Math.min(ns.getAbsorptionChance(type), RadiationShield.BEDINGOT.neutronAbsorbChance);
				boolean flag = ReikaRandomHelper.doWithChance(c);
				if (flag) {
					double c2 = Mth.clamp_double(ns.getRadiationSpawnMultiplier(world, x, y, z, type), 0, 1);
					if (ReikaRandomHelper.doWithChance(c2)) {
						this.spawnRadiationChance(world, x, y, z);
					}
					return true;
				}
			}

			if ((id == ReactorBlocks.FLUORITE.getBlockInstance() || id == ReactorBlocks.FLUORITEORE.getBlockInstance()) && meta < FluoriteTypes.colorList.length) {
				world.setBlock(x, y, z, id, meta+8, 3);
				world.func_147479_m(x, y, z);
			}

			RadiationShield rs = RadiationShield.getFrom(id, meta);
			if (rs != null && ReikaRandomHelper.doWithChance(rs.neutronAbsorbChance))
				return true;

			if (ReikaRandomHelper.doWithChance(speed.getIrradiatedAbsorptionChance())) {
				boolean flag = id.isOpaqueCube() ? (rand.nextBoolean() && id.getExplosionResistance(null, world, x, y, z, x, y, z) >= 12) || ReikaRandomHelper.getSafeRandomInt((int)(24 - id.getExplosionResistance(null, world, x, y, z, x, y, z))) == 0 : 255-id.getLightOpacity(world, x, y, z) == 0 ? ReikaRandomHelper.getSafeRandomInt(id.getLightOpacity(world, x, y, z)) > 0 : rand.nextInt(1000) == 0;
				if (flag) {
					this.spawnRadiationChance(world, x, y, z);
					if (ReikaRandomHelper.doWithChance(20))
						RadiationEffects.instance.transformBlock(world, x, y, z, RadiationIntensity.MODERATE);
					return true;
				}
			}
			return false;
		}

		return rand.nextInt(1000) == 0;
	}

	private boolean isNeutronTransparent(Block id) {
		return id == Blocks.air || id == botaniaPlatform || id == ttPlatform;
	}

	private void spawnRadiationChance(Level world, int x, int y, int z) {
		if (ReikaRandomHelper.doWithChance(2)) {
			AABB box = ReikaAABBHelper.getBlockAABB(x, y, z).expand(8, 8, 8);
			List inbox = world.getEntitiesWithinAABB(EntityRadiation.class, box);
			if (inbox.size() < 3)
				RadiationEffects.instance.contaminateArea(world, x, y, z, 1, 1, 0, false, RadiationIntensity.LOWLEVEL);
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
	public void writeSpawnData(ByteBuf data) {
		data.writeInt(type.ordinal());
	}

	@Override
	public void readSpawnData(ByteBuf data) {
		type = NeutronType.neutronList[data.readInt()];
		speed = type.getCreationSpeed();
	}

	public NeutronType getType() {
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
				case DECAY:
				case FUSION:
				case NULL:
				case WASTE:
				default:
					return NeutronSpeed.THERMAL;
				case BREEDER:
				case FISSION:
				case THORIUM:
					return NeutronSpeed.FAST;
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
	protected void readEntityFromNBT(CompoundTag NBT) {
		type = NeutronType.neutronList[NBT.getInteger("ntype")];
		speed = NeutronSpeed.speedList[NBT.getInteger("nspeed")];
	}

	@Override
	protected void writeEntityToNBT(CompoundTag NBT) {
		NBT.setInteger("ntype", this.getType().ordinal());
		NBT.setInteger("nspeed", this.getNeutronSpeed().ordinal());
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
