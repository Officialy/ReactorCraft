/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 ******************************************************************************/
package reika.reactorcraft.guis;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import reika.reactorcraft.base.ReactorGuiBase;
import reika.reactorcraft.container.MenuCentrifuge;
import reika.reactorcraft.tileentities.processing.TileEntityCentrifuge;

/**
 * 26.2 port of {@code GuiCentrifuge}. Draws the UF6-separation progress arrow and the tank frame
 * overlay on top of the {@code centrifuge.png} background. The live fluid-fill sprite is deferred —
 * the whole port still lacks the 26.2 still-sprite GUI render (cf. RotaryCraft {@code ReservoirScreen}).
 */
public class ScreenCentrifuge extends ReactorGuiBase<TileEntityCentrifuge, MenuCentrifuge> {

    public ScreenCentrifuge(MenuCentrifuge container, Inventory inv, Component title) {
        super(container, inv, title, 176, 166);
    }

    @Override
    protected String getGuiTexture() {
        return "centrifuge";
    }

    @Override
    protected boolean showInventoryLabel() {
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int j = (width - imageWidth) / 2;
        int k = (height - imageHeight) / 2;

        // TODO: live fluid-fill sprite at (j+80, k+78-getFluidScaled(60)) — blocked on the 26.2
        // still-sprite GUI render gap shared with RotaryCraft. Tank level still reaches the client.

        // tank frame overlay (gui sheet u=223,v=83)
        graphics.blit(RenderPipelines.GUI_TEXTURED, getTextureIdentifier(), j + 80, k + 18, 223, 83, 16, 60, 256, 256);

        // UF6-separation progress arrow (gui sheet u=216,v=84)
        int prog = tile.getProcessingScaled(48);
        graphics.blit(RenderPipelines.GUI_TEXTURED, getTextureIdentifier(), j + 104, k + 18, 216, 84, 4, prog, 256, 256);
    }
}
