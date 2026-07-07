/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.guis;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import reika.dragonapi.instantiable.gui.ImagedGuiButton;
import reika.dragonapi.libraries.rendering.ReikaGuiAPI;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.ReactorDescriptions;
import reika.reactorcraft.registry.ReactorBook;
import reika.rotarycraft.RotaryCraft;

/**
 * The ReactorCraft Handbook. Page layout, paging and tab structure mirror RotaryCraft's
 * GuiHandbook (whose page backgrounds are reused); pages come from {@link ReactorBook} with text
 * from {@link ReactorDescriptions}. Machine pages show the machine's 3D item icon.
 *
 * TODO: the legacy full 3D machine + structure renders on the page (RotaryCraft's
 * GuiMachineRenderState equivalent for ReactorCraft machines, and the STRUCTURES page's rotatable
 * multiblock preview) are not ported.
 */
public class GuiReactorBook extends Screen {

	public static final int PAGES_PER_SCREEN = 8;

	protected final int xSize = 256;
	protected final int ySize = 220;

	private static final int descX = 8;
	private static final int descY = 88;

	private static final Identifier TAB_TEXTURE = Identifier.fromNamespaceAndPath(RotaryCraft.MODID, "textures/screen/handbook/tabs_toc.png");
	private static final Identifier FISSION_TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/gui/handbook/fission.png");
	private static final Identifier FUSION_TEXTURE = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/gui/handbook/fusion.png");

	public Level level;
	private final Player player;

	protected int screen;
	protected int page;
	protected int subpage = 0;

	private long lastButtonClick;

	public GuiReactorBook(Player ep, Level world, int s, int p) {
		super(Component.translatable("item.reactorcraft.reactor_book"));
		player = ep;
		level = world;
		screen = s;
		page = p;
	}

	@Override
	protected void init() {
		super.init();
		this.clearWidgets();

		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2 - 8;

		addRenderableWidget(new ImagedGuiButton(11, j - 20, 17 + k + 143, 20, 20, 220, 20, "+", 0, false, TAB_TEXTURE, b -> this.nextScreen()));
		addRenderableWidget(new ImagedGuiButton(10, j - 20, 17 + k + 163, 20, 20, 220, 0, "-", 0, false, TAB_TEXTURE, b -> this.prevScreen()));
		addRenderableWidget(new ImagedGuiButton(15, j - 20, 17 + k + 183, 20, 20, 220, 20, "<<", 0, false, TAB_TEXTURE, b -> this.goToTOC()));
		addRenderableWidget(Button.builder(Component.literal("X"), b -> this.onClose()).bounds(j + xSize - 27, k + 6, 20, 20).build());

		ReactorBook h = this.getEntry();
		if (h.hasSubpages()) {
			addRenderableWidget(Button.builder(Component.literal(">"), b -> this.nextSubpage()).bounds(j + xSize - 27, k + 40, 20, 20).build());
			addRenderableWidget(Button.builder(Component.literal("<"), b -> this.prevSubpage()).bounds(j + xSize - 27, k + 60, 20, 20).build());
		}
		this.addTabButtons(j, k);
	}

	private void addTabButtons(int j, int k) {
		for (ReactorBook h : ReactorBook.getEntriesForScreen(screen)) {
			addRenderableWidget(new ImagedGuiButton(h.getPage(), j - 20, k + h.getRelativeTabPosn() * 20, 20, 20,
					0, 0, TAB_TEXTURE, b -> this.onTabClicked(h)));
		}
	}

	private void onTabClicked(ReactorBook h) {
		if (this.isClickThrottled())
			return;
		if (this.isOnTOC()) {
			screen = this.getNewScreenByTOCButton(h.getPage() + screen * PAGES_PER_SCREEN);
			page = 0;
		}
		else {
			page = h.getPage();
		}
		subpage = 0;
		this.rebuildWidgets();
	}

