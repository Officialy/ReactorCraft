/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.registry;

import java.util.ArrayList;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.oredict.ShapedOreRecipe;

import reika.dragonapi.exception.RegistrationException;
import reika.dragonapi.instantiable.data.maps.BlockMap;
import reika.dragonapi.interfaces.registry.TileEnum;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.dragonapi.modregistry.PowerTypes;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.LinkableReactorCore;
import reika.reactorcraft.auxiliary.MultiBlockTile;
import reika.reactorcraft.auxiliary.ReactorCoreTE;
import reika.reactorcraft.auxiliary.ReactorPowerReceiver;
import reika.reactorcraft.tileentities.TileEntityFusionMarker;
import reika.reactorcraft.tileentities.TileEntityGasCollector;
import reika.reactorcraft.tileentities.TileEntityGasDuct;
import reika.reactorcraft.tileentities.TileEntityHeatPipe;
import reika.reactorcraft.tileentities.TileEntityHeavyPump;
import reika.reactorcraft.tileentities.TileEntityMagneticPipe;
import reika.reactorcraft.tileentities.TileEntityNeutronReflector;
import reika.reactorcraft.tileentities.TileEntityReactorFlywheel;
import reika.reactorcraft.tileentities.TileEntityReactorGenerator;
import reika.reactorcraft.tileentities.TileEntitySolarTop;
import reika.reactorcraft.tileentities.TileEntitySteamDiffuser;
import reika.reactorcraft.tileentities.TileEntityTurbineMeter;
import reika.reactorcraft.tileentities.fission.TileEntityCPU;
import reika.reactorcraft.tileentities.fission.TileEntityControlRod;
import reika.reactorcraft.tileentities.fission.TileEntityFuelRod;
import reika.reactorcraft.tileentities.fission.TileEntityReactorBoiler;
import reika.reactorcraft.tileentities.fission.TileEntityWaterCell;
import reika.reactorcraft.tileentities.fission.breeder.TileEntityBreederCore;
import reika.reactorcraft.tileentities.fission.breeder.TileEntitySodiumHeater;
import reika.reactorcraft.tileentities.fission.thorium.TileEntityFuelDump;
import reika.reactorcraft.tileentities.fission.thorium.TileEntityThoriumCore;
import reika.reactorcraft.tileentities.fusion.TileEntityFusionHeater;
import reika.reactorcraft.tileentities.fusion.TileEntityFusionInjector;
import reika.reactorcraft.tileentities.fusion.TileEntityNeutronAbsorber;
import reika.reactorcraft.tileentities.fusion.TileEntitySolenoidMagnet;
import reika.reactorcraft.tileentities.fusion.TileEntityToroidMagnet;
import reika.reactorcraft.tileentities.htgr.TileEntityCO2Heater;
import reika.reactorcraft.tileentities.htgr.TileEntityPebbleBed;
import reika.reactorcraft.tileentities.powergen.TileEntityCentrifugalTurbine;
import reika.reactorcraft.tileentities.powergen.TileEntityCondenser;
import reika.reactorcraft.tileentities.powergen.TileEntityHeatExchanger;
import reika.reactorcraft.tileentities.powergen.TileEntityHiPTurbine;
import reika.reactorcraft.tileentities.powergen.TileEntityReactorPump;
import reika.reactorcraft.tileentities.powergen.TileEntitySolarExchanger;
import reika.reactorcraft.tileentities.powergen.TileEntitySteamGrate;
import reika.reactorcraft.tileentities.powergen.TileEntitySteamLine;
import reika.reactorcraft.tileentities.powergen.TileEntityTurbineCore;
import reika.reactorcraft.tileentities.processing.TileEntityCentrifuge;
import reika.reactorcraft.tileentities.processing.TileEntityElectrolyzer;
import reika.reactorcraft.tileentities.processing.TileEntitySynthesizer;
import reika.reactorcraft.tileentities.processing.TileEntityTritizer;
import reika.reactorcraft.tileentities.processing.TileEntityUProcessor;
import reika.reactorcraft.tileentities.processing.TileEntityWasteDecayer;
import reika.reactorcraft.tileentities.waste.TileEntityWasteContainer;
import reika.reactorcraft.tileentities.waste.TileEntityWastePipe;
import reika.reactorcraft.tileentities.waste.TileEntityWasteStorage;
import reika.rotarycraft.auxiliary.recipemanagers.recipehandler.RecipeLevel;
import reika.rotarycraft.auxiliary.recipemanagers.WorktableRecipes;
import reika.rotarycraft.registry.ConfigRegistry;

