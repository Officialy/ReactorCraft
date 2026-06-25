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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.reactorcraft.auxiliary.RadiationEffects.RadiationIntensity;

/**
 * 26.2 port of the 1.7.10 {@code PotionRadiation}. The legacy {@code performEffect}/{@code isReady}
 * overrides map onto {@link #applyEffectTick(LivingEntity, int)} /
 * {@link #shouldApplyEffectTickThisTick(int, int)}. The original per-player {@code TickHandler} that
 * stripped beneficial potions ("PotionMapCMEAvoidance") is folded into the periodic tick here.
 */
public class MobEffectRadiation extends MobEffect {

	public MobEffectRadiation(MobEffectCategory category, int color) {
		super(category, color);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
		return tickCount % 20 == 5;
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity e, int amplification) {
		boolean peaceful = e.level().getDifficulty() == Difficulty.PEACEFUL;
		double c = peaceful ? 75 : 50;
		if (amplification >= RadiationIntensity.HIGHLEVEL.ordinal())
			c *= 1.1;
		if (amplification >= RadiationIntensity.LETHAL.ordinal())
			c *= 1.25;
		if (ReikaRandomHelper.doWithChance(e.getHealth() / e.getMaxHealth() * c)) {
			int amt = peaceful ? 2 : 1;
			// MOD-PORT: legacy ReactorCraft.radiationDamage custom DamageSource -> vanilla magic source.
			e.hurtServer(level, level.damageSources().magic(), amt);
		}

		if (e instanceof Player ep) {
			FoodData food = ep.getFoodData();
			food.setFoodLevel(Math.min(food.getFoodLevel(), Math.max(1, 8 - 2 * amplification)));
			food.setSaturation(Math.max(0, 4 - amplification));
			// DRAGONAPI-PORT: ReikaPlayerAPI.setPlayerWalkSpeed (raw movement-attribute override) not ported;
			// the slowdown is instead approximated by the beneficial-effect stripping below.
			this.avoidBeneficialEffects(ep);
		}
		return true;
	}

	/** Mirrors the original PotionMapCMEAvoidance: radiation suppresses helpful potions and adds malaise. */
	private void avoidBeneficialEffects(Player e) {
		e.removeEffect(MobEffects.REGENERATION);
		e.removeEffect(MobEffects.ABSORPTION);
		e.removeEffect(MobEffects.SPEED);
		e.removeEffect(MobEffects.HASTE);
		e.removeEffect(MobEffects.STRENGTH);
		e.removeEffect(MobEffects.JUMP_BOOST);
		e.removeEffect(MobEffects.FIRE_RESISTANCE);
		e.removeEffect(MobEffects.RESISTANCE);
		e.removeEffect(MobEffects.NIGHT_VISION);

		if (!e.hasEffect(MobEffects.NAUSEA))
			e.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0));
		e.addEffect(new MobEffectInstance(MobEffects.POISON, 20, 0));
	}

}
