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

import net.minecraft.world.level.block.entity.BlockEntity;

import reika.dragonapi.modinteract.lua.LuaMethod;
import reika.reactorcraft.base.TileEntityReactorBase;

public class LuaReactorGetName extends LuaMethod {

	public LuaReactorGetName() {
		super("getName", TileEntityReactorBase.class);
	}

	@Override
	protected Object[] invoke(BlockEntity te, Object[] args) throws LuaMethodException, InterruptedException {
		return new Object[]{((TileEntityReactorBase)te).getName()};
	}

	@Override
	public String getDocumentation() {
		return "Returns the name of the machine.";
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
