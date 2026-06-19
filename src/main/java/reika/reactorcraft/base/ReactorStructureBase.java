package reika.reactorcraft.base;

import net.minecraftforge.common.util.ForgeDirection;

import reika.dragonapi.base.StructureBase;


public abstract class ReactorStructureBase extends StructureBase {

	public ForgeDirection dir;

	@Override
	protected void initDisplayData() {
		dir = ForgeDirection.EAST;
	}

	@Override
	protected void finishDisplayCall() {
		dir = null;
	}

}
