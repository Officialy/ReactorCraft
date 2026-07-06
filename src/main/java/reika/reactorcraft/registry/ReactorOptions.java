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

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import net.neoforged.neoforge.common.ModConfigSpec;

/** The ReactorCraft config options, backed by a NeoForge {@link ModConfigSpec} (registered in the
 *  mod constructor); enum defaults apply until the config file loads. */
public enum ReactorOptions {

	VISIBLENEUTRONS(true),
	SILVERORE(true),
	MAGNETORE(true),
	CALCITEORE(true),
	CADMIUMORE(true),
	INDIUMORE(true),
	RAINBOW(false),
	TOROIDCHARGE(4),
	CHUNKLOADING(true),
	OREDENSITY(100),
	DISCRETE(1),
	DYECRAFT(false),
	FASTNEUTRONS(false),
	VERTNEUTRONS(false),
	LODESTONERFMULT(1F),
	RADIOORE(false),
	STEAMLINECAP(Integer.MAX_VALUE);

	private final boolean boolDefault;
	private final int intDefault;
	private final float floatDefault;
	private final boolean isBool;
	private final boolean isInt;

	ReactorOptions(boolean d) {
		boolDefault = d;
		intDefault = 0;
		floatDefault = 0;
		isBool = true;
		isInt = false;
	}

	ReactorOptions(int d) {
		boolDefault = false;
		intDefault = d;
		floatDefault = 0;
		isBool = false;
		isInt = true;
	}

	ReactorOptions(float d) {
		boolDefault = false;
		intDefault = 0;
		floatDefault = d;
		isBool = false;
		isInt = false;
	}

	public static final ModConfigSpec SPEC;
	private static final EnumMap<ReactorOptions, ModConfigSpec.ConfigValue<?>> VALUES = new EnumMap<>(ReactorOptions.class);
	private static final ModConfigSpec.ConfigValue<String> HEAVY_WATER_DIMS;

	static {
		ModConfigSpec.Builder b = new ModConfigSpec.Builder();
		b.push("options");
		for (ReactorOptions o : values()) {
			String key = o.name().toLowerCase(Locale.ROOT);
			if (o.isBool)
				VALUES.put(o, b.define(key, o.boolDefault));
			else if (o.isInt)
				VALUES.put(o, b.define(key, o.intDefault));
			else
				VALUES.put(o, b.define(key, (double) o.floatDefault));
		}
		HEAVY_WATER_DIMS = b.comment("Comma-separated legacy dimension ids where the heavy pump works; empty = all dimensions")
				.define("heavywaterdimensions", "");
		b.pop();
		SPEC = b.build();
	}

	public boolean getState() {
		return SPEC.isLoaded() ? (Boolean) VALUES.get(this).get() : boolDefault;
	}

	public int getValue() {
		return SPEC.isLoaded() ? ((Number) VALUES.get(this).get()).intValue() : intDefault;
	}

	public float getFloat() {
		return SPEC.isLoaded() ? ((Number) VALUES.get(this).get()).floatValue() : floatDefault;
	}

	/** Legacy int dimension ids the heavy pump may extract in; empty = no restriction. */
	public static Set<Integer> getHeavyWaterDimensions() {
		if (!SPEC.isLoaded())
			return Set.of();
		Set<Integer> out = new HashSet<>();
		for (String s : HEAVY_WATER_DIMS.get().split(",")) {
			s = s.trim();
			if (!s.isEmpty()) {
				try {
					out.add(Integer.parseInt(s));
				} catch (NumberFormatException ignored) {}
			}
		}
		return out;
	}

	public static int getToroidChargeRate() {
		return TOROIDCHARGE.getValue();
	}

	public static float getOreMultiplier() {
		return OREDENSITY.getValue() / 100F;
	}

	public static final ReactorOptions[] optionList = values();
}
