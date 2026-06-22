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

import java.net.URL;
import java.util.HashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import reika.dragonapi.interfaces.registry.CustomDistanceSound;
import reika.dragonapi.libraries.io.ReikaPacketHelper;
import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.reactorcraft.ReactorCraft;
import reika.rotarycraft.registry.ConfigRegistry;

import static net.minecraft.sounds.SoundSource.MASTER;

public enum ReactorSounds implements CustomDistanceSound {

	TURBINE("#turbine-vol"),
	FUSION("fusion"),
	CONTROL("control"),
	GENERATOR_RF("#gen_rf"),
	GENERATOR_EU("#gen_eu"),
	GENERATOR_ELC("#gen_elc"),
	SCRAM("scram");

	public static final String SOUND_FOLDER = "sounds/";
	private static final String SOUND_DIR = "sounds/";
	private static final String SOUND_EXT = ".ogg";

	// DeferredRegister for the SoundEvents backing each enum value.
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
			DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, ReactorCraft.MODID);

	private static final HashMap<ReactorSounds, DeferredHolder<SoundEvent, SoundEvent>> SOUND_EVENT_MAP = new HashMap<>();

	static {
		// Sounds with a custom audible distance (the fusion reactor at 32 blocks) need a fixed-range
		// event; createVariableRangeEvent caps range at 16 for volume <= 1, which swallows long-range loops.
		for (ReactorSounds sound : values()) {
			SOUND_EVENT_MAP.put(sound, SOUND_EVENTS.register(sound.eventName, () -> {
				Identifier id = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, sound.eventName);
				float d = sound.getAudibleDistance();
				return d > 0 ? SoundEvent.createFixedRangeEvent(id, d) : SoundEvent.createVariableRangeEvent(id);
			}));
		}
	}

	private final Identifier path;
	private final String name;
	private final String eventName;

	private boolean isVolumed = false;

	private ReactorSounds(String n) {
		if (n.startsWith("#")) {
			isVolumed = true;
			n = n.substring(1);
		}
		name = n;
		eventName = n;
		path = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, SOUND_FOLDER + name + SOUND_EXT);
	}

	public SoundEvent getSoundEvent() {
		return SOUND_EVENT_MAP.get(this).get();
	}

	public float getSoundVolume() {
		float vol = ConfigRegistry.MACHINEVOLUME.getFloat();
		if (vol < 0)
			vol = 0;
		if (vol > 1)
			vol = 1F;
		return vol;
	}

	@Override
	public float getModulatedVolume() {
		if (!isVolumed)
			return 1F;
		else
			return this.getSoundVolume();
	}

	public void playSound(Entity e) {
		this.playSound(e, 1, 1);
	}

	public void playSound(Entity e, float vol, float pitch) {
		this.playSound(e.level(), e.getX(), e.getY(), e.getZ(), vol, pitch);
	}

	public void playSound(Level world, BlockPos pos, float vol, float pitch) {
		this.playSound(world, pos.getX(), pos.getY(), pos.getZ(), vol, pitch);
	}

	public void playSound(Level world, double x, double y, double z, float vol, float pitch) {
		// Server sends the packet; the client plays on receipt to avoid double-playback.
		if (world.isClientSide())
			return;
		ReikaSoundHelper.playSound(this, world, x, y, z, vol, pitch);
	}

	public void playSound(Level world, BlockPos pos, float vol, float pitch, boolean attenuate) {
		if (world.isClientSide())
			return;
		ReikaSoundHelper.playSound(this, world, pos.getX(), pos.getY(), pos.getZ(), vol, pitch, attenuate);
	}

	public void playSound(Level world, double x, double y, double z, float vol, float pitch, boolean attenuate) {
		if (world.isClientSide())
			return;
		ReikaSoundHelper.playSound(this, world, x, y, z, vol, pitch, attenuate);
	}

	public void playSoundAtBlock(Level world, BlockPos pos, float vol, float pitch) {
		this.playSound(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, vol, pitch);
	}

	public void playSoundAtBlock(Level world, BlockPos pos) {
		this.playSoundAtBlock(world, pos, 1, 1);
	}

	public void playSoundAtBlock(Level world, double x, double y, double z, float vol, float pitch) {
		this.playSound(world, x + 0.5, y + 0.5, z + 0.5, vol, pitch);
	}

	public void playSoundAtBlock(BlockEntity te) {
		this.playSoundAtBlock(te, 1, 1);
	}

	public void playSoundAtBlock(BlockEntity te, float vol, float pitch) {
		this.playSoundAtBlock(te.getLevel(), te.getBlockPos(), vol, pitch);
	}

	public void playSoundNoAttenuation(Level world, BlockPos pos, float vol, float pitch, int broadcast) {
		ReikaPacketHelper.sendSoundPacket(this, world, pos.getX(), pos.getY(), pos.getZ(), vol, pitch, false, broadcast);
	}

	public String getName() {
		return this.name();
	}

	public Identifier getPath() {
		return path;
	}

	public URL getURL() {
		return ReactorCraft.class.getResource(SOUND_DIR + name + SOUND_EXT);
	}

	@Override
	public SoundSource getCategory() {
		return MASTER;
	}

	@Override
	public boolean canOverlap() {
		return this == FUSION;
	}

	@Override
	public boolean attenuate() {
		return true;
	}

	@Override
	public boolean preload() {
		return false;
	}

	@Override
	public float getAudibleDistance() {
		switch (this) {
			case FUSION:
				return 32;
			default:
				return -1;
		}
	}
}
