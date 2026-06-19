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
import reika.reactorcraft.tileentities.TileEntityTurbineMeter;

public class LuaGetTurbineSpeed extends LuaMethod {

	public LuaGetTurbineSpeed() {
		super("getTurbineSpeed", TileEntityTurbineMeter.class);
	}

	@Override
	protected Object[] invoke(TileEntity te, Object[] args) throws LuaMethodException, InterruptedException {
		return new Object[]{((TileEntityTurbineMeter)te).getAnalogValue()};
	}

	@Override
	public String getDocumentation() {
		return "Returns the speed of the turbine.";
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