	private boolean isClickThrottled() {
		long time = System.currentTimeMillis();
		if (time - lastButtonClick < 250)
			return true;
		lastButtonClick = time;
		return false;
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	public int getMaxScreen() {
		return ReactorBook.RESOURCEDESC.getScreen() + ReactorBook.RESOURCEDESC.getNumberChildren() / PAGES_PER_SCREEN;
	}

	public int getMaxPage() {
		return ReactorBook.getEntriesForScreen(screen).size() - 1;
	}

	public int getMaxSubpage() {
		return this.getEntry().hasSubpages() ? 1 : 0;
	}

	private void goToTOC() {
		if (this.isClickThrottled())
			return;
		screen = 0;
		page = 0;
		subpage = 0;
		this.rebuildWidgets();
	}

	private void nextSubpage() {
		if (this.isClickThrottled())
			return;
		if (subpage < this.getMaxSubpage())
			subpage++;
		this.rebuildWidgets();
	}

	private void prevSubpage() {
		if (this.isClickThrottled())
			return;
		if (subpage > 0)
			subpage--;
		this.rebuildWidgets();
	}

	private void nextScreen() {
		if (this.isClickThrottled())
			return;
		if (screen < this.getMaxScreen()) {
			screen++;
			page = 0;
			subpage = 0;
		}
		this.rebuildWidgets();
	}

	private void prevScreen() {
		if (this.isClickThrottled())
			return;
		if (screen > 0) {
			screen--;
			page = 0;
			subpage = 0;
		}
		this.rebuildWidgets();
	}

	private void nextPage() {
		if (page < this.getMaxPage()) {
			page++;
			subpage = 0;
			this.rebuildWidgets();
		}
		else {
			lastButtonClick = 0;
			this.nextScreen();
		}
	}

	private void prevPage() {
		if (page > 0) {
			page--;
			subpage = 0;
			this.rebuildWidgets();
		}
		else {
			lastButtonClick = 0;
			this.prevScreen();
			page = Math.max(0, this.getMaxPage());
		}
	}

	protected boolean isOnTOC() {
		return this.getEntry() == ReactorBook.TOC;
	}

	private int getNewScreenByTOCButton(int id) {
		List<ReactorBook> li = ReactorBook.getCategoryTabs();
		if (id >= li.size())
			return 0;
		return li.get(id).getScreen();
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (super.keyPressed(event))
			return true;
		switch (event.key()) {
			case InputConstants.KEY_LEFT -> {
				this.prevPage();
				return true;
			}
			case InputConstants.KEY_RIGHT -> {
				this.nextPage();
				return true;
			}
			case InputConstants.KEY_PAGEUP -> {
				lastButtonClick = 0;
				this.prevScreen();
				return true;
			}
			case InputConstants.KEY_PAGEDOWN -> {
				lastButtonClick = 0;
				this.nextScreen();
				return true;
			}
		}
		return false;
	}

	private Identifier getBackgroundTexture() {
		ReactorBook h = this.getEntry();
		if (h == ReactorBook.FISSIONINFO)
			return FISSION_TEXTURE;
		if (h == ReactorBook.FUSIONINFO)
			return FUSION_TEXTURE;
		String type = this.isOnTOC() ? "a" : (h.hasMachineRender() ? "m" : "b");
		return Identifier.fromNamespaceAndPath(RotaryCraft.MODID, "textures/screen/handbook/handbookgui" + type + ".png");
	}

	protected ReactorBook getEntry() {
		return ReactorBook.getEntry(screen, page);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float f) {
		int posX = (width - xSize) / 2;
		int posY = (height - ySize) / 2 - 8;

		graphics.blit(RenderPipelines.GUI_TEXTURED, this.getBackgroundTexture(), posX, posY, 0, 0, xSize, ySize, 256, 256);

		ReactorBook h = this.getEntry();
		graphics.text(font, h.getTitle(), posX + 6, posY + 6, 0xFF000000, false);

		int px = posX + descX;
		int textY = posY + descY;
		if (this.isOnTOC())
			textY -= 44;
		String text = subpage == 0 || h.sameTextAllSubpages() ? h.getData() : h.getNotes(subpage);
		if (text != null && !text.isEmpty())
			graphics.textWithWordWrap(font, FormattedText.of(text), px, textY, 242, 0xFFFFFFFF, false);

		super.extractRenderState(graphics, mouseX, mouseY, f);

		this.drawTabIcons(graphics);
		this.drawPageInfo(graphics, posX, posY, mouseX, mouseY);

		if (subpage == 0 && h.hasMachineRender()) {
			ItemStack icon = h.getTabIcon();
			if (!icon.isEmpty())
				ReikaGuiAPI.instance.drawItemStack(graphics, font, icon, posX + 159, posY + 36);
		}
	}

	private void drawTabIcons(GuiGraphicsExtractor graphics) {
		int posX = (width - xSize) / 2;
		int posY = (height - ySize) / 2;
		List<ReactorBook> li = new ArrayList<>(ReactorBook.getEntriesForScreen(screen));
		for (int i = 0; i < li.size(); i++) {
			ItemStack icon = li.get(i).getTabIcon();
			if (icon != null && !icon.isEmpty())
				ReikaGuiAPI.instance.drawItemStack(graphics, font, icon, posX - 17, posY - 6 + i * 20);
		}
	}

	private void drawPageInfo(GuiGraphicsExtractor graphics, int posX, int posY, int mouseX, int mouseY) {
		String s = String.format("Page %d/%d", screen, this.getMaxScreen());
		ReikaGuiAPI.instance.drawTooltipAt(graphics, font, s, posX + 22 + xSize + font.width(s), posY + 12);
		if (ReikaGuiAPI.instance.isMouseInBox(posX - 20, posX, posY - 8, posY + 212, mouseX, mouseY)) {
			List<ReactorBook> li = ReactorBook.getEntriesForScreen(screen);
			int idx = (mouseY - (posY - 8)) / 20;
			String sg = "";
			if (idx >= PAGES_PER_SCREEN) {
				switch (idx - PAGES_PER_SCREEN) {
					case 0 -> sg = "Next";
					case 1 -> sg = "Back";
					case 2 -> sg = "Return";
				}
			}
			else if (idx >= 0 && idx < li.size()) {
				sg = li.get(idx).getTitle();
			}
			if (sg != null && !sg.isEmpty())
				ReikaGuiAPI.instance.drawTooltipAt(graphics, font, sg, mouseX + font.width(sg) + 30, mouseY);
		}
	}
}
