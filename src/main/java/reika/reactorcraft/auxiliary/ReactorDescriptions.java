/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.auxiliary;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import reika.dragonapi.instantiable.io.XMLInterface;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.registry.RadiationShield;
import reika.reactorcraft.registry.ReactorBook;
import reika.reactorcraft.registry.ReactorTiles;
import reika.reactorcraft.tileentities.TileEntityHeavyPump;
import reika.reactorcraft.tileentities.processing.TileEntityCentrifuge;
import reika.reactorcraft.tileentities.processing.TileEntityElectrolyzer;
import reika.reactorcraft.tileentities.processing.TileEntitySynthesizer;

/** The handbook text: page descriptions and notes loaded from the assets/reactorcraft/resources
 *  XML files (with per-language subfolders), formatted with the live machine constants. */
public final class ReactorDescriptions {

	public static final String DESC_SUFFIX = ":desc";
	public static final String NOTE_SUFFIX = ":note";

	private static final HashMap<ReactorBook, String> data = new HashMap<>();
	private static final HashMap<ReactorBook, String> notes = new HashMap<>();
	private static final HashMap<ReactorTiles, Object[]> machineData = new HashMap<>();
	private static final HashMap<ReactorTiles, Object[]> machineNotes = new HashMap<>();
	private static final HashMap<ReactorBook, Object[]> miscData = new HashMap<>();
	private static final ArrayList<ReactorBook> categories = new ArrayList<>();

	private static final String RESOURCE_ROOT = "/assets/reactorcraft/resources/";

	private static String PARENT = getParent(true);
	private static final XMLInterface parents = loadData("categories");
	private static final XMLInterface machines = loadData("machines");
	private static final XMLInterface tools = loadData("tools");
	private static final XMLInterface resources = loadData("resource");
	private static final XMLInterface infos = loadData("info");

	private static XMLInterface loadData(String name) {
		XMLInterface xml = new XMLInterface(ReactorCraft.class, PARENT + name + ".xml", false);
		xml.setFallback(getParent(false) + name + ".xml");
		xml.init();
		return xml;
	}

	private static String getParent(boolean locale) {
		return locale && FMLEnvironment.getDist() == Dist.CLIENT ? getLocalizedParent() : RESOURCE_ROOT;
	}

	private static String getLocalizedParent() {
		String language = Minecraft.getInstance().getLanguageManager().getSelected();
		if ("en_us".equals(language))
			return RESOURCE_ROOT;
		if (hasLocalizedFor(language))
			return RESOURCE_ROOT + language + "/";
		String legacy = toLegacyCode(language);
		if (hasLocalizedFor(legacy))
			return RESOURCE_ROOT + legacy + "/";
		return RESOURCE_ROOT;
	}

	private static String toLegacyCode(String language) {
		int idx = language.indexOf('_');
		if (idx < 0)
			return language;
		return language.substring(0, idx) + "_" + language.substring(idx + 1).toUpperCase(Locale.ENGLISH);
	}

	private static boolean hasLocalizedFor(String language) {
		try (InputStream o = ReactorCraft.class.getResourceAsStream(RESOURCE_ROOT + language + "/categories.xml")) {
			return o != null;
		}
		catch (IOException e) {
			return false;
		}
	}

	public static String getTOC() {
		List<ReactorBook> toctabs = ReactorBook.getTOCTabs();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < toctabs.size(); i++) {
			ReactorBook h = toctabs.get(i);
			sb.append("Page ");
			sb.append(h.getScreen());
			sb.append(" - ");
			sb.append(h.getTitle());
			if (i < toctabs.size() - 1)
				sb.append("\n");
		}
		return sb.toString();
	}

	private static void addData(ReactorTiles m, Object... data) {
		machineData.put(m, data);
	}

	private static void addNotes(ReactorTiles m, Object... data) {
		machineNotes.put(m, data);
	}

	private static void addData(ReactorBook h, Object... data) {
		miscData.put(h, data);
	}

	public static void reload() {
		PARENT = getParent(true);
		data.clear();
		loadNumericalData();
		machines.reread();
		tools.reread();
		resources.reread();
		infos.reread();
		parents.reread();
		loadData();
	}

	private static void addEntry(ReactorBook h, String sg) {
		data.put(h, sg);
	}

	public static void loadData() {
		List<ReactorBook> parenttabs = ReactorBook.getCategoryTabs();
		List<ReactorBook> machinetabs = ReactorBook.getMachineTabs();
		ReactorBook[] tooltabs = ReactorBook.getToolTabs();
		ReactorBook[] resourcetabs = ReactorBook.getResourceTabs();
		ReactorBook[] infotabs = ReactorBook.getInfoTabs();

		for (ReactorBook h : parenttabs) {
			String desc = parents.getValueAtNode("categories:" + h.name().toLowerCase(Locale.ENGLISH));
			addEntry(h, desc);
		}
		for (ReactorBook h : machinetabs) {
			ReactorTiles m = h.getMachine();
			String desc = machines.getValueAtNode("machines:" + m.name().toLowerCase(Locale.ENGLISH) + DESC_SUFFIX);
			String aux = machines.getValueAtNode("machines:" + m.name().toLowerCase(Locale.ENGLISH) + NOTE_SUFFIX);
			desc = safeFormat(desc, machineData.get(m));
			aux = safeFormat(aux, machineNotes.get(m));
			if (XMLInterface.NULL_VALUE.equals(desc))
				desc = "There is no handbook data for this machine yet.";
			addEntry(h, desc);
			notes.put(h, aux);
		}
		for (ReactorBook h : tooltabs) {
			addEntry(h, tools.getValueAtNode("tools:" + h.name().toLowerCase(Locale.ENGLISH)));
		}
		for (ReactorBook h : resourcetabs) {
			addEntry(h, resources.getValueAtNode("resource:" + h.name().toLowerCase(Locale.ENGLISH)));
		}
		for (ReactorBook h : infotabs) {
			String desc = infos.getValueAtNode("info:" + h.name().toLowerCase(Locale.ENGLISH));
			desc = safeFormat(desc, miscData.get(h));
			addEntry(h, desc);
		}
		notes.put(ReactorBook.SHIELDING, RadiationShield.getDataAsString());
	}

	// The XML text carries printf-style holes for live machine constants; a missing/renamed constant
	// must degrade to raw text rather than crash the book open.
	private static String safeFormat(String s, Object[] args) {
		if (s == null || args == null)
			return s;
		try {
			return String.format(s, args);
		}
		catch (Exception e) {
			return s;
		}
	}

	public static String getData(ReactorBook h) {
		return data.getOrDefault(h, "");
	}

	public static String getNotes(ReactorBook h) {
		return notes.getOrDefault(h, "");
	}

	static {
		loadNumericalData();
	}

	private static void loadNumericalData() {
		addNotes(ReactorTiles.CENTRIFUGE, TileEntityCentrifuge.MINSPEED);
		addNotes(ReactorTiles.ELECTROLYZER, TileEntityElectrolyzer.SALT_MELT);
		addNotes(ReactorTiles.SYNTHESIZER, TileEntitySynthesizer.AMMONIATEMP);
		addNotes(ReactorTiles.FLUIDEXTRACTOR, TileEntityHeavyPump.MINPOWER, TileEntityHeavyPump.MINTORQUE);
	}
}
