/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.items;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import reika.dragonapi.libraries.ReikaPlayerAPI;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.registry.ReikaParticleHelper;
import reika.reactorcraft.base.ItemReactorTool;
import reika.reactorcraft.entities.EntityRadiation;
import reika.rotarycraft.api.interfaces.ChargeableTool;

public class ItemRadiationCleaner extends ItemReactorTool implements ChargeableTool {

	private static final int CAPACITY = 32000;
	private static final int WATER_PER_TICK = 25;
	private static final int TICK_PER_KJ = 5;

	public ItemRadiationCleaner(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level world, Player ep, InteractionHand hand) {
		ItemStack is = ep.getItemInHand(hand);
		if (!world.isClientSide() && this.getWater(is) < CAPACITY) {
			BlockHitResult mov = ReikaPlayerAPI.getLookedAtBlock(ep, 5, true);
			if (mov != null && mov.getType() == HitResult.Type.BLOCK) {
				BlockPos pos = mov.getBlockPos();
				BlockState bs = world.getBlockState(pos);
				if (bs.getBlock() == Blocks.WATER && bs.getFluidState().isSource()) {
					this.addWater(is, 1000);
					world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
					return InteractionResult.SUCCESS;
				}
			}
		}

		ep.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public void onUseTick(Level world, LivingEntity liv, ItemStack is, int count) {
		if (is.getDamageValue() > 0 && this.getWater(is) > 0) {
			double r = 1;
			double d = ReikaRandomHelper.getRandomPlusMinus(2.5, 2);
			Vec3 vec = liv.getLookAngle();
			double dx = liv.getX()+vec.x*d;
			double dy = liv.getY()+liv.getEyeHeight()+vec.y*d;
			double dz = liv.getZ()+vec.z*d;
			if (count%TICK_PER_KJ == 0) {
				AABB box = new AABB(dx, dy, dz, dx, dy, dz).inflate(r, r, r);
				List<EntityRadiation> li = liv.level().getEntitiesOfClass(EntityRadiation.class, box);
				for (EntityRadiation e : li) {
					e.clean();
				}
			}
			int n = ReikaRandomHelper.getRandomPlusMinus(8, 4);
			for (int i = 0; i < n; i++) {
				double v = ReikaRandomHelper.getRandomPlusMinus(0.1875, 0.0625);
				double vx = vec.x*v;
				double vy = vec.y*v;
				double vz = vec.z*v;

				vx = ReikaRandomHelper.getRandomPlusMinus(vx, 0.001);
				vz = ReikaRandomHelper.getRandomPlusMinus(vz, 0.001);

				ReikaParticleHelper.RAIN.spawnAt(liv.level(), dx, dy, dz, vx, vy, vz);
			}
		}
	}

	@Override
	public boolean releaseUsing(ItemStack is, Level world, LivingEntity liv, int ticksLeft) {
		boolean creative = liv instanceof Player p && p.isCreative();
		if (!creative) {
			int used = this.getUseDuration(is, liv)-ticksLeft;
			is.setDamageValue(Math.max(0, is.getDamageValue()-(used/TICK_PER_KJ)));
			this.addWater(is, -WATER_PER_TICK*used);
		}
		return true;
	}

	@Override
	public void appendHoverText(ItemStack is, Item.TooltipContext ctx, List<Component> li, TooltipFlag flag) {
		li.add(Component.literal(String.format("Water: %d/%d mB", this.getWater(is), CAPACITY)));
	}

	private int getWater(ItemStack is) {
		return is.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr("water", 0);
	}

	private void addWater(ItemStack is, int amt) {
		this.setWater(is, amt+this.getWater(is));
	}

	private void setWater(ItemStack is, int level) {
		CustomData.update(DataComponents.CUSTOM_DATA, is, t -> t.putInt("water", Math.min(CAPACITY, level)));
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack is) {
		return ItemUseAnimation.BOW;
	}

	@Override
	public int getUseDuration(ItemStack is, LivingEntity liv) {
		return Math.min(72000, Math.min(is.getDamageValue()*TICK_PER_KJ, this.getWater(is)/WATER_PER_TICK));
	}

	@Override
	public int setCharged(ItemStack is, int charge, boolean strongcoil) {
		int ret = is.getDamageValue();
		is.setDamageValue(charge);
		return ret;
	}
}
