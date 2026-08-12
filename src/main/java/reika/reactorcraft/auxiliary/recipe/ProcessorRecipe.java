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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import reika.reactorcraft.registry.ReactorRecipeSerializers;
import reika.reactorcraft.registry.ReactorRecipeTypes;

/**
 * The uranium-processor chemistry, transcribed from {@code TileEntityUProcessor.Processes.UF6}:
 * <ol>
 *   <li><b>Intermediate</b> (every {@code intermediateTime} ticks): {@code inputFluid} +
 *       1 {@code catalyst} (fluorite) → {@code intermediateFluid} ({@code intermediateFluid.amount}).</li>
 *   <li><b>Output</b> (every {@code outputTime} ticks): {@code inputItem} (uranium ingot) +
 *       {@code intermediateConsumed} of the intermediate fluid → {@code outputFluid}.</li>
 * </ol>
 * Data-only: the consuming {@code TileEntityUProcessor} (deferred with the TE cluster) reads these
 * by input fluid; the recipe is registered as a custom {@link RecipeType} purely so the values live
 * in JSON (datagen) rather than a hardcoded enum.
 */
public class ProcessorRecipe implements Recipe<RecipeInput> {

    private final Ingredient catalyst;
    private final Ingredient inputItem;
    private final FluidStack inputFluid;
    private final FluidStack intermediateFluid;
    private final int intermediateConsumed;
    private final FluidStack outputFluid;
    private final int intermediateTime;
    private final int outputTime;

    public ProcessorRecipe(Ingredient catalyst, Ingredient inputItem, FluidStack inputFluid, FluidStack intermediateFluid,
                           int intermediateConsumed, FluidStack outputFluid, int intermediateTime, int outputTime) {
        this.catalyst = catalyst;
        this.inputItem = inputItem;
        this.inputFluid = inputFluid;
        this.intermediateFluid = intermediateFluid;
        this.intermediateConsumed = intermediateConsumed;
        this.outputFluid = outputFluid;
        this.intermediateTime = intermediateTime;
        this.outputTime = outputTime;
    }

    public Ingredient getCatalyst() { return catalyst; }
    public Ingredient getInputItem() { return inputItem; }
    public FluidStack getInputFluid() { return inputFluid; }
    public FluidStack getIntermediateFluid() { return intermediateFluid; }
    public int getIntermediateConsumed() { return intermediateConsumed; }
    public FluidStack getOutputFluid() { return outputFluid; }
    public int getIntermediateTime() { return intermediateTime; }
    public int getOutputTime() { return outputTime; }

    @Override
    public boolean matches(RecipeInput in, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput in) {
        return ItemStack.EMPTY;
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
        return ReactorRecipeSerializers.PROCESSOR.get();
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return ReactorRecipeTypes.PROCESSOR.get();
    }

    public static final MapCodec<ProcessorRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("catalyst").forGetter(r -> r.catalyst),
            Ingredient.CODEC.fieldOf("input_item").forGetter(r -> r.inputItem),
            FluidStack.CODEC.fieldOf("input_fluid").forGetter(r -> r.inputFluid),
            FluidStack.CODEC.fieldOf("intermediate_fluid").forGetter(r -> r.intermediateFluid),
            Codec.INT.fieldOf("intermediate_consumed").forGetter(r -> r.intermediateConsumed),
            FluidStack.CODEC.fieldOf("output_fluid").forGetter(r -> r.outputFluid),
            Codec.INT.fieldOf("intermediate_time").forGetter(r -> r.intermediateTime),
            Codec.INT.fieldOf("output_time").forGetter(r -> r.outputTime)
    ).apply(inst, ProcessorRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessorRecipe> STREAM_CODEC = StreamCodec.of(
            (buf, r) -> {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, r.catalyst);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, r.inputItem);
                FluidStack.STREAM_CODEC.encode(buf, r.inputFluid);
                FluidStack.STREAM_CODEC.encode(buf, r.intermediateFluid);
                ByteBufCodecs.VAR_INT.encode(buf, r.intermediateConsumed);
                FluidStack.STREAM_CODEC.encode(buf, r.outputFluid);
                ByteBufCodecs.VAR_INT.encode(buf, r.intermediateTime);
                ByteBufCodecs.VAR_INT.encode(buf, r.outputTime);
            },
            buf -> new ProcessorRecipe(
                    Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                    Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                    FluidStack.STREAM_CODEC.decode(buf),
                    FluidStack.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    FluidStack.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf))
    );
}
