/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.base;

import reika.dragonapi.base.DragonAPIMod;
import reika.dragonapi.base.TileEntityBase;
import reika.dragonapi.base.TileEntityRenderBase;
import reika.dragonapi.interfaces.TextureFetcher;
import reika.reactorcraft.ReactorCraft;

public abstract class ReactorRenderBase extends TileEntityRenderBase implements TextureFetcher {

	@Override
	public final String getTextureFolder() {
		return "/Reika/ReactorCraft/Textures/BlockEntity/";
	}

	@Override
	protected Class getModClass() {
		return ReactorCraft.class;
	}

	@Override
	protected final boolean doRenderModel(TileEntityBase te) {
		return this.isValidMachineRenderPass(te);
	}

	@Override
	protected final DragonAPIMod getOwnerMod() {
		return ReactorCraft.instance;
	}

}
