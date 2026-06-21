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

/** Config defaults until {@code ReactorConfig} is wired to NeoForge {@code ModConfigSpec}. */
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

	public boolean getState() {
		return boolDefault;
	}

	public int getValue() {
		return intDefault;
	}

	public float getFloat() {
		return floatDefault;
	}

	public static int getToroidChargeRate() {
		return TOROIDCHARGE.getValue();
	}

	public static float getOreMultiplier() {
		return OREDENSITY.getValue() / 100F;
	}

	public static final ReactorOptions[] optionList = values();
}
