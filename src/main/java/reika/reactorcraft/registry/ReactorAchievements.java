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

import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import reika.reactorcraft.auxiliary.CanisterContents;
import reika.dragonapi.libraries.AdvancementHelper;
import reika.rotarycraft.registry.ConfigRegistry;

/** All 28 V33a achievements, retaining their parents and gameplay award sites. */
public enum ReactorAchievements {
    RECUSEBOOK(() -> new ItemStackTemplate(ReactorItems.REACTOR_BOOK.get()), null, false),
    MINEURANIUM(() -> new ItemStackTemplate(ReactorItems.URANIUM_INGOT.get()), null, false),
    MINECADMIUM(() -> new ItemStackTemplate(ReactorItems.CADMIUM_INGOT.get()), null, false),
    PEBBLE(() -> new ItemStackTemplate(ReactorItems.FUEL_PELLET.get()), MINEURANIUM, false),
    UF6(() -> fluidIcon(ReactorFluids.UF6.get()), MINEURANIUM, false),
    DEPLETED(() -> new ItemStackTemplate(ReactorItems.DEPLETED_FUEL.get()), UF6, false),
    FISSION(() -> new ItemStackTemplate(ReactorItems.FUEL_ROD.get()), UF6, false),
    PLUTONIUM(() -> new ItemStackTemplate(ReactorItems.PLUTONIUM_ROD.get()), FISSION, true),
    PUPOISON(() -> new ItemStackTemplate(Items.SPIDER_EYE), PLUTONIUM, false),
    HOLDWASTE(() -> new ItemStackTemplate(ReactorItems.WASTE_ITEM.get()), FISSION, false),
    DECAY(() -> new ItemStackTemplate(ReactorBlocks.STORAGE.get().asItem()), FISSION, true),
    WASTELEAK(() -> new ItemStackTemplate(ReactorItems.GOGGLES_ITEM.get()), HOLDWASTE, false),
    AMMONIA(() -> fluidIcon(ReactorFluids.AMMONIA.get()), FISSION, false),
    NH3EXPLODE(() -> new ItemStackTemplate(ReactorBlocks.STEAMLINE.get().asItem()), AMMONIA, false),
    GIGATURBINE(() -> new ItemStackTemplate(ReactorBlocks.TURBINECORE.get().asItem()), AMMONIA, true),
    HOTCORE(() -> new ItemStackTemplate(Items.LAVA_BUCKET), FISSION, false),
    SCRAM(() -> new ItemStackTemplate(ReactorBlocks.CPU.get().asItem()), HOTCORE, false),
    MELTDOWN(() -> new ItemStackTemplate(ReactorBlocks.matBlock(MatBlocks.SLAG).asItem()), HOTCORE, false),
    HEAVYWATER(() -> new ItemStackTemplate(ReactorItems.HEAVY_BUCKET.get()), MINEURANIUM, false),
    CANDU(() -> new ItemStackTemplate(ReactorBlocks.COOLANT.get().asItem()), HEAVYWATER, false),
    PLASMA(() -> new ItemStackTemplate(ReactorBlocks.HEATER.get().asItem()), HEAVYWATER, true),
    ESCAPE(() -> new ItemStackTemplate(Items.FLINT_AND_STEEL), PLASMA, false),
    MELTPIPE(() -> new ItemStackTemplate(ReactorBlocks.MAGNETPIPE.get().asItem()), PLASMA, false),
    FUSION(() -> new ItemStackTemplate(ReactorBlocks.MAGNET.get().asItem()), PLASMA, true),
    FIFTYGW(() -> new ItemStackTemplate(reika.rotarycraft.registry.MachineRegistry.DYNAMOMETER.getBlockState().getBlock().asItem()), FUSION, true),
    PEBBLEFAIL(() -> new ItemStackTemplate(ReactorItems.DEPLETED_PELLET.get()), PEBBLE, true),
    THORIUMDUMP(() -> new ItemStackTemplate(ReactorBlocks.THORIUM.get().asItem()), FISSION, true),
    PLASMADIE(() -> new ItemStackTemplate(Items.WITHER_SKELETON_SKULL), ESCAPE, false);

    public static final ReactorAchievements[] list = values();
    public final ReactorAchievements dependency;
    public final boolean isSpecial;
    private final Supplier<ItemStackTemplate> icon;

    ReactorAchievements(Supplier<ItemStackTemplate> icon, ReactorAchievements dependency, boolean special) {
        this.icon = icon;
        this.dependency = dependency;
        this.isSpecial = special;
    }

    private static ItemStackTemplate fluidIcon(Fluid fluid) {
        return new ItemStackTemplate(ReactorItems.CANISTER.get(), DataComponentPatch.builder()
                .set(ReactorDataComponents.CANISTER_FLUID.get(), CanisterContents.of(fluid, 1000))
                .build());
    }

    public ItemStackTemplate getIcon() {
        return icon.get();
    }

    public Identifier getId() {
        return Identifier.fromNamespaceAndPath("reactorcraft", name().toLowerCase(Locale.ROOT));
    }

    public void triggerAchievement(Player player) {
        if (ConfigRegistry.ACHIEVEMENTS.getState())
            AdvancementHelper.grant(player, getId());
    }

    public void triggerAchievement(String playerName) {
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null && playerName != null)
            triggerAchievement(server.getPlayerList().getPlayerByName(playerName));
    }

    public boolean hasDependency() {
        return dependency != null;
    }
}
