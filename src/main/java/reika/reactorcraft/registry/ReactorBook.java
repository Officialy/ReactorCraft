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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import reika.reactorcraft.auxiliary.ReactorBookData;
import reika.reactorcraft.auxiliary.ReactorDescriptions;
import reika.reactorcraft.auxiliary.ReactorStacks;
import reika.rotarycraft.auxiliary.interfaces.HandbookEntry;
import reika.rotarycraft.gui.screen.GuiHandbook;
import reika.rotarycraft.registry.RotaryBlocks;

/**
 * The ReactorCraft Handbook page registry. Ordinal order IS the page order: each {@code isParent}
 * entry starts a category; screen/page indices derive from position (8 pages per screen, matching
 * {@link GuiHandbook#PAGES_PER_SCREEN}). Legacy entries whose items are not ported (remote control,
 * geiger counter, cleanup gun) are omitted.
 */
public enum ReactorBook implements HandbookEntry {

	//---------------------TOC--------------------//
	TOC("Table Of Contents", true),
	INFO("Info", () -> ReactorItems.BOOK.getStackOf()),
	PROCESSING("Processing", ReactorTiles.PROCESSOR),
	POWERGEN("Power Gen", ReactorTiles.TURBINECORE),
	HTGRS("HTGR", ReactorTiles.PEBBLEBED),
	FISSION("Fission", ReactorTiles.FUEL),
	BREEDER("Breeder", ReactorTiles.BREEDER),
	THORIUMSEC("Thorium", ReactorTiles.THORIUM),
	FUSION("Fusion", ReactorTiles.MAGNET),
	ACC("Accessory", ReactorTiles.MAGNETPIPE),
	TOOLS("Tools", () -> ReactorItems.GOGGLES.getStackOf()),
	RESOURCE("Resources", () -> ReactorItems.FUEL.getStackOf()),

	//---------------------INFO--------------------//
	INTRO("Introduction", true),
	PHYSICS("Nuclear Physics", () -> new ItemStack(Items.BOOK)),
	FISSIONINFO("Nuclear Fission", () -> ReactorItems.FUEL.getStackOf()),
	FUSIONINFO("Nuclear Fusion", () -> ReactorStacks.h2can.copy()),
	BASICS("Nuclear Power Basics", () -> new ItemStack(Items.WATER_BUCKET)),
	ENRICHMENT("Uranium Enrichment", ReactorTiles.CENTRIFUGE),
	MELTDOWN("Meltdowns", () -> MatBlocks.SLAG.getStackOf()),
	RADIATION("Radiation", () -> ReactorItems.WASTE.getStackOf()),
	SHIELDING("Shielding", () -> new ItemStack(RotaryBlocks.HSLA_STEEL_BLOCK.get())),
	STRUCTURES("Structures", () -> new ItemStack(ReactorBlocks.FERROMAGNETIC_BASE.get())),

	//--------------------PROCESSING---------------//
	PROCDESC("Processing Machines", true),
	PROCESSOR(ReactorTiles.PROCESSOR),
	CENTRIFUGE(ReactorTiles.CENTRIFUGE),
	ELECTROLYZER(ReactorTiles.ELECTROLYZER),
	SYNTHESIZER(ReactorTiles.SYNTHESIZER),
	TRITIZER(ReactorTiles.TRITIZER),

	GENDESC("Power Generation Machines", true),
	BOILER(ReactorTiles.BOILER),
	STEAMLINE(ReactorTiles.STEAMLINE),
	STEAMGRATE(ReactorTiles.GRATE),
	TURBINE(ReactorTiles.TURBINECORE),
	CONDENSER(ReactorTiles.CONDENSER),
	HEATEXCHANGER(ReactorTiles.EXCHANGER),
	PUMP(ReactorTiles.PUMP),
	GENERATOR(ReactorTiles.GENERATOR),
	BIGTURBINE(ReactorTiles.BIGTURBINE),

	HTGRDESC("HTGR Components", true),
	PEBBLEBED(ReactorTiles.PEBBLEBED),
	CO2HEATER(ReactorTiles.CO2HEATER),

	FISSIONDESC("Fission Reactor Components", true),
	FUELROD(ReactorTiles.FUEL),
	CONTROLROD(ReactorTiles.CONTROL),
	WATERCELL(ReactorTiles.COOLANT),
	CPU(ReactorTiles.CPU),
	WASTEDECAYER(ReactorTiles.WASTEDECAYER),

