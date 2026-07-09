/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.auxiliary;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import reika.dragonapi.instantiable.RayTracer;
import reika.dragonapi.instantiable.data.immutable.BlockKey;
import reika.dragonapi.libraries.ReikaEntityHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.dragonapi.modregistry.ModWoodList;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.api.RadiationHandler.RadiationLevel;
import reika.reactorcraft.entities.EntityRadiation;
import reika.reactorcraft.registry.RadiationShield;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorItems;
import reika.rotarycraft.items.tools.bedrock.ItemBedrockArmor;

public class RadiationEffects {

	private static final Random rand = new Random();

	public static final RadiationEffects instance = new RadiationEffects();

	private RadiationEffects() {

	}

	// DRAGONAPI-PORT: dirtyBombs(CreeperExplodeEvent) — DragonAPI's CreeperExplodeEvent is not ported,
	// so radioactive creepers don't contaminate on explode yet. applyEffects still tags the creeper
	// (below) so this re-enables cleanly once the event lands.

	public boolean applyEffects(LivingEntity e, RadiationIntensity ri) {
		if (ri.causesHarm()) {
			if (!e.hasEffect(ReactorCraft.radiation)) {
				if (!this.isEntityImmuneToAll(e) && !ri.hasSufficientShielding(e)) {
					e.addEffect(this.getRadiationEffect(ri));
					return true;
				}
			}
			// DRAGONAPI-PORT: powered-armor decharge (ReikaEntityHelper.isEntityWearingPoweredArmor +
			// ReikaItemHelper.dechargeItem) not ported — radiation no longer drains powered armor yet.
			if (e instanceof Creeper) {
				((Creeper)e).getPersistentData().putBoolean("radioactive", true);
			}
		}
		return false;
	}

	public void applyPulseEffects(LivingEntity e, RadiationIntensity ri) {
		if (!e.hasEffect(ReactorCraft.radiation) && !this.isEntityImmuneToAll(e) && !ri.hasSufficientShielding(e))
			e.addEffect(this.getRadiationEffect(20, ri));
	}

	public boolean isEntityImmuneToAll(LivingEntity e) {
		return e instanceof Player && ((Player)e).isCreative();
	}

	public boolean hasHazmatSuit(LivingEntity e) {
		return ReikaEntityHelper.isEntityWearingFullSuitOf(e, (ItemStack is) -> this.isValidHazmatItem(is));
	}

	private boolean isValidHazmatItem(ItemStack is) {
		return is.is(ReactorItems.HAZMAT_HELMET.get()) || is.is(ReactorItems.HAZMAT_CHESTPLATE.get())
				|| is.is(ReactorItems.HAZMAT_LEGGINGS.get()) || is.is(ReactorItems.HAZMAT_BOOTS.get());
	}

	public double contaminateArea(Level world, int x, int y, int z, int range, float density, double force, boolean los, RadiationIntensity ri) {
		double frac = 1;
		int num = Math.max(1, (int)(Math.sqrt(range)*density));
		for (int i = 0; i < num; i++) {
			int dx = ReikaRandomHelper.getRandomPlusMinus(x, range);
			int dy = ReikaRandomHelper.getRandomPlusMinus(y, range);
			int dz = ReikaRandomHelper.getRandomPlusMinus(z, range);
			while(los && !this.isValidRadiationPosition(world, x, y, z, dx, dy, dz, 2)) {
				dx = ReikaRandomHelper.getRandomPlusMinus(x, range);
				dy = ReikaRandomHelper.getRandomPlusMinus(y, range);
				dz = ReikaRandomHelper.getRandomPlusMinus(z, range);
			}
			if (ReikaMathLibrary.py3d(dx-x, dy-y, dz-z) <= force) {
				frac -= 1D/num;
			}
			EntityRadiation rad = new EntityRadiation(world, range, ri);
			rad.snapTo(dx+0.5, dy+0.5, dz+0.5, 0, 0);
			if (!world.isClientSide())
				world.addFreshEntity(rad);
		}
		return frac;
	}

	private boolean isValidRadiationPosition(Level world, int x, int y, int z, int dx, int dy, int dz, double forceDist) {
		if (ReikaMathLibrary.py3d(dx-x, dy-y, dz-z) <= forceDist)
			return true;
		ArrayList<BlockKey> li = ReikaWorldHelper.getBlocksAlongVector(world, x+0.5, y+0.5, z+0.5, dx+0.5, dy+0.5, dz+0.5);
		double chance = 1;
		for (BlockKey bk : li) {
			RadiationShield rs = RadiationShield.getFrom(bk);
			if (rs != null) {
				chance *= 1-rs.radiationDeflectChance/100D;
			}
		}
		return chance > 0 && ReikaRandomHelper.doWithChance(chance);
	}

