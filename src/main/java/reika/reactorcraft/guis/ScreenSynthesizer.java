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
import reika.reactorcraft.container.MenuSynthesizer;
import reika.reactorcraft.tileentities.processing.TileEntitySynthesizer;

/**
 * 26.2 port of {@code GuiSynthesizer}. Draws the ammonia-synthesis progress arrow; input/output tank
 * fills are deferred (live fluid sprite gap). Texture {@code synthesizer.png}.
 */
public class ScreenSynthesizer extends ReactorGuiBase<TileEntitySynthesizer, MenuSynthesizer> {

    public ScreenSynthesizer(MenuSynthesizer container, Inventory inv, Component title) {
        super(container, inv, title, 176, 175);
    }

    @Override
    protected String getGuiTexture() {
        return "synthesizer";
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int j = (width - imageWidth) / 2;
        int k = (height - imageHeight) / 2;

        int prog = tile.getTimerScaled(24);
        graphics.blit(RenderPipelines.GUI_TEXTURED, getTextureIdentifier(), j + 103, k + 26, 176, 92, prog, 34, 256, 256);
    }
}
