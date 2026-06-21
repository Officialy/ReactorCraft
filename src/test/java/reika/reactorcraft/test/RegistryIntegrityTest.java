package reika.reactorcraft.test;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;

import org.junit.jupiter.api.Test;

import reika.reactorcraft.registry.FluoriteTypes;
import reika.reactorcraft.registry.ReactorBlocks;
import reika.reactorcraft.registry.ReactorFluids;
import reika.reactorcraft.registry.ReactorItems;
import reika.reactorcraft.registry.ReactorOreType;
import reika.reactorcraft.registry.ReactorRecipeSerializers;
import reika.reactorcraft.registry.ReactorRecipeTypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Headless registry/logic checks. These run with the mod loaded by the NeoForge moddev JUnit
 * harness, so DeferredHolder.get() resolves real registered objects. They guard the regressions
 * this port keeps hitting: the full fluid set being registered with bound FluidTypes, the custom
 * processor/centrifuge recipe type + serializer wiring, and the ore-type data the worldgen/datagen
 * pipeline reads.
 */
public class RegistryIntegrityTest {

    /** All 24 ReactorCraft-owned fluids must register as distinct sources with bound, distinct types. */
    @Test
    void allFluidsRegisteredWithTypes() {
        assertEquals(24, ReactorFluids.tintedFluids().size(), "expected 24 ReactorCraft-owned fluids");
        Set<Fluid> seenFluids = new HashSet<>();
        Set<Object> seenTypes = new HashSet<>();
        for (ReactorFluids.TintedFluid tf : ReactorFluids.tintedFluids()) {
            FlowingFluid fluid = tf.fluid().get();
            assertNotNull(fluid, "fluid holder resolved null");
            assertTrue(seenFluids.add(fluid), "duplicate fluid instance: " + fluid);
            assertNotNull(fluid.getFluidType(), fluid + " has no bound FluidType");
            assertTrue(seenTypes.add(fluid.getFluidType()), "duplicate FluidType across fluids: " + fluid);
        }
    }

    /** The chemistry recipe types and serializers must be registered and mutually distinct. */
    @Test
    void recipeTypesAndSerializersRegistered() {
        RecipeType<?> processorType = ReactorRecipeTypes.PROCESSOR.get();
        RecipeType<?> centrifugeType = ReactorRecipeTypes.CENTRIFUGE.get();
        RecipeSerializer<?> processorSer = ReactorRecipeSerializers.PROCESSOR.get();
        RecipeSerializer<?> centrifugeSer = ReactorRecipeSerializers.CENTRIFUGE.get();
        assertNotNull(processorType);
        assertNotNull(centrifugeType);
        assertNotNull(processorSer);
        assertNotNull(centrifugeSer);
        assertNotEquals(processorType, centrifugeType, "processor and centrifuge recipe types must be distinct");
        assertNotEquals(processorSer, centrifugeSer, "processor and centrifuge serializers must be distinct");
    }

    /** Every ore type resolves to a registered block + product (the worldgen/loot/smelt inputs). */
    @Test
    void oreTypesResolveBlocksAndProducts() {
        for (ReactorOreType ore : ReactorOreType.list) {
            assertNotNull(ore.getBlock(), ore + " has null block");
            assertNotNull(ore.getProduct(), ore + " has null product");
            assertTrue(ore.maxY >= ore.minY, ore + " has an inverted Y band");
        }
    }

    /** All eight fluorite colours must have a distinct ore block and gem item. */
    @Test
    void fluoriteColoursAreDistinct() {
        Set<Object> blocks = new HashSet<>();
        Set<Object> gems = new HashSet<>();
        for (FluoriteTypes f : FluoriteTypes.colorList) {
            assertTrue(blocks.add(ReactorBlocks.fluoriteOre(f)), "duplicate fluorite ore block: " + f);
            assertTrue(gems.add(ReactorItems.fluorite(f)), "duplicate fluorite gem: " + f);
        }
        assertEquals(8, blocks.size(), "expected 8 fluorite colours");
    }
}
