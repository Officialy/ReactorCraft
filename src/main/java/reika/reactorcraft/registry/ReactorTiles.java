package reika.reactorcraft.registry;

import java.util.Locale;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;

import reika.dragonapi.exception.RegistrationException;
import reika.dragonapi.instantiable.data.maps.BlockMap;
import reika.dragonapi.interfaces.registry.TileEnum;
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

public enum ReactorTiles implements TileEnum {

	FUEL("machine.fuel", ReactorBlocks.FUEL, TileEntityFuelRod.class),
	CONTROL("machine.control", ReactorBlocks.CONTROL, TileEntityControlRod.class),
	COOLANT("machine.coolant", ReactorBlocks.COOLANT, TileEntityWaterCell.class),
	CPU("machine.cpu", ReactorBlocks.CPU, TileEntityCPU.class),
	TURBINECORE("machine.turbine", ReactorBlocks.TURBINECORE, TileEntityTurbineCore.class),
	CONDENSER("machine.condenser", ReactorBlocks.CONDENSER, TileEntityCondenser.class),
	STEAMLINE("machine.steamline", ReactorBlocks.STEAMLINE, TileEntitySteamLine.class),
	FLUIDEXTRACTOR("machine.heavypump", ReactorBlocks.FLUIDEXTRACTOR, TileEntityHeavyPump.class),
	CENTRIFUGE("machine.isocentrifuge", ReactorBlocks.CENTRIFUGE, TileEntityCentrifuge.class),
	PROCESSOR("machine.processor", ReactorBlocks.PROCESSOR, TileEntityUProcessor.class),
	WASTECONTAINER("machine.wastecontainer", ReactorBlocks.WASTECONTAINER, TileEntityWasteContainer.class),
	BOILER("machine.reactorboiler", ReactorBlocks.BOILER, TileEntityReactorBoiler.class),
	GRATE("machine.grate", ReactorBlocks.GRATE, TileEntitySteamGrate.class),
	PUMP("machine.reactorpump", ReactorBlocks.PUMP, TileEntityReactorPump.class),
	SYNTHESIZER("machine.synthesizer", ReactorBlocks.SYNTHESIZER, TileEntitySynthesizer.class),
	MAGNET("machine.magnet", ReactorBlocks.MAGNET, TileEntityToroidMagnet.class),
	ELECTROLYZER("machine.electrolyzer", ReactorBlocks.ELECTROLYZER, TileEntityElectrolyzer.class),
	TRITIZER("machine.tritizer", ReactorBlocks.TRITIZER, TileEntityTritizer.class),
	BREEDER("machine.breedercore", ReactorBlocks.BREEDER, TileEntityBreederCore.class),
	SODIUMBOILER("machine.sodiumboiler", ReactorBlocks.SODIUMBOILER, TileEntitySodiumHeater.class),
	EXCHANGER("machine.exchanger", ReactorBlocks.EXCHANGER, TileEntityHeatExchanger.class),
	STORAGE("machine.storage", ReactorBlocks.STORAGE, TileEntityWasteStorage.class),
	INJECTOR("machine.injector", ReactorBlocks.INJECTOR, TileEntityFusionInjector.class),
	HEATER("machine.fusionheater", ReactorBlocks.HEATER, TileEntityFusionHeater.class),
	GASPIPE("machine.gasduct", ReactorBlocks.GASPIPE, TileEntityGasDuct.class),
	MAGNETPIPE("machine.magnetpipe", ReactorBlocks.MAGNETPIPE, TileEntityMagneticPipe.class),
	ABSORBER("machine.absorber", ReactorBlocks.ABSORBER, TileEntityNeutronAbsorber.class),
	SOLENOID("machine.solenoid", ReactorBlocks.SOLENOID, TileEntitySolenoidMagnet.class),
	COLLECTOR("machine.collector", ReactorBlocks.COLLECTOR, TileEntityGasCollector.class),
	PEBBLEBED("machine.pebblebed", ReactorBlocks.PEBBLEBED, TileEntityPebbleBed.class),
	CO2HEATER("machine.co2heater", ReactorBlocks.CO2HEATER, TileEntityCO2Heater.class),
	FLYWHEEL("machine.turbinewheel", ReactorBlocks.FLYWHEEL, TileEntityReactorFlywheel.class),
	REFLECTOR("machine.reflector", ReactorBlocks.REFLECTOR, TileEntityNeutronReflector.class),
	GENERATOR("machine.reactorgenerator", ReactorBlocks.GENERATOR, TileEntityReactorGenerator.class),
	MARKER("machine.fusionmarker", ReactorBlocks.MARKER, TileEntityFusionMarker.class),
	TURBINEMETER("machine.turbinemeter", ReactorBlocks.TURBINEMETER, TileEntityTurbineMeter.class),
	BIGTURBINE("machine.bigturbine", ReactorBlocks.BIGTURBINE, TileEntityHiPTurbine.class),
	DIFFUSER("machine.steamdiffuser", ReactorBlocks.DIFFUSER, TileEntitySteamDiffuser.class),
	THORIUM("machine.thorium", ReactorBlocks.THORIUM, TileEntityThoriumCore.class),
	WASTEPIPE("machine.wastepipe", ReactorBlocks.WASTEPIPE, TileEntityWastePipe.class),
	FUELDUMP("machine.fueldump", ReactorBlocks.FUELDUMP, TileEntityFuelDump.class),
	SOLAR("machine.solarexchange", ReactorBlocks.SOLAR, TileEntitySolarExchanger.class),
	SOLARTOP("machine.solartop", ReactorBlocks.SOLARTOP, TileEntitySolarTop.class),
	MINITURBINE("machine.miniturbine", ReactorBlocks.MINITURBINE, TileEntityCentrifugalTurbine.class),
	HEATPIPE("machine.heatpipe", ReactorBlocks.HEATPIPE, TileEntityHeatPipe.class),
	WASTEDECAYER("machine.wastedecayer", ReactorBlocks.WASTEDECAYER, TileEntityWasteDecayer.class);

