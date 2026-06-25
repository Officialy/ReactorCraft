/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 ******************************************************************************/
package reika.reactorcraft.base;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;

import reika.dragonapi.base.CoreContainer;
import reika.dragonapi.libraries.rendering.ReikaGuiAPI;
import reika.reactorcraft.ReactorCraft;

/**
 * 26.2 base screen for ReactorCraft machine GUIs, mirroring RotaryCraft's non-powered
 * {@code MachineScreen} but without the power tab (ReactorCraft tiles are not power receivers).
 *
 * Subclasses supply {@link #getGuiTexture()} (the {@code textures/gui/<name>.png} stem) and may
 * override {@link #extractBackground} to draw progress bars / fluid tanks on top of the background,
 * remembering to call {@code super.extractBackground(...)} first.
 *
 * The 1.21.5+ render pipeline replaces {@code drawGuiContainerBackgroundLayer}/{@code ...Foreground}
 * with {@link #extractBackground}/{@link #extractLabels} using a {@link GuiGraphicsExtractor}.
 * {@code imageWidth}/{@code imageHeight} are final and must be passed through the super constructor.
 */
public abstract class ReactorGuiBase<E extends TileEntityReactorBase, T extends CoreContainer<E>> extends AbstractContainerScreen<T> {

    protected static final ReikaGuiAPI api = ReikaGuiAPI.instance;
    protected final E tile;
    protected Inventory inventory;

    public ReactorGuiBase(T container, Inventory inv, Component title) {
        super(container, inv, title);
        tile = container.tile;
        inventory = inv;
    }

    public ReactorGuiBase(T container, Inventory inv, Component title, int imageWidth, int imageHeight) {
        super(container, inv, title, imageWidth, imageHeight);
        tile = container.tile;
        inventory = inv;
    }

    protected abstract String getGuiTexture();

    protected Identifier getTextureIdentifier() {
        return Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "textures/gui/" + getGuiTexture() + ".png");
    }

    public final int getXSize() {
        return imageWidth;
    }

    public final int getYSize() {
        return imageHeight;
    }

    public int getGuiLeft() {
        return leftPos;
    }

    public int getGuiTop() {
        return topPos;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // TODO(OFF-50): the legacy ReactorGuiBase added two ImagedGuiButtons (ids 24000/24001) that
    // open the reactor handbook (GuiReactorBook). Re-add them once the book screens are ported.

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int j = (width - imageWidth) / 2;
        int k = (height - imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, getTextureIdentifier(), j, k, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        api.drawCenteredStringNoShadow(graphics, font, this.title.getString(), imageWidth / 2, 5, 4210752);
        if (tile instanceof Container && this.showInventoryLabel()) {
            graphics.text(font, I18n.get("container.inventory"), imageWidth - 58, (imageHeight - 96) + 3, 4210752);
        }
    }

    protected boolean showInventoryLabel() {
        return true;
    }
}