import cpw.mods.fml.common.registry.GameRegistry;

public enum ReactorTiles implements TileEnum {

	FUEL("machine.fuel", 						ReactorBlocks.REACTOR,			TileEntityFuelRod.class, 		0),
	CONTROL("machine.control", 					ReactorBlocks.MODELREACTOR,		TileEntityControlRod.class, 	6, "RenderControl"),
	COOLANT("machine.coolant", 					ReactorBlocks.REACTOR,			TileEntityWaterCell.class, 		2),
	CPU("machine.cpu", 							ReactorBlocks.MACHINE,			TileEntityCPU.class, 			0),
	TURBINECORE("machine.turbine", 				ReactorBlocks.MODELREACTOR,		TileEntityTurbineCore.class, 	0, "RenderTurbine"),
	CONDENSER("machine.condenser", 				ReactorBlocks.MODELREACTOR,		TileEntityCondenser.class, 		1, "RenderCondenser"),
	STEAMLINE("machine.steamline", 				ReactorBlocks.LINE,				TileEntitySteamLine.class, 		2, "RenderWaterLine"),
	FLUIDEXTRACTOR("machine.heavypump", 		ReactorBlocks.MODELMACHINE,		TileEntityHeavyPump.class, 		0, "RenderHeavyPump"),
	CENTRIFUGE("machine.isocentrifuge", 		ReactorBlocks.MODELMACHINE,		TileEntityCentrifuge.class, 	1, "RenderCentrifuge"),
	PROCESSOR("machine.processor", 				ReactorBlocks.MODELMACHINE,		TileEntityUProcessor.class, 	2, "RenderProcessor"),
	WASTECONTAINER("machine.wastecontainer", 	ReactorBlocks.MACHINE,			TileEntityWasteContainer.class, 2),
	BOILER("machine.reactorboiler", 			ReactorBlocks.REACTOR,			TileEntityReactorBoiler.class, 	3),
	GRATE("machine.grate", 						ReactorBlocks.MODELREACTOR,		TileEntitySteamGrate.class, 	3, "RenderSteamGrate"),
	PUMP("machine.reactorpump", 				ReactorBlocks.MODELREACTOR,		TileEntityReactorPump.class, 	4, "RenderReactorPump"),
	SYNTHESIZER("machine.synthesizer", 			ReactorBlocks.MACHINE,			TileEntitySynthesizer.class, 	1),
	MAGNET("machine.magnet", 					ReactorBlocks.MODELREACTOR,		TileEntityToroidMagnet.class, 	5, "RenderMagnet"),
	ELECTROLYZER("machine.electrolyzer", 		ReactorBlocks.MODELMACHINE,		TileEntityElectrolyzer.class, 	5, "RenderElectrolyzer"),
	TRITIZER("machine.tritizer", 				ReactorBlocks.REACTOR,			TileEntityTritizer.class, 		4),
	BREEDER("machine.breedercore", 				ReactorBlocks.REACTOR,			TileEntityBreederCore.class, 	5),
	SODIUMBOILER("machine.sodiumboiler", 		ReactorBlocks.REACTOR,			TileEntitySodiumHeater.class, 	6),
	EXCHANGER("machine.exchanger", 				ReactorBlocks.MODELMACHINE,		TileEntityHeatExchanger.class, 	4, "RenderExchanger"),
	STORAGE("machine.storage", 					ReactorBlocks.MODELMACHINE,		TileEntityWasteStorage.class,	3, "RenderWasteStorage"),
	INJECTOR("machine.injector", 				ReactorBlocks.REACTOR,			TileEntityFusionInjector.class, 7),
	HEATER("machine.fusionheater", 				ReactorBlocks.REACTOR,			TileEntityFusionHeater.class, 	8),
	GASPIPE("machine.gasduct", 					ReactorBlocks.DUCT,				TileEntityGasDuct.class, 		0, "DuctRenderer"),
	MAGNETPIPE("machine.magnetpipe", 			ReactorBlocks.DUCT,				TileEntityMagneticPipe.class, 	1, "DuctRenderer"),
	ABSORBER("machine.absorber",				ReactorBlocks.REACTOR,			TileEntityNeutronAbsorber.class,1),
	SOLENOID("machine.solenoid",				ReactorBlocks.MODELREACTOR,		TileEntitySolenoidMagnet.class,	9, "RenderSolenoid"),
	COLLECTOR("machine.collector",				ReactorBlocks.MODELMACHINE,		TileEntityGasCollector.class,	6, "RenderGasCollector"),
	PEBBLEBED("machine.pebblebed",				ReactorBlocks.REACTOR,			TileEntityPebbleBed.class,		9),
	CO2HEATER("machine.co2heater",				ReactorBlocks.REACTOR,			TileEntityCO2Heater.class,		10),
	FLYWHEEL("machine.turbinewheel",			ReactorBlocks.MODELMACHINE,		TileEntityReactorFlywheel.class,7, "RenderTurbineWheel"),
	REFLECTOR("machine.reflector",				ReactorBlocks.REACTOR,			TileEntityNeutronReflector.class,11),
	GENERATOR("machine.reactorgenerator",		ReactorBlocks.MODELMACHINE,		TileEntityReactorGenerator.class,8, "RenderGenerator"),
	MARKER("machine.fusionmarker",				ReactorBlocks.MODELMACHINE,		TileEntityFusionMarker.class,	9,	"RenderFusionMarker"),
	TURBINEMETER("machine.turbinemeter",		ReactorBlocks.MACHINE,			TileEntityTurbineMeter.class,	3),
	BIGTURBINE("machine.bigturbine", 			ReactorBlocks.MODELREACTOR,		TileEntityHiPTurbine.class,		7, "RenderBigTurbine"),
	DIFFUSER("machine.steamdiffuser",			ReactorBlocks.MODELMACHINE,		TileEntitySteamDiffuser.class,	10, "RenderSteamDiffuser"),
	THORIUM("machine.thorium",					ReactorBlocks.REACTOR,			TileEntityThoriumCore.class,	12),
	WASTEPIPE("machine.wastepipe",				ReactorBlocks.DUCT,				TileEntityWastePipe.class,		2),
	FUELDUMP("machine.fueldump",				ReactorBlocks.REACTOR,			TileEntityFuelDump.class,		13),
	SOLAR("machine.solarexchange",				ReactorBlocks.MODELMACHINE,		TileEntitySolarExchanger.class,	11, "RenderSolarExchanger"),
	SOLARTOP("machine.solartop",				ReactorBlocks.MODELMACHINE,		TileEntitySolarTop.class,		12, "RenderSolarTop"),
	MINITURBINE("machine.miniturbine", 			ReactorBlocks.MODELREACTOR,		TileEntityCentrifugalTurbine.class,	8, "RenderMiniTurbine"),
	HEATPIPE("machine.heatpipe", 				ReactorBlocks.LINE,				TileEntityHeatPipe.class, 		3, "RenderWaterLine"),
	WASTEDECAYER("machine.wastedecayer",		ReactorBlocks.REACTOR,			TileEntityWasteDecayer.class,	14);

