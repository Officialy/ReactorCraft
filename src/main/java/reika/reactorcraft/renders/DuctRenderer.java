/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.renders;

import net.minecraft.tileentity.TileEntity;

import reika.dragonapi.interfaces.tileentity.RenderFetcher;
import reika.reactorcraft.base.ReactorRenderBase;
import reika.rotarycraft.auxiliary.interfaces.RenderableDuct;
import reika.rotarycraft.renders.PipeRenderer;

public class DuctRenderer extends ReactorRenderBase
{
	private static final PipeRenderer pipe = new PipeRenderer();

	@Override
	public void renderTileEntityAt(TileEntity tile, double par2, double par4, double par6, float par8)
	{
		RenderableDuct te = (RenderableDuct)tile;
		//RotaryRenderList.getRenderForMachine(MachineRegistry.PIPE).renderTileEntityAt(tile, par2, par4, par6, par8);
		pipe.renderTileEntityAt(tile, par2, par4, par6, par8);
	}

	@Override
	public String getImageFileName(RenderFetcher te) {
		return "";
	}
}
