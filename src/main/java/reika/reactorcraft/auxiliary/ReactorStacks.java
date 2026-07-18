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

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

import reika.reactorcraft.registry.ReactorDataComponents;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorItems;

public class ReactorStacks {

	// The legacy 1.7.10 "raw materials" item was a single metadata item (ReactorItems.RAW,
	// meta 0-9). The port split it into distinct items, but these refs were left pointing at
	// RAW.getStackOfMetadata(n) — and since RAW's backing item carries no durability,
	// setDamageValue clamps every meta>0 to 0, collapsing them all to fuel_dust. That silently
	// broke the isotope centrifuge (depleted output), ore drops (ammonium/calcite/lodestone/
	// thorium), and ammonia synthesis. Point each ref at its real distinct item instead.
	public static final ItemStack hf = ReactorItems.RAW.getStackOfMetadata(0); // meta 0: no distinct item, unused
	public static final ItemStack fueldust = ReactorItems.FUEL_DUST.toStack();
	public static final ItemStack depdust = ReactorItems.DEPLETED_DUST.toStack();
	public static final ItemStack ammonium = ReactorItems.AMMONIUM_DUST.toStack();
	public static final ItemStack lime = ReactorItems.LIME.toStack();
	public static final ItemStack calcite = ReactorItems.CALCITE.toStack();
	public static final ItemStack lodestone = ReactorItems.LODESTONE.toStack();
	public static final ItemStack thordust = ReactorItems.THORIUM_DUST.toStack();
	public static final ItemStack emeralddust = ReactorItems.EMERALD_DUST.toStack();
	public static final ItemStack wastedust = ReactorItems.WASTE_DUST.toStack();

	public static final ItemStack emptycan = ReactorItems.CANISTER_REF.getStackOf();
	public static final ItemStack uf6can = canister(ReactorFluids.UF6.get(), 1000);
	public static final ItemStack hfcan = canister(ReactorFluids.HF.get(), 1000);
	public static final ItemStack nh3can = canister(ReactorFluids.AMMONIA.get(), 1000);
	public static final ItemStack nacan = canister(ReactorFluids.SODIUM.get(), 1000);
	public static final ItemStack h2can = canister(ReactorFluids.DEUTERIUM.get(), 1000);
	public static final ItemStack h3can = canister(ReactorFluids.TRITIUM.get(), 1000);
	public static final ItemStack clcan = canister(ReactorFluids.CHLORINE.get(), 1000);
	public static final ItemStack ocan = canister(ReactorFluids.OXYGEN.get(), 1000);
	public static final ItemStack co2can = canister(ReactorFluids.CO2.get(), 1000);
	public static final ItemStack hotco2can = canister(ReactorFluids.HOT_CO2.get(), 1000);
	public static final ItemStack hotnacan = canister(ReactorFluids.HOT_SODIUM.get(), 1000);
	public static final ItemStack lican = canister(ReactorFluids.LITHIUM.get(), 1000);
	public static final ItemStack lifbecan = canister(ReactorFluids.LIFBE.get(), 1000);
	public static final ItemStack hotlifbecan = canister(ReactorFluids.HOT_LIFBE.get(), 1000);
	public static final ItemStack lifbefuelcan = canister(ReactorFluids.LIFBE_FUEL.get(), 1000);

	public static final ItemStack maxMagnet = ReactorItems.MAGNET.getStackOfMetadata(ReactorItems.MAGNET.getNumberMetadatas() - 1);
	public static final ItemStack weakerMagnet = ReactorItems.MAGNET.getStackOfMetadata(ReactorItems.MAGNET.getNumberMetadatas() - 2);

	private static ItemStack canister(Fluid fluid, int amount) {
		ItemStack s = ReactorItems.CANISTER_REF.getStackOf();
		s.set(ReactorDataComponents.CANISTER_FLUID.get(), SimpleFluidContent.copyOf(new FluidStack(fluid, amount)));
		return s;
	}

	public static ItemStack canisterOf(Fluid fluid) {
		return canister(fluid, 1000);
	}

	public static boolean isCanisterOf(ItemStack is, Fluid fluid) {
		if (is.isEmpty() || is.getItem() != ReactorItems.CANISTER.get())
			return false;
		SimpleFluidContent c = is.get(ReactorDataComponents.CANISTER_FLUID.get());
		return c != null && !c.isEmpty() && c.getFluid() == fluid;
	}

	public static boolean isEmptyCanister(ItemStack is) {
		if (is.isEmpty() || is.getItem() != ReactorItems.CANISTER.get())
			return false;
		SimpleFluidContent c = is.get(ReactorDataComponents.CANISTER_FLUID.get());
		return c == null || c.isEmpty();
	}

	private ReactorStacks() {}
}
