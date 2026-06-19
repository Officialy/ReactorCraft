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

import net.minecraft.tileentity.TileEntity;

import reika.dragonapi.modinteract.lua.LuaMethod;
import reika.reactorcraft.auxiliary.SteamTile;

public class LuaGetSteam extends LuaMethod {

	public LuaGetSteam() {
		super("getSteam", SteamTile.class);
	}

	@Override
	protected Object[] invoke(TileEntity te, Object[] args) throws LuaMethodException, InterruptedException {
		return new Object[]{((SteamTile)te).getSteam()};
	}

	@Override
	public String getDocumentation() {
		return "Returns the steam content of a machine.";
	}

	@Override
	public String getArgsAsString() {
		return "";
	}

	@Override
	public ReturnType getReturnType() {
		return ReturnType.INTEGER;
	}

}
