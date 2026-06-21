/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;

import reika.dragonapi.base.CoreContainer;
import reika.dragonapi.libraries.io.ReikaPacketHelper;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.tileentities.processing.TileEntitySynthesizer;

public class ContainerSynthesizer extends CoreContainer {

	private TileEntitySynthesizer synth;

	public ContainerSynthesizer(EntityPlayer player, TileEntitySynthesizer te) {
		super(player, te);

		this.addSlotToContainer(new Slot(te, 0, 35, 62));
		this.addSlotToContainer(new Slot(te, 1, 80, 26));
		this.addSlotToContainer(new Slot(te, 2, 80, 44));

		this.addPlayerInventoryWithOffset(player, 0, 9);

		synth = te;
	}

	/**
	 * Updates crafting matrix; called from onCraftMatrixChanged. Args: none
	 */
	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		for (int i = 0; i < crafters.size(); i++)
		{
			ICrafting icrafting = (ICrafting)crafters.get(i);

			icrafting.sendProgressBarUpdate(this, 0, synth.timer);
		}
		ReikaPacketHelper.sendTankSyncPacket(ReactorCraft.packetChannel, synth, "tank");
		ReikaPacketHelper.sendTankSyncPacket(ReactorCraft.packetChannel, synth, "water");
	}

	@Override
	public void updateProgressBar(int par1, int par2)
	{
		switch(par1) {
		case 0: synth.timer = par2; break;
		}
	}
}
