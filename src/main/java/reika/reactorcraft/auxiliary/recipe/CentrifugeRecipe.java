/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.auxiliary.recipe;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

import reika.reactorcraft.registry.ReactorRecipeSerializers;
import reika.reactorcraft.registry.ReactorRecipeTypes;

/**
 * The gas-centrifuge chemistry, transcribed from {@code TileEntityCentrifuge.Centrifuging.UF6}:
 * {@code input} ({@code UF6_PER_DUST}=50 mB UF6) is enriched, yielding {@code outputA}
 * (enriched fuel dust) with a {@code chanceOfAOverB}% chance ({@code FUEL_CHANCE}=9) and otherwise
 * {@code outputB} (depleted dust). Requires the centrifuge to spin at {@code minSpeed}
 * ({@code MINSPEED}=262144 rad/s); {@code speedFactor} scales the per-tick progress.
 * <p>
 * The consuming {@code TileEntityCentrifuge} looks these up by input fluid at runtime.
 */
public class CentrifugeRecipe implements Recipe<RecipeInput> {

    public record FluidInput(FluidStack fluid) implements RecipeInput {
        @Override public ItemStack getItem(int slot) { throw new IndexOutOfBoundsException(slot); }
        @Override public int size() { return 0; }
    }

    private final FluidStackTemplate input;
    private final ItemStackTemplate outputA;
    private final Optional<ItemStackTemplate> outputB;
    private final float chanceOfAOverB;
    private final int minSpeed;
    private final int speedFactor;

    public CentrifugeRecipe(FluidStackTemplate input, ItemStackTemplate outputA, Optional<ItemStackTemplate> outputB,
                            float chanceOfAOverB, int minSpeed, int speedFactor) {
        this.input = input;
        this.outputA = outputA;
        this.outputB = outputB;
        this.chanceOfAOverB = chanceOfAOverB;
        this.minSpeed = minSpeed;
        this.speedFactor = speedFactor;
    }

    public FluidStack getInput() { return input.create(); }
    public ItemStack getOutputA() { return outputA.create(); }
    public ItemStack getOutputB() { return outputB.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY); }
    public float getChanceOfAOverB() { return chanceOfAOverB; }
    public int getMinSpeed() { return minSpeed; }
    public int getSpeedFactor() { return speedFactor; }

    @Override
    public boolean matches(RecipeInput in, Level level) {
        return in instanceof FluidInput(FluidStack fluid) && !fluid.isEmpty()
                && fluid.getFluid().isSame(input.create().getFluid());
    }

    @Override
    public ItemStack assemble(RecipeInput in) {
        return outputA.create();
    }

    @Override public boolean isSpecial() { return true; }

    @Override public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return ReactorRecipeSerializers.CENTRIFUGE.get();
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return ReactorRecipeTypes.CENTRIFUGE.get();
    }

    public static final MapCodec<CentrifugeRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            FluidStackTemplate.CODEC.fieldOf("input").forGetter(r -> r.input),
            ItemStackTemplate.CODEC.fieldOf("output_a").forGetter(r -> r.outputA),
            ItemStackTemplate.CODEC.optionalFieldOf("output_b").forGetter(r -> r.outputB),
            Codec.FLOAT.fieldOf("chance_a_over_b").forGetter(r -> r.chanceOfAOverB),
            Codec.INT.fieldOf("min_speed").forGetter(r -> r.minSpeed),
            Codec.INT.fieldOf("speed_factor").forGetter(r -> r.speedFactor)
    ).apply(inst, CentrifugeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CentrifugeRecipe> STREAM_CODEC = StreamCodec.of(
            (buf, r) -> {
                FluidStackTemplate.STREAM_CODEC.encode(buf, r.input);
                ItemStackTemplate.STREAM_CODEC.encode(buf, r.outputA);
                ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC).encode(buf, r.outputB);
                ByteBufCodecs.FLOAT.encode(buf, r.chanceOfAOverB);
                ByteBufCodecs.VAR_INT.encode(buf, r.minSpeed);
                ByteBufCodecs.VAR_INT.encode(buf, r.speedFactor);
            },
            buf -> new CentrifugeRecipe(
                    FluidStackTemplate.STREAM_CODEC.decode(buf),
                    ItemStackTemplate.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC).decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf))
    );
}
