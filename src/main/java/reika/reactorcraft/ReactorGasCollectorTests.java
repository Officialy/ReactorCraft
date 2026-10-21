package reika.reactorcraft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import reika.reactorcraft.blocks.BlockReactorMachine;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.tileentities.TileEntityGasCollector;

/** CO2 Collector: placed on a burning furnace it faces up, reads the furnace below and fills with CO2. */
final class ReactorGasCollectorTests {

    private ReactorGasCollectorTests() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        ReactorGameTests.register(event, environment, "gas_collector_on_furnace_collects_co2", 80, ReactorGasCollectorTests::collectsFurnaceCO2);
    }

    private static void collectsFurnaceCO2(GameTestHelper h) {
        BlockPos furnace = new BlockPos(3, 2, 4);
        h.setBlock(furnace, Blocks.FURNACE);
        FurnaceBlockEntity f = h.getBlockEntity(furnace, FurnaceBlockEntity.class);
        f.setItem(0, new ItemStack(Items.RAW_IRON, 16));
        f.setItem(1, new ItemStack(Items.COAL, 16));

        var player = h.makeMockServerPlayer(GameType.SURVIVAL);
        ItemStack item = new ItemStack(ReactorBlocks.COLLECTOR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        h.placeAt(player, item, furnace, Direction.UP);
        BlockPos pos = furnace.above();
        Direction facing = h.getBlockState(pos).getValue(BlockReactorMachine.FACING);
        h.assertTrue(facing == Direction.UP, "collector clicked onto a furnace top must face UP (1.7.10 side metadata), faced " + facing);

        h.runAfterDelay(40, () -> {
            var te = h.getBlockEntity(pos, TileEntityGasCollector.class);
            h.assertTrue(te.getReadDirection() == Direction.DOWN, "collector must read the furnace below, reads " + te.getReadDirection());
            var tank = te.getFluidHandler(Direction.UP);
            h.assertTrue(tank != null && tank.getAmountAsLong(0) > 0
                            && tank.getResource(0).getFluid() == ReactorFluids.getLegacyFluid("rc co2"),
                    "collector over a burning coal furnace holds " + (tank == null ? "no tank" : tank.getAmountAsLong(0) + " mB " + tank.getResource(0)));
            h.succeed();
        });
    }
}