	BREEDERDESC("Breeder Reactor Components", true),
	BREEDERCORE(ReactorTiles.BREEDER),
	SODIUMHEATER(ReactorTiles.SODIUMBOILER),

	THORIUMDESC("Thorium Reactor Components", true),
	THORIUM(ReactorTiles.THORIUM),
	FUELDUMP(ReactorTiles.FUELDUMP),

	FUSIONDESC("Fusion Reactor Components", true),
	FUSIONHEATER(ReactorTiles.HEATER),
	FUSIONINJECTOR(ReactorTiles.INJECTOR),
	TOROID(ReactorTiles.MAGNET),
	SOLENOID(ReactorTiles.SOLENOID),
	ABSORBER(ReactorTiles.ABSORBER),

	ACCDESC("Utility Machines", true),
	GASCOLLECTOR(ReactorTiles.COLLECTOR),
	GASDUCT(ReactorTiles.GASPIPE),
	MAGNETPIPE(ReactorTiles.MAGNETPIPE),
	HEAVYPUMP(ReactorTiles.FLUIDEXTRACTOR),
	WASTECONTAINER(ReactorTiles.WASTECONTAINER),
	WASTESTORAGE(ReactorTiles.STORAGE),
	REFLECTOR(ReactorTiles.REFLECTOR),
	DYNAMOMETER(ReactorTiles.TURBINEMETER),
	BLUEPRINT(ReactorTiles.MARKER),
	FLYWHEEL(ReactorTiles.FLYWHEEL),
	DIFFUSER(ReactorTiles.DIFFUSER),
	SOLARTOP(ReactorTiles.SOLARTOP),
	SOLAREXCH(ReactorTiles.SOLAR),

	TOOLDESC("Tools", true),
	GOGGLES(ReactorItems.GOGGLES, "Radiation Goggles"),

	RESOURCEDESC("Resource Items", true),
	FLUORITE(ReactorItems.FLUORITE, "Fluorite"),
	FUEL(ReactorItems.FUEL, "Uranium Fuel"),
	DEPLETED(ReactorItems.DEPLETED, "Depleted Uranium"),
	WASTE(ReactorItems.WASTE, "Nuclear Waste"),
	PLUTONIUM(ReactorItems.PLUTONIUM, "Plutonium"),
	BREEDERFUEL(ReactorItems.BREEDERFUEL, "Breeder Fuel"),
	MAGNET(ReactorItems.MAGNET, "Permanent Magnet"),
	PELLET(ReactorItems.PELLET, "TRISO Pellet"),
	OLDPELLET(ReactorItems.OLDPELLET, "Depleted TRISO Fuel");

	private final java.util.function.Supplier<ItemStack> iconItem;
	private final String pageTitle;
	private boolean isParent = false;
	private ReactorTiles machine;
	private ReactorItems.ItemRef item;

	public static final ReactorBook[] tabList = values();

	private ReactorBook(String name, boolean parent) {
		this(name, (java.util.function.Supplier<ItemStack>) null);
		isParent = parent;
	}

	private ReactorBook(ReactorTiles r) {
		this((String) null, () -> new ItemStack(r.getBlock()));
		machine = r;
	}

	private ReactorBook(String name, ReactorTiles r) {
		this(name, () -> new ItemStack(r.getBlock()));
	}

	private ReactorBook(ReactorItems.ItemRef i, String name) {
		this(name, i::getStackOf);
		item = i;
	}

	private ReactorBook(String name, java.util.function.Supplier<ItemStack> icon) {
		iconItem = icon;
		pageTitle = name;
	}

	public static ReactorBook getFromScreenAndPage(int screen, int page) {
		if (screen < INTRO.getScreen())
			return TOC;
		ReactorBook h = ReactorBookData.getMapping(screen, page);
		return h != null ? h : TOC;
	}

	public static ReactorBook getEntry(int screen, int page) {
		return getFromScreenAndPage(screen, page);
	}

	public static List<ReactorBook> getEntriesForScreen(int screen) {
		List<ReactorBook> li = new ArrayList<>();
		for (ReactorBook h : tabList) {
			if (h.getScreen() == screen)
				li.add(h);
		}
		return li;
	}

	public static List<ReactorBook> getTOCTabs() {
		return getCategoryTabs();
	}

	public static List<ReactorBook> getMachineTabs() {
		List<ReactorBook> tabs = new ArrayList<>();
		for (ReactorBook h : tabList) {
			if (h.isMachine() && !h.isParent)
				tabs.add(h);
		}
		return tabs;
	}

