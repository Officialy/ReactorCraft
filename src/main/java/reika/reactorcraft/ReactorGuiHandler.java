/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import reika.dragonapi.base.CoreContainer;
import reika.dragonapi.libraries.ReikaInventoryHelper;
import reika.reactorcraft.base.TileEntityNuclearCore;
import reika.reactorcraft.container.ContainerCentrifuge;
import reika.reactorcraft.container.ContainerElectrolyzer;
import reika.reactorcraft.container.ContainerNuclearCore;
import reika.reactorcraft.container.ContainerPebbleBed;
import reika.reactorcraft.container.ContainerProcessor;
import reika.reactorcraft.container.ContainerSynthesizer;
import reika.reactorcraft.container.ContainerThoriumCore;
import reika.reactorcraft.container.ContainerWasteContainer;
import reika.reactorcraft.container.ContainerWasteDecayer;
import reika.reactorcraft.container.ContainerWasteStorage;
import reika.reactorcraft.guis.GuiCPU;
import reika.reactorcraft.guis.GuiCentrifuge;
import reika.reactorcraft.guis.GuiElectrolyzer;
import reika.reactorcraft.guis.GuiNuclearCore;
import reika.reactorcraft.guis.GuiPebbleBed;
import reika.reactorcraft.guis.GuiProcessor;
import reika.reactorcraft.guis.GuiReactorBook;
import reika.reactorcraft.guis.GuiReactorBookPage;
import reika.reactorcraft.guis.GuiSynthesizer;
import reika.reactorcraft.guis.GuiThoriumCore;
import reika.reactorcraft.guis.GuiWasteContainer;
import reika.reactorcraft.guis.GuiWasteDecayer;
import reika.reactorcraft.guis.GuiWasteStorage;
import reika.reactorcraft.registry.ReactorBook;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.fission.TileEntityCPU;
import reika.reactorcraft.tileentities.fission.thorium.TileEntityThoriumCore;
import reika.reactorcraft.tileentities.htgr.TileEntityPebbleBed;
import reika.reactorcraft.tileentities.processing.TileEntityCentrifuge;
import reika.reactorcraft.tileentities.processing.TileEntityElectrolyzer;
import reika.reactorcraft.tileentities.processing.TileEntitySynthesizer;
import reika.reactorcraft.tileentities.processing.TileEntityUProcessor;
import reika.reactorcraft.tileentities.processing.TileEntityWasteDecayer;
import reika.reactorcraft.tileentities.waste.TileEntityWasteContainer;
import reika.reactorcraft.tileentities.waste.TileEntityWasteStorage;

import cpw.mods.fml.common.network.IGuiHandler;

public class ReactorGuiHandler implements IGuiHandler {

	public static final ReactorGuiHandler instance = new ReactorGuiHandler();

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == 0) {
			ReactorTiles r = ReactorTiles.getTE(world, x, y, z);
			if (r != null) {
				TileEntity te = world.getTileEntity(x, y, z);
				switch(r) {
					case FUEL:
					case BREEDER:
						return new ContainerNuclearCore(player, (TileEntityNuclearCore)te);
					case THORIUM:
						return new ContainerThoriumCore(player, (TileEntityThoriumCore)te);
					case WASTECONTAINER:
						return new ContainerWasteContainer(player, (TileEntityWasteContainer)te);
					case WASTEDECAYER:
						return new ContainerWasteDecayer(player, (TileEntityWasteDecayer)te);
					case PROCESSOR:
						return new ContainerProcessor(player, (TileEntityUProcessor)te);
					case CENTRIFUGE:
						return new ContainerCentrifuge(player, (TileEntityCentrifuge)te);
					case SYNTHESIZER:
						return new ContainerSynthesizer(player, (TileEntitySynthesizer)te);
					case ELECTROLYZER:
						return new ContainerElectrolyzer(player, (TileEntityElectrolyzer)te);
					case STORAGE:
						return new ContainerWasteStorage(player, (TileEntityWasteStorage)te);
					case PEBBLEBED:
						return new ContainerPebbleBed(player, (TileEntityPebbleBed)te);
					case CPU:
						int slot = ReikaInventoryHelper.locateIDInInventory(ReactorItems.REMOTE.getItemInstance(), player.inventory);
						return new CoreContainer(player, te).setAlwaysInteractable().addSlotRelay(player.inventory, slot);
					default:
						return null;
				}
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
		ReactorTiles r = ReactorTiles.getTE(world, x, y, z);

		if (ID == 10)
			return new GuiReactorBook(player, world, 0, 0);
		if (ID == 11) {
			return new GuiReactorBook(player, world, ReactorBook.getScreen(r, te), ReactorBook.getPage(r, te));
		}
		if (ID == 12) {
			return new GuiReactorBookPage(player, world, ReactorBook.getScreen(r, te), ReactorBook.getPage(r, te));
		}

		if (r != null) {
			switch(r) {
				case FUEL:
				case BREEDER:
					return new GuiNuclearCore(player, (TileEntityNuclearCore)te);
				case THORIUM:
					return new GuiThoriumCore(player, (TileEntityThoriumCore)te);
				case WASTECONTAINER:
					return new GuiWasteContainer(player, (TileEntityWasteContainer)te);
				case WASTEDECAYER:
					return new GuiWasteDecayer(player, (TileEntityWasteDecayer)te);
				case PROCESSOR:
					return new GuiProcessor(player, (TileEntityUProcessor)te);
				case CENTRIFUGE:
					return new GuiCentrifuge(player, (TileEntityCentrifuge)te);
				case SYNTHESIZER:
					return new GuiSynthesizer(player, (TileEntitySynthesizer)te);
				case ELECTROLYZER:
					return new GuiElectrolyzer(player, (TileEntityElectrolyzer)te);
				case STORAGE:
					return new GuiWasteStorage(player, (TileEntityWasteStorage)te);
				case PEBBLEBED:
					return new GuiPebbleBed(player, (TileEntityPebbleBed)te);
				case CPU:
					return new GuiCPU(player, (TileEntityCPU)te);
				default:
					return null;
			}
		}
		return null;
	}

}
