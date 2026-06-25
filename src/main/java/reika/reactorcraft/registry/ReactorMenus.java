/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 ******************************************************************************/
package reika.reactorcraft.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.container.MenuCentrifuge;
import reika.reactorcraft.container.MenuNuclearCore;

/**
 * 26.2 menu-type registry, mirroring {@code RotaryMenus}. Each {@link MenuType} is built from an
 * {@link IContainerFactory} so the client side can reconstruct the menu from the {@code FriendlyByteBuf}
 * written by {@code ServerPlayer.openMenu(provider, pos)} (the buf carries the {@code BlockPos}).
 *
 * Screens are bound to these types in {@code ReactorCraft.registerScreens} via {@code RegisterMenuScreensEvent}.
 */
public interface ReactorMenus {

    DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.MENU, ReactorCraft.MODID);

    static <T extends AbstractContainerMenu> Supplier<MenuType<T>> register(String id, IContainerFactory<T> factory) {
        return REGISTRY.register(id, () -> new MenuType<>(factory, FeatureFlags.DEFAULT_FLAGS));
    }

    Supplier<MenuType<MenuNuclearCore>> NUCLEAR_CORE = register("nuclear_core", MenuNuclearCore::new);
    Supplier<MenuType<MenuCentrifuge>> CENTRIFUGE = register("centrifuge", MenuCentrifuge::new);

}