	public static ReactorBook[] getToolTabs() {
		int size = RESOURCEDESC.ordinal() - TOOLDESC.ordinal() - 1;
		ReactorBook[] tabs = new ReactorBook[size];
		System.arraycopy(tabList, TOOLDESC.ordinal() + 1, tabs, 0, size);
		return tabs;
	}

	public static ReactorBook[] getResourceTabs() {
		int size = tabList.length - RESOURCEDESC.ordinal() - 1;
		ReactorBook[] tabs = new ReactorBook[size];
		System.arraycopy(tabList, RESOURCEDESC.ordinal() + 1, tabs, 0, size);
		return tabs;
	}

	public static ReactorBook[] getInfoTabs() {
		int size = PROCDESC.ordinal() - INTRO.ordinal() - 1;
		ReactorBook[] tabs = new ReactorBook[size];
		System.arraycopy(tabList, INTRO.ordinal() + 1, tabs, 0, size);
		return tabs;
	}

	public static List<ReactorBook> getCategoryTabs() {
		List<ReactorBook> li = new ArrayList<>();
		for (ReactorBook h : tabList) {
			if (h.isParent && h != TOC)
				li.add(h);
		}
		return li;
	}

	public boolean isMachine() {
		return machine != null;
	}

	public ReactorTiles getMachine() {
		return machine;
	}

	public ReactorItems.ItemRef getItem() {
		return item;
	}

	@Override
	public ItemStack getTabIcon() {
		return iconItem != null ? iconItem.get() : ItemStack.EMPTY;
	}

	@Override
	public String getData() {
		if (this == TOC)
			return ReactorDescriptions.getTOC();
		return ReactorDescriptions.getData(this);
	}

	@Override
	public String getNotes(int subpage) {
		return ReactorDescriptions.getNotes(this);
	}

	@Override
	public boolean sameTextAllSubpages() {
		return false;
	}

	@Override
	public String getTitle() {
		if (pageTitle != null)
			return pageTitle;
		if (machine != null)
			return machine.getName();
		return this.name();
	}

	@Override
	public boolean hasMachineRender() {
		return this.isMachine();
	}

	@Override
	public boolean hasSubpages() {
		return this.isMachine() || this == STRUCTURES || this == SHIELDING;
	}

	public int getRelativeScreen() {
		int offset = this.ordinal() - this.getParent().ordinal();
		return offset / GuiHandbook.PAGES_PER_SCREEN;
	}

	public ReactorBook getParent() {
		ReactorBook parent = null;
		for (ReactorBook h : tabList) {
			if (h.isParent && this.ordinal() >= h.ordinal())
				parent = h;
		}
		return parent;
	}

	public boolean isParent() {
		return isParent;
	}

	public int getBaseScreen() {
		int sc = 0;
		for (int i = 0; i < this.ordinal(); i++) {
			ReactorBook h = tabList[i];
			if (h.isParent)
				sc += h.getNumberChildren() / GuiHandbook.PAGES_PER_SCREEN + 1;
		}
		return sc;
	}

	public int getNumberChildren() {
		if (!isParent)
			return 0;
		int ch = 0;
		for (int i = this.ordinal() + 1; i < tabList.length; i++) {
			if (tabList[i].isParent)
				return ch;
			ch++;
		}
		return ch;
	}

	public int getRelativePage() {
		return this.ordinal() - this.getParent().ordinal();
	}

	public int getRelativeTabPosn() {
		int offset = this.ordinal() - this.getParent().ordinal();
		return offset - this.getRelativeScreen() * GuiHandbook.PAGES_PER_SCREEN;
	}

	@Override
	public int getScreen() {
		return this.getParent().getBaseScreen() + this.getRelativeScreen();
	}

	@Override
	public int getPage() {
		return (this.ordinal() - this.getParent().ordinal()) % GuiHandbook.PAGES_PER_SCREEN;
	}

	@Override
	public boolean isConfigDisabled() {
		return false;
	}

	public static int getScreen(ReactorTiles m) {
		for (int i = PROCDESC.ordinal(); i < TOOLDESC.ordinal(); i++) {
			if (tabList[i].machine == m)
				return tabList[i].getScreen();
		}
		return -1;
	}

	public static int getPage(ReactorTiles m) {
		for (ReactorBook h : tabList) {
			if (h.machine == m)
				return h.getPage();
		}
		return -1;
	}
}
