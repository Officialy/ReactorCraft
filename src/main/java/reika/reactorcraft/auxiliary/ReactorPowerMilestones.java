package reika.reactorcraft.auxiliary;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import reika.reactorcraft.blocks.BlockSteam;
import reika.reactorcraft.registry.ReactorAchievements;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.tileentities.powergen.TileEntityHiPTurbine;
import reika.reactorcraft.tileentities.powergen.TileEntitySteamGrate;
import reika.reactorcraft.tileentities.powergen.TileEntitySteamLine;
import reika.reactorcraft.tileentities.powergen.TileEntityTurbineCore;

/** Measures simultaneous turbine output from one connected reactor steam circuit. */
public final class ReactorPowerMilestones {
    public static final long FIFTY_GW = 50_000_000_000L;
    private static final Map<ServerLevel, Cache> CACHE = new WeakHashMap<>();
    private ReactorPowerMilestones() {}

    private static final class Cache {
        long tick = Long.MIN_VALUE;
        final Map<BlockPos, Circuit> circuits = new HashMap<>();
    }
    private record Circuit(long power, Set<BlockPos> outlets) {}

    public static void check(TileEntityTurbineCore turbine) {
        if (!reika.rotarycraft.registry.ConfigRegistry.ACHIEVEMENTS.getState()
                || !(turbine.getLevel() instanceof ServerLevel level) || !turbine.isPowerTopologyReady()
                || turbine.getCurrentPower() <= 0) return;
        Circuit circuit = circuit(level, turbine.getBlockPos());
        if (circuit.power < FIFTY_GW) return;
        for (BlockPos outlet : circuit.outlets)
            if (level.getBlockEntity(outlet) instanceof TileEntityTurbineCore generator)
                ReactorAchievements.FIFTYGW.triggerAchievement(generator.getPlacer());
    }

    public static long circuitPower(ServerLevel level, BlockPos start) { return circuit(level, start).power; }

    private static Circuit circuit(ServerLevel level, BlockPos start) {
        Cache cache = CACHE.computeIfAbsent(level, ignored -> new Cache());
        if (cache.tick != level.getGameTime()) {
            cache.tick = level.getGameTime();
            cache.circuits.clear();
        }
        var previous = cache.circuits.get(start);
        if (previous != null) return previous;
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> outlets = new HashSet<>();
        var pending = new ArrayDeque<BlockPos>();
        pending.add(start);
        long power = 0;
        while (!pending.isEmpty()) {
            BlockPos pos = pending.removeFirst();
            if (!level.hasChunkAt(pos) || !visited.add(pos)) continue;
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof TileEntityTurbineCore turbine && turbine.isPowerTopologyReady() && outlet(level, turbine)) {
                long generated = Math.max(0, turbine.getCurrentPower());
                power = generated > Long.MAX_VALUE - power ? Long.MAX_VALUE : power + generated;
                if (generated > 0) outlets.add(pos);
            }
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (level.hasChunkAt(next) && connected(level, pos, tile, next, level.getBlockEntity(next)))
                    pending.addLast(next);
            }
        }
        var result = new Circuit(power, Set.copyOf(outlets));
        for (BlockPos pos : visited) cache.circuits.put(pos, result);
        return result;
    }

    private static boolean outlet(ServerLevel level, TileEntityTurbineCore turbine) {
        BlockPos next = turbine.getBlockPos().relative(turbine.getFacing());
        return !level.hasChunkAt(next) || !(level.getBlockEntity(next) instanceof TileEntityTurbineCore other)
                || other.getTile() != turbine.getTile() || other.getFacing() != turbine.getFacing();
    }

    private static boolean connected(ServerLevel level, BlockPos a, BlockEntity first, BlockPos b, BlockEntity second) {
        if (first instanceof TileEntityTurbineCore turbine) {
            if (second instanceof TileEntityTurbineCore other)
                return turbine.getTile() == other.getTile() && turbine.getFacing() == other.getFacing()
                        && (a.relative(turbine.getFacing()).equals(b) || a.relative(turbine.getFacing().getOpposite()).equals(b));
            return turbine instanceof TileEntityHiPTurbine
                    ? second instanceof TileEntitySteamLine && a.relative(turbine.getFacing().getOpposite()).equals(b)
                    : steam(level, b) && a.below().equals(b);
        }
        if (second instanceof TileEntityTurbineCore) return connected(level, b, second, a, first);
        if (first instanceof TileEntitySteamLine)
            return second instanceof TileEntitySteamLine || second instanceof TileEntitySteamGrate;
        if (second instanceof TileEntitySteamLine) return first instanceof TileEntitySteamGrate;
        if (first instanceof TileEntitySteamGrate) return a.above().equals(b) && steam(level, b);
        if (second instanceof TileEntitySteamGrate) return b.above().equals(a) && steam(level, a);
        return steam(level, a) && steam(level, b)
                && level.getBlockState(a).getValue(BlockSteam.AMMONIA) == level.getBlockState(b).getValue(BlockSteam.AMMONIA);
    }

    private static boolean steam(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(ReactorBlocks.STEAM.get()) && state.getValue(BlockSteam.POWERED);
    }
}