	private final String name;
	private final Class teClass;
	private final int meta;
	private String render;
	private final ReactorBlocks blockInstance;
	private BlockEntity renderInstance;

	private static final BlockMap<ReactorTiles> reactorMappings = new BlockMap();

	public static final ReactorTiles[] TEList = values();

	private ReactorTiles(String n, ReactorBlocks block, Class<? extends BlockEntity> tile, int m) {
		this(n, block, tile, m, null);
	}

	private ReactorTiles(String n, ReactorBlocks block, Class<? extends BlockEntity> tile, int m, String r) {
		teClass = tile;
		name = n;
		render = r;
		meta = m;
		blockInstance = block;
	}

	public String getName() {
		return StatCollector.translateToLocal(name);
	}

	public Class getTEClass() {
		return teClass;
	}

	public static ArrayList<ReactorTiles> getTilesOfBlock(ReactorBlocks b) {
		ArrayList li = new ArrayList();
		for (int i = 0; i < TEList.length; i++) {
			if (TEList[i].blockInstance == b)
				li.add(TEList[i]);
		}
		return li;
	}

	public static BlockEntity createTEFromIDAndMetadata(Block id, int meta) {
		ReactorTiles index = getMachineFromIDandMetadata(id, meta);
		if (index == null) {
			ReactorCraft.logger.logError("ID "+id+" and metadata "+meta+" are not a valid machine identification pair!");
			return null;
		}
		Class TEClass = index.teClass;
		try {
			return (BlockEntity)TEClass.newInstance();
		}
		catch (InstantiationException e) {
			e.printStackTrace();
			throw new RegistrationException(ReactorCraft.instance, "ID "+id+" and Metadata "+meta+" failed to instantiate its BlockEntity of "+TEClass);
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RegistrationException(ReactorCraft.instance, "ID "+id+" and Metadata "+meta+" failed illegally accessed its BlockEntity of "+TEClass);
		}
	}

