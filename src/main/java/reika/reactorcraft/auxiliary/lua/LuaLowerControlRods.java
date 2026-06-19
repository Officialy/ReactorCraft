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
import reika.reactorcraft.tileentities.fission.TileEntityCPU;

public class LuaLowerControlRods extends LuaMethod {

	public LuaLowerControlRods() {
		super("lowerRods", TileEntityCPU.class);
	}

	@Override
	protected Object[] invoke(TileEntity te, Object[] args) throws LuaMethodException, InterruptedException {
		((TileEntityCPU)te).lowerAllRods();
		return null;
	}

	@Override
	public String getDocumentation() {
		return "Lowers all the reactor control rods.";
	}

	@Override
	public String getArgsAsString() {
		return "";
	}

	@Override
	public ReturnType getReturnType() {
		return ReturnType.VOID;
	}

}
