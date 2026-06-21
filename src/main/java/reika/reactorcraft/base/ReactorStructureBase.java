package reika.reactorcraft.base;

import net.minecraft.core.Direction;

import reika.dragonapi.base.StructureBase;


public abstract class ReactorStructureBase extends StructureBase {

	public Direction dir;

	@Override
	protected void initDisplayData() {
		dir = Direction.EAST;
	}

	@Override
	protected void finishDisplayCall() {
		dir = ItemStack.EMPTY;
	}

}