	public static ReactorTiles getMachineFromIDandMetadata(Block id, int meta) {
		return reactorMappings.get(id, meta);
	}

	public boolean isAvailableInCreativeInventory() {
		if (this == GENERATOR)
			return PowerTypes.RF.isLoaded();
		return true;
	}

	public static ReactorTiles getTE(BlockGetter iba, int x, int y, int z) {
		Block id = iba.getBlock(x, y, z);
		int meta = iba.getBlockMetadata(x, y, z);
		return getMachineFromIDandMetadata(id, meta);
	}

	public ItemStack getCraftedProduct() {
		return new ItemStack(ReactorItems.PLACER.getItemInstance(), 1, this.ordinal());
	}

	public BlockEntity createTEInstanceForRender() {
		if (renderInstance == null) {
			try {
				renderInstance = (BlockEntity)teClass.newInstance();
			}
			catch (InstantiationException e) {
				e.printStackTrace();
				throw new RegistrationException(ReactorCraft.instance, "Could not create TE instance to render "+this);
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new RegistrationException(ReactorCraft.instance, "Could not create TE instance to render "+this);
			}
		}
		return renderInstance;
	}

	public boolean hasRender() {
		return render != null;
	}

	public String getRenderer() {
		if (!this.hasRender())
			throw new RuntimeException("Machine "+name+" has no render to call!");
		return "Reika.ReactorCraft.Renders."+render;
	}

	public int getTextureStates() {
		switch(this) {
			case COOLANT:
				return TileEntityWaterCell.LiquidStates.list.length;
			case BOILER:
			case SODIUMBOILER:
			case CO2HEATER:
				return 4;
			case PEBBLEBED:
			case FUEL:
			case BREEDER:
			case TRITIZER:
			case THORIUM:
			case WASTEDECAYER:
				return 5;
			case INJECTOR:
				return 3;
			case TURBINEMETER:
				return 3;
			default:
				return 1;
		}
	}

	public boolean hasSidedTextures() {
		return false;
	}

	public boolean isEndTextured() {
		switch(this) {
			//case FUEL:
			case CONTROL:
			case WASTECONTAINER:
			case SYNTHESIZER:
			case FUELDUMP:
				//case BREEDER:
				//case TRITIZER:
				return true;
			default:
				return false;
		}
	}

	public boolean isTopSameTextureAsBottom() {
		switch(this) {
			case FUELDUMP:
				return false;
			default:
				return true;
		}
	}

	public boolean hasTextureStates() {
		return this.getTextureStates() > 1;
	}

	public Block getBlock() {
		return this.getBlockInstance();
	}

	public Block getBlockInstance() {
		return blockInstance.getBlockInstance();
	}

	public int getBlockMetadata() {
		return meta%16;
	}

	public boolean renderInPass1() {
		switch(this) {
			case PROCESSOR:
			case MAGNET:
			case GASPIPE:
			case MAGNETPIPE:
			case COLLECTOR:
			case SOLARTOP:
				return true;
			default:
				return false;
		}
	}

	public boolean isDummiedOut() {
		return false;
	}