	public void transformBlock(Level world, int x, int y, int z, RadiationIntensity ri) {
		if (world.isClientSide())
			return;
		BlockPos pos = new BlockPos(x, y, z);
		BlockState state = world.getBlockState(pos);
		Block id = state.getBlock();
		if (id == Blocks.AIR || id == Blocks.DEAD_BUSH)
			return;

		if (ri.isAtLeast(RadiationIntensity.HIGHLEVEL)) {
			if (state.is(BlockTags.LEAVES) || ModWoodList.isModLeaf(id)) {
				world.removeBlock(pos, false);
			}
			else if (id instanceof SaplingBlock) {
				world.setBlockAndUpdate(pos, Blocks.DEAD_BUSH.defaultBlockState());
			}
			else if (id == Blocks.SHORT_GRASS || id == Blocks.FERN || id == Blocks.TALL_GRASS) {
				world.setBlockAndUpdate(pos, Blocks.DEAD_BUSH.defaultBlockState());
			}
			else if (id == Blocks.GRASS_BLOCK) {
				world.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
			}
			else if (id == Blocks.MOSSY_COBBLESTONE) {
				world.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
			}
			else if (state.is(BlockTags.SMALL_FLOWERS) || state.is(BlockTags.CROPS) || id == Blocks.SUGAR_CANE || id == Blocks.VINE
					|| id == Blocks.LILY_PAD || id == Blocks.CACTUS || id == Blocks.PUMPKIN || id == Blocks.PUMPKIN_STEM
					|| id == Blocks.MELON || id == Blocks.MELON_STEM || id == Blocks.COCOA) {
				world.destroyBlock(pos, true);
			}
		}

		// BLOCK-PORT: fluorite irradiation (legacy meta+8 glowing variant) — the ported fluorite ore is
		// per-colour (ReactorBlocks.FLUORITE_ORE map) with no single FLUORITE/FLUORITEORE block; wire the
		// irradiated blockstate here once the fluorite irradiation variant is ported.

		// DRAGONAPI-PORT: Thaumcraft node tainting (INode/Aspect) gated out — Thaumcraft not in build.
	}

	public MobEffectInstance getRadiationEffect(RadiationIntensity ri) {
		return this.getRadiationEffect(ri.potionDuration, ri);
	}

	private MobEffectInstance getRadiationEffect(int duration, RadiationIntensity ri) {
		return new MobEffectInstance(ReactorCraft.radiation, duration, ri.ordinal());
	}

	public void doOreIrradiation(Level world, int x, int y, int z, Player ep) {
		int r = 9;
		double dd = ep.distanceToSqr(x+0.5, y+0.5, z+0.5);
		if (dd <= r*r) {
			for (double dx = 0; dx <= 1; dx += 1) {
				for (double dy = 0; dy <= 1; dy += 1) {
					for (double dz = 0; dz <= 1; dz += 1) {
						for (double dh = 0; dh <= ep.getBbHeight(); dh += ep.getBbHeight()/2) {
							RayTracer tracer = new RayTracer(x+dx, y+dy, z+dz, ep.getX(), ep.getY()+dh, ep.getZ());
							if (tracer.isClearLineOfSight(world)) {
								int dur = (int)(200/Math.max(1, Math.sqrt(dd)));
								ep.addEffect(this.getRadiationEffect(dur, RadiationIntensity.LOWLEVEL));
								return;
							}
						}
					}
				}
			}
		}
	}

	public static enum RadiationIntensity implements RadiationLevel {
		BACKGROUND(0), //always
		LOWLEVEL(100), //neutrons
		MODERATE(1200), //plutonium, creepers, waste containers
		HIGHLEVEL(6000), //waste
		LETHAL(36000); //Meltdowns; Hazmat does not protect

		public static final RadiationIntensity[] radiationList = values();

		private final int potionDuration;

		private RadiationIntensity(int t) {
			potionDuration = t;
		}

		public boolean causesHarm() {
			return this != BACKGROUND;
		}

		public boolean isAtLeast(RadiationIntensity ri) {
			return this.ordinal() >= ri.ordinal();
		}

		public boolean hasSufficientShielding(LivingEntity e) {
			switch(this) {
				case BACKGROUND:
					return true;
				case LETHAL:
					return false;
				case HIGHLEVEL:
					return instance.hasHazmatSuit(e);
				case MODERATE: {
					Function<ItemStack, Boolean> func = (ItemStack is) -> instance.isValidHazmatItem(is) || ItemBedrockArmor.isValidBedrockArmorItem(is);
					return ReikaEntityHelper.isEntityWearingFullSuitOf(e, func);
				}
				case LOWLEVEL: {
					// DRAGONAPI-PORT: ReikaItemHelper.isDenseArmor not ported — dense armor no longer
					// shields low-level radiation; hazmat/bedrock still do.
					Function<ItemStack, Boolean> func = (ItemStack is) -> instance.isValidHazmatItem(is) || ItemBedrockArmor.isValidBedrockArmorItem(is);
					return ReikaEntityHelper.isEntityWearingFullSuitOf(e, func);
				}
			}
			return false;
		}
	}

	// DRAGONAPI-PORT: createMESystemEffect (Applied Energistics ME-system waste leak) gated out —
	// AE2 (appeng.api.*) + DragonAPI's MESystemEffect are not in this build.

}
