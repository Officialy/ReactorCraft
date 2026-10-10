package reika.reactorcraft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import reika.dragonapi.base.BlockMultiBlock;
import reika.reactorcraft.blocks.multi.BlockTurbineMulti;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.tileentities.powergen.TileEntitySteamInjector;

/** Native factory, same-block variant transition and persistence checks for accepted casing consumers. */
final class ReactorMultiblockContractTests {
    private static final BlockPos POS = new BlockPos(4, 1, 4);

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        ReactorGameTests.register(event, environment, "dragonapi_multiblock_native_factory", 40, ReactorMultiblockContractTests::factory);
        ReactorGameTests.register(event, environment, "dragonapi_multiblock_part_transition", 40, ReactorMultiblockContractTests::transition);
    }

    private static void factory(GameTestHelper h) {
        var turbine = ReactorBlocks.TURBINEMULTI.get();
        h.assertTrue(turbine instanceof EntityBlock, "turbine casing must use the native factory interface");
        for (var part : BlockTurbineMulti.TurbinePart.values()) {
            var state = turbine.defaultBlockState().setValue(BlockTurbineMulti.PART, part);
            h.setBlock(POS, state);
            h.assertTrue((h.getLevel().getBlockEntity(h.absolutePos(POS)) instanceof TileEntitySteamInjector)
                    == (part == BlockTurbineMulti.TurbinePart.ELEMENT), "only the turbine ELEMENT hosts an injector: " + part);
        }
        var casings = new java.util.ArrayList<net.minecraft.world.level.block.Block>(java.util.List.of(
                ReactorBlocks.GENERATORMULTI.get(), ReactorBlocks.FLYWHEELMULTI.get(),
                ReactorBlocks.FERROMAGNETIC_BASE.get(), ReactorBlocks.MAGNETIC_LINKAGE.get(),
                ReactorBlocks.CENTRAL_MAGNET.get(), ReactorBlocks.AUXILIARY_MAGNET.get(),
                ReactorBlocks.HYSTERESIS_ROD.get(), ReactorBlocks.SOLENOID_HUB.get()));
        ReactorBlocks.HEATER_CASINGS.values().forEach(holder -> casings.add(holder.get()));
        ReactorBlocks.INJECTOR_CASINGS.values().forEach(holder -> casings.add(holder.get()));
        for (var casing : casings) {
            h.setBlock(POS, casing);
            h.assertTrue(!(casing instanceof EntityBlock) && h.getLevel().getBlockEntity(h.absolutePos(POS)) == null,
                    "ordinary accepted casing finds its controller and does not invent a local BE: " + casing);
        }
        h.succeed();
    }

    private static void transition(GameTestHelper h) {
        var element = ReactorBlocks.TURBINEMULTI.get().defaultBlockState()
                .setValue(BlockTurbineMulti.PART, BlockTurbineMulti.TurbinePart.ELEMENT);
        h.setBlock(POS, element);
        var injector = h.getBlockEntity(POS, TileEntitySteamInjector.class);
        var resource = FluidResource.of(ReactorFluids.getLegacyFluid("rc lubricant"));
        var automation = h.getLevel().getCapability(Capabilities.Fluid.BLOCK, h.absolutePos(POS), Direction.UP);
        h.assertTrue(automation != null, "injector has its actual native lubricant capability");
        try (Transaction tx = Transaction.openRoot()) {
            h.assertTrue(automation.insert(resource, 500, tx) == 500, "native tank accepts lubricant"); tx.commit();
        }
        h.setBlock(POS, element.setValue(BlockMultiBlock.FORMED, true));
        h.assertTrue(h.getBlockEntity(POS, TileEntitySteamInjector.class) == injector && injector.getLubricant() == 500,
                "forming the same element retains its existing BE and contents");
        var saved = injector.saveWithoutMetadata(h.getLevel().registryAccess());
        var restored = new TileEntitySteamInjector(injector.getBlockPos(), injector.getBlockState());
        restored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), saved));
        h.assertTrue(restored.getLubricant() == 500, "native injector persistence retains lubricant");
        var shell = element.setValue(BlockTurbineMulti.PART, BlockTurbineMulti.TurbinePart.SHELL);
        h.assertTrue(!injector.isValidBlockState(shell), "same block does not make a non-element state valid for this BE");
        h.setBlock(POS, shell);
        h.assertTrue(injector.isRemoved() && h.getLevel().getBlockEntity(h.absolutePos(POS)) == null
                && h.getLevel().getCapability(Capabilities.Fluid.BLOCK, h.absolutePos(POS), Direction.UP) == null,
                "changing element into shell clears the obsolete injector and capability");
        h.setBlock(POS, element);
        var replacement = h.getBlockEntity(POS, TileEntitySteamInjector.class);
        h.assertTrue(replacement != injector && replacement.getLubricant() == 0, "restoring element creates fresh storage rather than retaining a ghost BE");
        h.setBlock(POS, Blocks.AIR);
        h.assertTrue(replacement.isRemoved() && h.getLevel().getBlockEntity(h.absolutePos(POS)) == null,
                "native block removal clears the remaining factory-created BE");
        h.succeed();
    }
}
