/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.registry;

import java.util.HashMap;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public enum ReactorFuel {

	URANIUM(ReactorItems.FUEL_ROD.get(), 25, 3, 5, 20, 0),
	PLUTONIUM(ReactorItems.PLUTONIUM_ROD.get(), 30, 4, 10, 30, 0.025F);

	private final Item fuel;
	public final int fissionChance;
	public final int consumeChance;
	public final int wasteChance;
	public final int temperatureStep;
	public final float voidCoefficient;

	private static final HashMap<Item, ReactorFuel> itemMap = new HashMap<>();
	public static final ReactorFuel[] fuelList = values();

	private ReactorFuel(Item item, int fiss, int con, int waste, int temp, float v) {
		fuel = item;
		fissionChance = fiss;
		consumeChance = con;
		wasteChance = waste;
		temperatureStep = temp;
		voidCoefficient = v;
	}

	public boolean canProducePower() {
		return true;
	}

	public ItemStack getFuelItem() {
		return new ItemStack(fuel);
	}

	public ItemStack getFissionProduct(ItemStack input) {
		if (input == null || input.isEmpty())
			return null;
		return switch (this) {
			case PLUTONIUM -> ReactorItems.PLUTONIUM.getStackOfMetadata(input.getDamageValue() + 1);
			case URANIUM -> input.getDamageValue() >= ReactorItems.FUEL.getNumberMetadatas() - 1
					? ReactorItems.DEPLETED.getStackOf()
					: ReactorItems.FUEL.getStackOfMetadata(input.getDamageValue() + 1);
		};
	}

	public static ReactorFuel getFrom(ItemStack is) {
		return is != null && !is.isEmpty() ? itemMap.get(is.getItem()) : null;
	}

	static {
		for (ReactorFuel f : fuelList) {
			itemMap.put(f.fuel, f);
		}
	}
}
