/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.auxiliary.lua;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import reika.dragonapi.modinteract.lua.LuaMethod;
import reika.reactorcraft.base.TileEntityNuclearCore;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorTiles;

public class LuaReactorCheckFuel extends LuaMethod {

	public LuaReactorCheckFuel() {
		super("checkFuel", TileEntityNuclearCore.class);
	}

	@Override
	protected Object[] invoke(BlockEntity te, Object[] args) throws LuaMethodException, InterruptedException {
		TileEntityNuclearCore tile = (TileEntityNuclearCore)te;
		ReactorTiles r = tile.getTile();
		int fuel = 0;
		int maxfuel = 1;
		if (r == ReactorTiles.BREEDER) {
			maxfuel = 4*ReactorItems.BREEDERFUEL.getNumberMetadatas();
			for (int i = 0; i < 4; i++) {
				ItemStack is = tile.getStackInSlot(i);
				if (is != null) {
					if (is.getItem() == ReactorItems.BREEDERFUEL.getItemInstance()) {
						fuel += ReactorItems.BREEDERFUEL.getNumberMetadatas()-1-is.getDamageValue();
					}
				}
			}
		}
		else if (r == ReactorTiles.FUEL) {
			maxfuel = 4*ReactorItems.FUEL.getNumberMetadatas();
			for (int i = 0; i < 4; i++) {
				ItemStack is = tile.getStackInSlot(i);
				if (is != null) {
					if (is.getItem() == ReactorItems.FUEL.getItemInstance()) {
						fuel += ReactorItems.FUEL.getNumberMetadatas()-1-is.getDamageValue();
					}
					else if (is.getItem() == ReactorItems.PLUTONIUM.getItemInstance()) {
						fuel += ReactorItems.PLUTONIUM.getNumberMetadatas()-1-is.getDamageValue();
					}
				}
			}
		}
		return new Object[]{String.format("%.3f%s", 100F*fuel/maxfuel, "%")};
	}

	@Override
	public String getDocumentation() {
		return "Returns the fuel level of a nuclear fuel core.";
	}

	@Override
	public String getArgsAsString() {
		return "";
	}

	@Override
	public ReturnType getReturnType() {
		return ReturnType.STRING;
	}

}
