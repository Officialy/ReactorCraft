/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.guis;

import net.minecraft.entity.player.EntityPlayer;

import reika.reactorcraft.base.ReactorGuiBase;
import reika.reactorcraft.base.TileEntityNuclearCore;
import reika.reactorcraft.container.ContainerNuclearCore;

public class GuiNuclearCore extends ReactorGuiBase {

	public GuiNuclearCore(EntityPlayer player, TileEntityNuclearCore fuel) {
		super(new ContainerNuclearCore(player, fuel), player, fuel);
		ySize = 182;
	}

	@Override
	public String getGuiTexture() {
		return "fuelrod";
	}

}