	private final String nameKey;
	private final DeferredBlock<Block> block;
	private final Class<? extends BlockEntity> teClass;

	public static final ReactorTiles[] TEList = values();
	private static final BlockMap<ReactorTiles> reactorMappings = new BlockMap<>();

	ReactorTiles(String nameKey, DeferredBlock<Block> block, Class<? extends BlockEntity> teClass) {
		this.nameKey = nameKey;
		this.block = block;
		this.teClass = teClass;
	}

	@Override
	public String getName() {
		return I18n.get(nameKey);
	}

	@Override
	public Class<? extends BlockEntity> getTEClass() {
		return teClass;
	}

	@Override
	public BlockState getBlockState() {
		return block.get().defaultBlockState();
	}

	public Block getBlock() {
		return block.get();
	}

	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		try {
			return teClass.getConstructor(BlockPos.class, BlockState.class).newInstance(pos, state);
		} catch (ReflectiveOperationException e) {
			throw new RegistrationException(ReactorCraft.getInstance(),
					"Failed to instantiate " + teClass + " for " + this, e);
		}
	}

	public static ReactorTiles getMachine(Level level, BlockPos pos) {
		Block b = level.getBlockState(pos).getBlock();
		if (b == Blocks.AIR)
			return null;
		return getMachineMapping(b);
	}

	public static ReactorTiles getMachineMapping(Block block) {
		return reactorMappings.get(block);
	}

	public static ReactorTiles getTE(BlockGetter level, BlockPos pos) {
		return getMachine((Level) level, pos);
	}

	public static void loadMappings() {
		for (ReactorTiles r : TEList) {
			Block id = r.getBlock();
			if (reactorMappings.containsKey(id))
				throw new RegistrationException(ReactorCraft.getInstance(), "Block conflict: " + id);
			reactorMappings.put(id, r);
		}
	}

	public boolean isReactorCore() {
		return ReactorCoreTE.class.isAssignableFrom(teClass);
	}

	public boolean isLinkableReactorCore() {
		return LinkableReactorCore.class.isAssignableFrom(teClass);
	}

	public boolean isPowerReceiver() {
		return ReactorPowerReceiver.class.isAssignableFrom(teClass);
	}

	public boolean isMultiblock() {
		return MultiBlockTile.class.isAssignableFrom(teClass);
	}

	public boolean allowTickAcceleration() {
		return this == PROCESSOR;
	}

	public boolean isPipe() {
		return this == GASPIPE || this == MAGNETPIPE || this == WASTEPIPE;
	}

	public static BlockEntityType<?> blockEntityType(ReactorTiles tile) {
		return ReactorBlockEntities.getType(tile);
	}

}
