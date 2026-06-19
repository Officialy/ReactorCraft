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

import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.entities.EntityFusion;
import reika.reactorcraft.entities.EntityNeutron;
import reika.reactorcraft.entities.EntityNuclearWaste;
import reika.reactorcraft.entities.EntityPlasma;
import reika.reactorcraft.entities.EntityRadiation;

public class ReactorEntities {

	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, ReactorCraft.MODID);

	public static final DeferredHolder<EntityType<?>, EntityType<EntityNeutron>> NEUTRON = registerEntityType("neutron", () -> EntityType.Builder.of(EntityNeutron::new, MobCategory.MISC).sized(0.2F, 0.2F).clientTrackingRange(4));

	public static final DeferredHolder<EntityType<?>, EntityType<EntityRadiation>> RADIATION = registerEntityType("radiation", () -> EntityType.Builder.of(EntityRadiation::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4));

	public static final DeferredHolder<EntityType<?>, EntityType<EntityPlasma>> PLASMA = registerEntityType("plasma", () -> EntityType.Builder.of(EntityPlasma::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(8));

	public static final DeferredHolder<EntityType<?>, EntityType<EntityFusion>> FUSION = registerEntityType("fusion", () -> EntityType.Builder.of(EntityFusion::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(8));

	public static final DeferredHolder<EntityType<?>, EntityType<EntityNuclearWaste>> NUCLEARWASTE = registerEntityType("nuclear_waste", () -> EntityType.Builder.of(EntityNuclearWaste::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(6));

	private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> registerEntityType(final String name, final Supplier<EntityType.Builder<T>> factory) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(ReactorCraft.MODID, name));
		return ENTITIES.register(name, () -> factory.get().build(key));
	}

}
