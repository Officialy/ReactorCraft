package reika.reactorcraft.registry;

import java.util.EnumMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import reika.dragonapi.interfaces.blockentity.HasItemHandler;
import reika.reactorcraft.ReactorCraft;
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
import reika.reactorcraft.tileentities.powergen.TileEntitySteamInjector;
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
import reika.reactorcraft.base.TileEntityInventoriedReactorBase;
import reika.reactorcraft.base.TileEntityTankedReactorMachine;

public final class ReactorBlockEntities {

	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
			DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ReactorCraft.MODID);

	private static final EnumMap<ReactorTiles, DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<?>>> BY_TILE = new EnumMap<>(ReactorTiles.class);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityFuelRod>> FUEL = register("fuel_rod", TileEntityFuelRod.class, ReactorBlocks.FUEL);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityControlRod>> CONTROL = register("control_rod", TileEntityControlRod.class, ReactorBlocks.CONTROL);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityWaterCell>> COOLANT = register("coolant_cell", TileEntityWaterCell.class, ReactorBlocks.COOLANT);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityCPU>> CPU = register("reactor_cpu", TileEntityCPU.class, ReactorBlocks.CPU);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityTurbineCore>> TURBINECORE = register("turbine_core", TileEntityTurbineCore.class, ReactorBlocks.TURBINECORE);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntitySteamInjector>> STEAMINJECTOR = register("steam_injector", TileEntitySteamInjector.class, ReactorBlocks.TURBINEMULTI);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityCondenser>> CONDENSER = register("condenser", TileEntityCondenser.class, ReactorBlocks.CONDENSER);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntitySteamLine>> STEAMLINE = register("steam_line", TileEntitySteamLine.class, ReactorBlocks.STEAMLINE);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityHeavyPump>> FLUIDEXTRACTOR = register("heavy_pump", TileEntityHeavyPump.class, ReactorBlocks.FLUIDEXTRACTOR);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityCentrifuge>> CENTRIFUGE = register("isotope_centrifuge", TileEntityCentrifuge.class, ReactorBlocks.CENTRIFUGE);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityUProcessor>> PROCESSOR = register("uranium_processor", TileEntityUProcessor.class, ReactorBlocks.PROCESSOR);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityWasteContainer>> WASTECONTAINER = register("waste_container", TileEntityWasteContainer.class, ReactorBlocks.WASTECONTAINER);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityReactorBoiler>> BOILER = register("reactor_boiler", TileEntityReactorBoiler.class, ReactorBlocks.BOILER);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntitySteamGrate>> GRATE = register("steam_grate", TileEntitySteamGrate.class, ReactorBlocks.GRATE);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityReactorPump>> PUMP = register("reactor_pump", TileEntityReactorPump.class, ReactorBlocks.PUMP);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntitySynthesizer>> SYNTHESIZER = register("synthesizer", TileEntitySynthesizer.class, ReactorBlocks.SYNTHESIZER);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityToroidMagnet>> MAGNET = register("toroid_magnet", TileEntityToroidMagnet.class, ReactorBlocks.MAGNET);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityElectrolyzer>> ELECTROLYZER = register("electrolyzer", TileEntityElectrolyzer.class, ReactorBlocks.ELECTROLYZER);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityTritizer>> TRITIZER = register("tritizer", TileEntityTritizer.class, ReactorBlocks.TRITIZER);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityBreederCore>> BREEDER = register("breeder_core", TileEntityBreederCore.class, ReactorBlocks.BREEDER);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntitySodiumHeater>> SODIUMBOILER = register("sodium_boiler", TileEntitySodiumHeater.class, ReactorBlocks.SODIUMBOILER);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityHeatExchanger>> EXCHANGER = register("heat_exchanger", TileEntityHeatExchanger.class, ReactorBlocks.EXCHANGER);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityWasteStorage>> STORAGE = register("waste_storage", TileEntityWasteStorage.class, ReactorBlocks.STORAGE);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityFusionInjector>> INJECTOR = register("fusion_injector", TileEntityFusionInjector.class, ReactorBlocks.INJECTOR);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityFusionHeater>> HEATER = register("fusion_heater", TileEntityFusionHeater.class, ReactorBlocks.HEATER);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityGasDuct>> GASPIPE = register("gas_duct", TileEntityGasDuct.class, ReactorBlocks.GASPIPE);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityMagneticPipe>> MAGNETPIPE = register("magnetic_pipe", TileEntityMagneticPipe.class, ReactorBlocks.MAGNETPIPE);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityNeutronAbsorber>> ABSORBER = register("neutron_absorber", TileEntityNeutronAbsorber.class, ReactorBlocks.ABSORBER);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntitySolenoidMagnet>> SOLENOID = register("solenoid_magnet", TileEntitySolenoidMagnet.class, ReactorBlocks.SOLENOID);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityGasCollector>> COLLECTOR = register("gas_collector", TileEntityGasCollector.class, ReactorBlocks.COLLECTOR);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityPebbleBed>> PEBBLEBED = register("pebble_bed", TileEntityPebbleBed.class, ReactorBlocks.PEBBLEBED);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityCO2Heater>> CO2HEATER = register("co2_heater", TileEntityCO2Heater.class, ReactorBlocks.CO2HEATER);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityReactorFlywheel>> FLYWHEEL = register("turbine_flywheel", TileEntityReactorFlywheel.class, ReactorBlocks.FLYWHEEL);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityNeutronReflector>> REFLECTOR = register("neutron_reflector", TileEntityNeutronReflector.class, ReactorBlocks.REFLECTOR);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityReactorGenerator>> GENERATOR = register("reactor_generator", TileEntityReactorGenerator.class, ReactorBlocks.GENERATOR);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityFusionMarker>> MARKER = register("fusion_marker", TileEntityFusionMarker.class, ReactorBlocks.MARKER);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityTurbineMeter>> TURBINEMETER = register("turbine_meter", TileEntityTurbineMeter.class, ReactorBlocks.TURBINEMETER);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityHiPTurbine>> BIGTURBINE = register("high_pressure_turbine", TileEntityHiPTurbine.class, ReactorBlocks.BIGTURBINE);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntitySteamDiffuser>> DIFFUSER = register("steam_diffuser", TileEntitySteamDiffuser.class, ReactorBlocks.DIFFUSER);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityThoriumCore>> THORIUM = register("thorium_core", TileEntityThoriumCore.class, ReactorBlocks.THORIUM);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityWastePipe>> WASTEPIPE = register("waste_pipe", TileEntityWastePipe.class, ReactorBlocks.WASTEPIPE);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityFuelDump>> FUELDUMP = register("fuel_dump", TileEntityFuelDump.class, ReactorBlocks.FUELDUMP);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntitySolarExchanger>> SOLAR = register("solar_exchanger", TileEntitySolarExchanger.class, ReactorBlocks.SOLAR);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntitySolarTop>> SOLARTOP = register("solar_top", TileEntitySolarTop.class, ReactorBlocks.SOLARTOP);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityCentrifugalTurbine>> MINITURBINE = register("mini_turbine", TileEntityCentrifugalTurbine.class, ReactorBlocks.MINITURBINE);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityHeatPipe>> HEATPIPE = register("heat_pipe", TileEntityHeatPipe.class, ReactorBlocks.HEATPIPE);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityWasteDecayer>> WASTEDECAYER = register("waste_decayer", TileEntityWasteDecayer.class, ReactorBlocks.WASTEDECAYER);

	private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(
			String name, Class<T> cls, DeferredHolder<Block, ? extends Block> block) {
		// cls::new is not expressible off a Class<T>, so construct reflectively via the (BlockPos, BlockState) ctor.
		BlockEntityType.BlockEntitySupplier<T> factory = (pos, state) -> {
			try {
				return cls.getConstructor(BlockPos.class, BlockState.class).newInstance(pos, state);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Failed to instantiate " + cls + " for " + name, e);
			}
		};
		return BLOCK_ENTITIES.register(name, () -> new BlockEntityType<>(factory, block.get()));
	}

	public static BlockEntityType<?> getType(ReactorTiles tile) {
		return BY_TILE.get(tile).get();
	}

	static {
		BY_TILE.put(ReactorTiles.FUEL, FUEL);
		BY_TILE.put(ReactorTiles.CONTROL, CONTROL);
		BY_TILE.put(ReactorTiles.COOLANT, COOLANT);
		BY_TILE.put(ReactorTiles.CPU, CPU);
		BY_TILE.put(ReactorTiles.TURBINECORE, TURBINECORE);
		BY_TILE.put(ReactorTiles.CONDENSER, CONDENSER);
		BY_TILE.put(ReactorTiles.STEAMLINE, STEAMLINE);
		BY_TILE.put(ReactorTiles.FLUIDEXTRACTOR, FLUIDEXTRACTOR);
		BY_TILE.put(ReactorTiles.CENTRIFUGE, CENTRIFUGE);
		BY_TILE.put(ReactorTiles.PROCESSOR, PROCESSOR);
		BY_TILE.put(ReactorTiles.WASTECONTAINER, WASTECONTAINER);
		BY_TILE.put(ReactorTiles.BOILER, BOILER);
		BY_TILE.put(ReactorTiles.GRATE, GRATE);
		BY_TILE.put(ReactorTiles.PUMP, PUMP);
		BY_TILE.put(ReactorTiles.SYNTHESIZER, SYNTHESIZER);
		BY_TILE.put(ReactorTiles.MAGNET, MAGNET);
		BY_TILE.put(ReactorTiles.ELECTROLYZER, ELECTROLYZER);
		BY_TILE.put(ReactorTiles.TRITIZER, TRITIZER);
		BY_TILE.put(ReactorTiles.BREEDER, BREEDER);
		BY_TILE.put(ReactorTiles.SODIUMBOILER, SODIUMBOILER);
		BY_TILE.put(ReactorTiles.EXCHANGER, EXCHANGER);
		BY_TILE.put(ReactorTiles.STORAGE, STORAGE);
		BY_TILE.put(ReactorTiles.INJECTOR, INJECTOR);
		BY_TILE.put(ReactorTiles.HEATER, HEATER);
		BY_TILE.put(ReactorTiles.GASPIPE, GASPIPE);
		BY_TILE.put(ReactorTiles.MAGNETPIPE, MAGNETPIPE);
		BY_TILE.put(ReactorTiles.ABSORBER, ABSORBER);
		BY_TILE.put(ReactorTiles.SOLENOID, SOLENOID);
		BY_TILE.put(ReactorTiles.COLLECTOR, COLLECTOR);
		BY_TILE.put(ReactorTiles.PEBBLEBED, PEBBLEBED);
		BY_TILE.put(ReactorTiles.CO2HEATER, CO2HEATER);
		BY_TILE.put(ReactorTiles.FLYWHEEL, FLYWHEEL);
		BY_TILE.put(ReactorTiles.REFLECTOR, REFLECTOR);
		BY_TILE.put(ReactorTiles.GENERATOR, GENERATOR);
		BY_TILE.put(ReactorTiles.MARKER, MARKER);
		BY_TILE.put(ReactorTiles.TURBINEMETER, TURBINEMETER);
		BY_TILE.put(ReactorTiles.BIGTURBINE, BIGTURBINE);
		BY_TILE.put(ReactorTiles.DIFFUSER, DIFFUSER);
		BY_TILE.put(ReactorTiles.THORIUM, THORIUM);
		BY_TILE.put(ReactorTiles.WASTEPIPE, WASTEPIPE);
		BY_TILE.put(ReactorTiles.FUELDUMP, FUELDUMP);
		BY_TILE.put(ReactorTiles.SOLAR, SOLAR);
		BY_TILE.put(ReactorTiles.SOLARTOP, SOLARTOP);
		BY_TILE.put(ReactorTiles.MINITURBINE, MINITURBINE);
		BY_TILE.put(ReactorTiles.HEATPIPE, HEATPIPE);
		BY_TILE.put(ReactorTiles.WASTEDECAYER, WASTEDECAYER);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		for (DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<?>> holder : BLOCK_ENTITIES.getEntries()) {
			registerItemCap(event, holder.get());
		}
	}

	private static <T extends BlockEntity> void registerItemCap(RegisterCapabilitiesEvent event, BlockEntityType<T> type) {
		event.registerBlockEntity(Capabilities.Item.BLOCK, type,
				(be, ctx) -> be instanceof HasItemHandler h ? h.getAutomationItemHandler() : null);
		event.registerBlockEntity(Capabilities.Fluid.BLOCK, type,
				(be, side) -> be instanceof reika.dragonapi.interfaces.blockentity.HasFluidResourceHandler h
						? h.getFluidHandler(side) : null);
	}

	private ReactorBlockEntities() {}
}