	public void addRecipe(IRecipe ir) {
		if (!this.isDummiedOut()) {
			WorktableRecipes.getInstance().addRecipe(ir, RecipeLevel.CORE);
			if (ConfigRegistry.TABLEMACHINES.getState()) {
				GameRegistry.addRecipe(ir);
			}
		}
	}

	public void addRecipe(ItemStack is, Object... obj) {
		if (!this.isDummiedOut()) {
			WorktableRecipes.getInstance().addRecipe(is, RecipeLevel.CORE, obj);
			if (ConfigRegistry.TABLEMACHINES.getState()) {
				GameRegistry.addRecipe(is, obj);
			}
		}
	}

	public void addCrafting(Object... obj) {
		if (!this.isDummiedOut()) {
			WorktableRecipes.getInstance().addRecipe(this.getCraftedProduct(), RecipeLevel.CORE, obj);
			if (ConfigRegistry.TABLEMACHINES.getState()) {
				GameRegistry.addRecipe(this.getCraftedProduct(), obj);
			}
		}
	}

	public void addSizedCrafting(int num, Object... obj) {
		if (!this.isDummiedOut()) {
			WorktableRecipes.getInstance().addRecipe(ReikaItemHelper.getSizedItemStack(this.getCraftedProduct(), num), RecipeLevel.CORE, obj);
			if (ConfigRegistry.TABLEMACHINES.getState()) {
				GameRegistry.addRecipe(ReikaItemHelper.getSizedItemStack(this.getCraftedProduct(), num), obj);
			}
		}
	}

	public void addSizedOreCrafting(int num, Object... obj) {
		if (!this.isDummiedOut()) {
			WorktableRecipes.getInstance().addRecipe(new ShapedOreRecipe(ReikaItemHelper.getSizedItemStack(this.getCraftedProduct(), num), obj), RecipeLevel.CORE);
			if (ConfigRegistry.TABLEMACHINES.getState()) {
				GameRegistry.addRecipe(new ShapedOreRecipe(ReikaItemHelper.getSizedItemStack(this.getCraftedProduct(), num), obj));
			}
		}
	}

	public static void loadMappings() {
		for (int i = 0; i < ReactorTiles.TEList.length; i++) {
			ReactorTiles r = ReactorTiles.TEList[i];
			Block id = r.getBlock();
			int meta = r.getBlockMetadata();
			reactorMappings.put(id, meta, r);
		}
	}

	public boolean isPipe() {
		return this == GASPIPE || this == MAGNETPIPE;
	}
	/*
	public ReactorType getReactorType() {
		switch (this) {
			case ABSORBER:
			case HEATER:
			case INJECTOR:
			case MAGNET:
			case MAGNETPIPE:
			case SOLENOID:
				return ReactorType.FUSION;
			case BOILER:
			case CONTROL:
			case COOLANT:
			case CPU:
			case FUEL:
				return ReactorType.FISSION;
			case BREEDER:
			case SODIUMBOILER:
				return ReactorType.BREEDER;
			case CO2HEATER:
			case PEBBLEBED:
				return ReactorType.HTGR;
			case THORIUM:
			case FUELDUMP:
				return ReactorType.THORIUM;
			case SOLAR:
			case SOLARTOP:
				return ReactorType.SOLAR;
			default:
				return null;
		}
	}*/

	public boolean isTurbine() {
		return TileEntityTurbineCore.class.isAssignableFrom(teClass);
	}

	public boolean isPowerReceiver() {
		return ReactorPowerReceiver.class.isAssignableFrom(teClass);
	}

	public boolean isMultiblock() {
		return MultiBlockTile.class.isAssignableFrom(teClass);
	}

	public boolean isReactorCore() {
		return ReactorCoreTE.class.isAssignableFrom(teClass);
	}

	public boolean isLinkableReactorCore() {
		return LinkableReactorCore.class.isAssignableFrom(teClass);
	}

	public ItemStack getCraftedProduct(BlockEntity te) {
		return this.getCraftedProduct();
	}

	public boolean allowTickAcceleration() {
		switch(this) {
			case PROCESSOR:
				return true;
			default:
				return false;
		}
	}


}
