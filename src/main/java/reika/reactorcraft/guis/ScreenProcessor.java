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
import reika.reactorcraft.container.MenuProcessor;
import reika.reactorcraft.tileentities.processing.TileEntityUProcessor;

/**
 * 26.2 port of {@code GuiProcessor}. Two progress arrows (intermediate step + output step); the
 * water/HF/UF6 tank fills are deferred (live fluid sprite gap). Texture {@code processor.png}.
 */
public class ScreenProcessor extends ReactorGuiBase<TileEntityUProcessor, MenuProcessor> {

    public ScreenProcessor(MenuProcessor container, Inventory inv, Component title) {
        super(container, inv, title, 176, 175);
    }

    @Override
    protected String getGuiTexture() {
        return "processor";
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int j = (width - imageWidth) / 2;
        int k = (height - imageHeight) / 2;

        int interm = tile.getIntermediateTimerScaled(24);
        graphics.blit(RenderPipelines.GUI_TEXTURED, getTextureIdentifier(), j + 67, k + 21, 176, 92, interm, 17, 256, 256);

        int out = tile.getOutputTimerScaled(24);
        graphics.blit(RenderPipelines.GUI_TEXTURED, getTextureIdentifier(), j + 67, k + 58, 176, 92, out, 17, 256, 256);
    }
}
